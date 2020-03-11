package sygus

import ast.Types.Types
import ast._
import net.liftweb.json.JsonAST.{JArray, JObject}
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
		// TODO What's a faster way to do this?
		s"\treturnType: $returnType\n" +
		  s"\tparameters: $parameters\n" +
		  "\tvocab: ...\n" +
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
	private def cleanupInput(input: Any) : Option[Any] =
	{
		if (!input.isInstanceOf[String])
			None
		else input.asInstanceOf[String].trim match {
			case "" => None
			case s if s(0).equals('\'') =>
				// String
				Some(s.substring(1, s.length - 1))
			case s if s.forall(_.isDigit) =>
				// Int
				Some(s.toInt)
			case s if s.startsWith("[") =>
				// List
				Some(s.substring(1, s.length - 1).split(',').map(v => cleanupInput(v).get).toList)
			case s if s.startsWith("{") && s.contains(":") =>
				// Map
				Some(s.substring(1, s.length - 1)
				       .split(',')
				       .map(entryStr => entryStr.split(':').map(e => cleanupInput(e).get))
				       .map(lst => lst.head -> lst.tail.head)
				       .toMap)
			case s if s.startsWith("{") =>
				// Set
				Some(s.substring(1, s.length - 1).split(',').map(e => cleanupInput(e).get).toSet)
			case _ => None
		}
	}

	private def cleanupInputs(input: Map[String, Any]): Map[String, Any] = {
		input
		  .filter(v => !PythonExample.reserved_names.contains(v._1))
		  // TODO Is there a cleaner way to do this?
		  .map(variable => cleanupInput(variable._2) match {
			  case None =>
				  trace.DebugPrints.eprintln(s"Input not recognized: $variable")
				  (variable._1, null)
			  case Some(v) =>
				  (variable._1, v)
		  })
  		.filter(v => v._2 != null)
	}

	def buildExample(lst: List[Map[String, Any]]) : (String, Example) =
	{
		assert(lst.length == 2, "Invalid input format: 2 Objects not provided for example")
		val outputs = lst.tail.head.filter(v => !lst.head.contains(v._1))
		assert(outputs.size == 1, "Invalid input format: Output variables not exactly 1")
		(outputs.head._1, Example(lst.head, outputs.head._2))
	}

	def fromString(json: String): PythonPBETask =
	{
		val json_examples = JsonParser.parse(json).children
		  .map(lst => lst.asInstanceOf[JArray].children.map(obj => cleanupInputs(obj.asInstanceOf[JObject].values)))
  		  .map(buildExample)

		assert(json_examples.nonEmpty, "No examples provided")
		val outputVarName: String = json_examples.head._1
		assert(json_examples.forall(_._1.equals(outputVarName)), "Different output variables")

		val examples = json_examples.map(_._2)
		val returnType = Types.typeof(examples.head.output)
		val parameters = examples.head.input.map(example => (example._1, Types.typeof(example._2))).toList
		val vocab = PythonPBETask.vocabFactory(parameters)

		val rs = new PythonPBETask(returnType, parameters, vocab, examples, outputVarName)
		trace.DebugPrints.dprintln(s"Solving Python PBE Task:\n\n$rs")
		rs
	}

	private def vocabFactory(variables: List[(String, Types.Value)]): VocabFactory =
	{
		val vocab: List[VocabMaker] = List(
			// Literals
			new BasicVocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				
				override def apply(children    : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringLiteral(" ", contexts.length)
			},
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
			// Binary Ops
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
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.String)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringLength(children.head.asInstanceOf[StringNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.IntList)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Min(children.head.asInstanceOf[IntListNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.IntList)
				override val returnType: Types = Types.Int
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Max(children.head.asInstanceOf[IntListNode])
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
					new StringJoin(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[StringListNode])
			},
			new BasicVocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.StringList)
				override val returnType: Types = Types.StringList
				
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new SortedStringList(children.head.asInstanceOf[StringListNode])
			},
			new ListCompVocabMaker(Types.String, Types.String) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new StringToStringListCompNode(
						lst.asInstanceOf[StringListNode],
						map.asInstanceOf[StringNode],
						this.varName)
			},
			new ListCompVocabMaker(Types.String, Types.Int) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new StringToIntListCompNode(
						lst.asInstanceOf[StringListNode],
						map.asInstanceOf[IntNode],
						this.varName)
			},
			new ListCompVocabMaker(Types.Int, Types.String) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new IntToStringListCompNode(
						lst.asInstanceOf[IntListNode],
						map.asInstanceOf[StringNode],
						this.varName)
			},
			new ListCompVocabMaker(Types.Int, Types.Int) {
				override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
					new IntToIntListCompNode(
						lst.asInstanceOf[IntListNode],
						map.asInstanceOf[IntNode],
						this.varName)
			}
//			new BasicVocabMaker
//			{
//				override val arity: Int = 4
//				override val childTypes: List[Types] = List(Types.String, Types.Int, Types.Int, Types.Int)
//				override val returnType: Types = Types.String
//				//
//				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
//					new QuaternarySubstring(
//						children.head.asInstanceOf[StringNode],
//						children(1).asInstanceOf[IntNode],
//						children(2).asInstanceOf[IntNode],
//						children(3).asInstanceOf[IntNode])
//			}
		)

		VocabFactory(vocab.appendedAll(
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
				  case (name, typ) =>
					  assert(assertion = false, s"Input type $typ not supported for input $name")
					  null
			  }
			))
	}
}