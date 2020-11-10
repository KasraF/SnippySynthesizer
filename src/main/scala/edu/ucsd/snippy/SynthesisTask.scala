package edu.ucsd.snippy

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.vocab._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonParser


trait SynthesisTask
{
	val returnType: ast.Types.Value
	val parameters: List[(String, ast.Types.Value)]
	val vocab: VocabFactory
	val examples: List[Example]

	def fit(program: ASTNode): (Int, Int)

	override def toString: String =
	{
		s"\treturnType: $returnType\n" +
		  s"\tparameters: $parameters\n" +
		  "\tvocab: [...]\n" +
		  s"\texamples: $examples"
	}
}

class PythonExample(var env: Map[String, String])
{
	env = env.filter(pair => PythonExample.reserved_names.contains(pair._1))
}

object PythonExample
{
	val reserved_names: Set[String] =
		Set("time", "#", "$", "lineno", "prev_lineno", "next_lineno", "__run_py__")
}

class PythonPBETask(
  val returnType: ast.Types.Value,
  val parameters: List[(String, ast.Types.Value)],
  val vocab: VocabFactory,
  val examples: List[Example],
  val outputVar: String) extends SynthesisTask
{
	override def fit(program: ASTNode): (Int, Int) =
	{
		val expectedResults = examples.map(_.output)
		val k = program.values.zip(expectedResults).count(pair => pair._1 == pair._2)
		val n = expectedResults.length
		(k, n)
	}
}

object PythonPBETask
{
	private def cleanupInputs(input: Map[String, Any]): Map[String, Any] = {
		val parser = new InputParser
		input
		  .filter(v => !PythonExample.reserved_names.contains(v._1))
		  // TODO Is there a cleaner way to do this?
		  .filter(_._2.isInstanceOf[String])
		  .map(variable => parser.parse(variable._2.asInstanceOf[String]) match {
			  case None =>
				  DebugPrints.eprintln(s"Input not recognized: $variable")
				  (variable._1, null)
			  case Some(v) =>
				  (variable._1, v)
		  })
  		.filter(v => v._2 != null)
	}

	private def getTypeOfAll(values: List[Any]): Types = {
		val (empty, nonempty) = values.partition(v => v.isInstanceOf[Iterable[_]] && v.asInstanceOf[Iterable[_]].isEmpty)
		val neType = if (nonempty.isEmpty) Types.Unknown else nonempty.map(v => Types.typeof(v)).reduce((acc,t) => if (acc == t) t else Types.Unknown)
		if (empty.nonEmpty) {
			if (nonempty.isEmpty){
				val defaultTypes: Set[Types] = empty.map {
					case _: List[_] => Types.StringList
					case _: Map[_, _] => Types.StringIntMap
				}.toSet
				return if (defaultTypes.size == 1) defaultTypes.head else Types.Unknown
			}
			else  for (v <- empty) {
				if (neType match {
					case Types.StringList | Types.IntList => !v.isInstanceOf[List[_]]
					case Types.Map(_, _) => !v.isInstanceOf[Map[_, _]]
					case _ => false //nonempties are not a list/map, fail.
				}) return Types.Unknown
			}
			neType
		}
		else neType
	}

	def fromString(jsonString: String): PythonPBETask =
	{
		val input = JsonParser.parse(jsonString).asInstanceOf[JObject].values
		val outputVarName: String = input("varName").asInstanceOf[String]
		val envs: List[Map[String, Any]] = input("envs").asInstanceOf[List[Map[String, Any]]]
		val previousEnv: Map[String, Any] =
			cleanupInputs(input("previous_env").asInstanceOf[Map[String, Any]])
		val environments: List[Map[String, Any]] = envs
		  .asInstanceOf[List[Map[String, Any]]]
		  .map(cleanupInputs)

		// Loops!
		// Check if we have consecutive iterations of the loop
		val loopy = {
			previousEnv.nonEmpty &&
			envs.head.contains("#") &&
			// Not in a loop, but reusing variable
			(envs.head("#").asInstanceOf[String].isEmpty && previousEnv.contains(outputVarName) ||
			// In a loop
			(envs.head("#").asInstanceOf[String].nonEmpty &&
			  envs.tail.foldLeft(envs.head("#").asInstanceOf[String].toInt)((currIndex, env) => {
				  if (currIndex >= 0 && env("#").asInstanceOf[String].toInt == currIndex + 1) {
					  currIndex + 1
				  } else {
					  -1
				  }
			  }) != -1))
		}

		val examples = if (loopy) {
			// We are in a loop!
			// If we are in a loop, and modifying a variable defined outside the loop, we can
			// reuse its existing value.

			// TODO How to tell if variable was indeed defined outside the loop?
			// Just add the "previous_env" by adding the env of the line right above this one!!

			environments
			  .zip(previousEnv :: environments.slice(0, environments.length - 1))
			  .map {
				  case (curr_env, prev_env) =>
					  Example(
						  curr_env.filter(_._1 != outputVarName).filter(_._1 != "#") + (outputVarName -> prev_env(outputVarName)),
						  curr_env(outputVarName))
			  }
		} else {
			// Not in a loop. Do the usual.
			environments.map(env => Example(env.filter(_._1 != outputVarName).filter(_._1 != "#"), env(outputVarName)))
		}

		val returnType = getTypeOfAll(examples.map(_.output))
		val parameters =
			examples.head.input
			  .map { inputVar =>
				  val varValueOpts = examples.map(ex => ex.input.find(kv => kv._1 == inputVar._1))
				  (inputVar._1, if (varValueOpts.exists(_.isEmpty)) Types.Unknown else getTypeOfAll(varValueOpts.flatten.map(_._2)))
			  }
			  // TODO Handle empty sets
			  .filter(!_._2.equals(Types.Unknown))
			  .toList
		val additionalLiterals = getStringLiterals(examples, outputVarName)
		val vocab = PythonPBETask.vocabFactory(parameters, additionalLiterals)

		val rs = new PythonPBETask(returnType, parameters, vocab, examples, outputVarName)
		// trace.DebugPrints.dprintln(s"Solving Python PBE Task:\n\n$rs")
		rs
	}

	private def getStringLiterals(examples: List[Example], outputName: String): List[String] =
	{
		if (examples.exists(ex => Types.typeof(ex.output) != Types.String)) //this is only for strings
		{
			return Nil
		}
		val opts = examples.map {
			ex =>
				val outputVal = ex.output.asInstanceOf[String]
				val stringInputs = for ((inputName, inputVal) <- ex.input; if Types.typeof(inputVal) == Types.String && inputName != outputName)
					yield inputVal.asInstanceOf[String]
				val chars: Iterable[String] =
					for (char <- outputVal; if stringInputs.forall(inputVal => !inputVal.contains(char.toLower) && !inputVal.contains(char.toUpper)))
						yield char.toString
				chars.toSet
		}
		val intersection = opts.reduce((a, b) => a.intersect(b))
		intersection.toList
	}

	private def vocabFactory(variables: List[(String, Types.Value)], additionalLiterals: List[String]): VocabFactory =
	{
		val defaultStringLiterals = List(" ")
		val stringLiterals = (defaultStringLiterals ++ additionalLiterals).distinct

		val vocab: List[VocabMaker] =
			stringLiterals.map{str =>
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.String

					override def apply(children    : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringLiteral(str, contexts.length)
				}
			} ++ List(
			// Literals
			new BasicVocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntLiteral(0, contexts.length)
			},
			new BasicVocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				
				override def apply(children    : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntLiteral(1, contexts.length)
			},
			new BasicVocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntLiteral(-1, contexts.length)
			},
			// Binary Ops
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Bool

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new GreaterThan(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Bool

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new LessThanEq(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.String)
				override val returnType: Types = Types.String
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringConcat(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.Int)
				override val returnType: Types = Types.String
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new BinarySubstring(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.Int)
				override val returnType: Types = Types.String

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringStep(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.String)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Find(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.String)
				override val returnType: Types = Types.Bool
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Contains(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.String)
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Count(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.AnyIterable)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Length(children.head.asInstanceOf[IterableNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.IntList)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Min(children.head.asInstanceOf[ListNode[Int]])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.IntList)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Max(children.head.asInstanceOf[ListNode[Int]])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.String)
				override val returnType: Types = Types.String
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringLower(children.head.asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.String)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringToInt(children.head.asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.Int)
				override val returnType: Types = Types.String
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntToString(children.head.asInstanceOf[IntNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 3
				override val childTypes: List[Types] = List(Types.String, Types.Int, Types.Int)
				override val returnType: Types = Types.String
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new TernarySubstring(
						children.head.asInstanceOf[StringNode],
						children(1).asInstanceOf[IntNode],
						children(2).asInstanceOf[IntNode])
			},
			//			new BasicVocabMaker
			//			{
			//				override val arity: Int = 3
			//				override val childTypes: List[Types] = List(Types.String, Types.String, Types.String)
			//				override val returnType: Types = Types.String
			//
			//				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			//					new StringReplace(
			//						children.head.asInstanceOf[StringNode],
			//						children(1).asInstanceOf[StringNode],
			//						children(2).asInstanceOf[StringNode])
			//			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.String)
				override val returnType: Types = Types.StringList
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringSplit(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.StringList)
				override val returnType: Types = Types.String
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringJoin(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[ListNode[String]])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.StringList)
				override val returnType: Types = Types.StringList
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new SortedStringList(children.head.asInstanceOf[ListNode[String]])
			},
			new ListCompVocabMaker(Types.String, Types.String) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new StringToStringListCompNode(
						lst.asInstanceOf[ListNode[String]],
						map.asInstanceOf[StringNode],
						this.varName)
			},
			new ListCompVocabMaker(Types.String, Types.Int) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new StringToIntListCompNode(
						lst.asInstanceOf[ListNode[String]],
						map.asInstanceOf[IntNode],
						this.varName)
			},
			new ListCompVocabMaker(Types.Int, Types.String) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new IntToStringListCompNode(
						lst.asInstanceOf[ListNode[Int]],
						map.asInstanceOf[StringNode],
						this.varName)
			},
			new ListCompVocabMaker(Types.Int, Types.Int) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new IntToIntListCompNode(
						lst.asInstanceOf[ListNode[Int]],
						map.asInstanceOf[IntNode],
						this.varName)
			},
			new ListCompVocabMaker(Types.Int, Types.Int) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new IntToIntListCompNode(
						lst.asInstanceOf[ListNode[Int]],
						map.asInstanceOf[IntNode],
						this.varName)
			},
			new MapCompVocabMaker(Types.String, Types.String) {
				override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
					new StringStringMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[StringNode], this.varName)
			},
			new MapCompVocabMaker(Types.String, Types.Int) {
				override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
					new StringIntMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[IntNode], this.varName)
			},
			new FilteredMapVocabMaker(Types.String, Types.String) {
				override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
					new StringStringFilteredMapNode(map.asInstanceOf[StringStringMapNode], filter, this.keyName)
			},
			new FilteredMapVocabMaker(Types.String, Types.Int) {
				override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
					new StringIntFilteredMapNode(map.asInstanceOf[MapNode[String,Int]], filter, this.keyName)
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.StringIntMap, Types.String)
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new MapGet(children.head.asInstanceOf[MapNode[String,Int]], children(1).asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntAddition(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntSubtraction(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntDivision(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			}
		)

		VocabFactory(
			variables.
			  map {
				  case (name, Types.String) => new BasicVocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.String

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new StringVariable(name, contexts)
				  }
				  case (name, Types.Int) => new BasicVocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.Int

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new IntVariable(name, contexts)
				  }
				  case (name, Types.Bool) => new BasicVocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.Bool

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new BoolVariable(name, contexts)
				  }
				  case (name, Types.List(childType)) => new BasicVocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.listOf(childType)

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new ListVariable(name, contexts, childType)
				  }
				  case (name, Types.Map(keyType, valType)) => new BasicVocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.mapOf(keyType, valType)

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new MapVariable(name, contexts, keyType, valType)
				  }
				  case (name, typ) =>
					  assert(assertion = false, s"Input type $typ not supported for input $name")
					  null
			  } ++ vocab
			)
	}
}