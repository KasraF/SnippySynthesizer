package sygus

import ast.Types.Types
import ast._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonParser

trait SynthesisTask
{
	var returnType: ast.Types.Value
	var parameters: List[(String, ast.Types.Value)]
	var vocab: VocabFactory
	var examples: List[Example]

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

class PythonPBETask extends SynthesisTask
{
	override var returnType: ast.Types.Value = _
	override var parameters: List[(String, ast.Types.Value)] = _

	override var vocab: VocabFactory = _
	override var examples: List[Example] = _

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
	def fromString(json: String): PythonPBETask =
	{
		val examples = JsonParser.parse(json).children.map(obj => obj.asInstanceOf[JObject])
		  .map(obj => obj.values)
		  .map(values => values.filter(entry => !PythonExample.reserved_names.contains(entry._1)))
		val before: Map[String, AnyRef] = examples.head.asInstanceOf[Map[String, AnyRef]]
		val after: Map[String, AnyRef] =
			examples.tail.head.asInstanceOf[Map[String, AnyRef]].
			  filter(entry => !before.contains(entry._1) || !before(entry._1).equals(entry._2))

		// TODO How can we support multiple results/variable assignment?
		assert(after.size == 1, "Multiple results not supported")

		val rs = new PythonPBETask
		{
			returnType = Types.typeof(after.head._2)
			parameters = before.toList.map(entry => (entry._1, Types.typeof(entry._2)))
			vocab = PythonPBETask.vocabFactory(parameters)
			examples = List(Example(before, after.head._2))
		}

		trace.DebugPrints.dprintln(s"Solving Python PBE Task:\n\n$rs")

		rs
	}

	private def vocabFactory(variables: List[(String, Types.Value)]): VocabFactory =
	{
		val vocab: List[VocabMaker] = List(
			// Literals
			new VocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val head: String = " "

				override def apply(children    : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringLiteral(" ", contexts.length)
			},
			new VocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				override val head: String = "0"

				override def apply(children    : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntLiteral(0, contexts.length)
			},
			new VocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				override val head: String = "1"

				override def apply(children    : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntLiteral(1, contexts.length)
			},
			// Binary Ops
			new VocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int
				override val head: String = "+"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntAddition(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new VocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int
				override val head: String = "-"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntSubtraction(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new VocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int
				override val head: String = "//"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntDivision(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new VocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int
				override val head: String = "<="

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntLessThanEq(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new VocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.Int, Types.Int)
				override val returnType: Types = Types.Int
				override val head: String = "=="

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntEquals(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
			},
			new VocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.String)
				override val returnType: Types = Types.String
				override val head: String = "+"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringConcat(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
			},
			new VocabMaker
			{
				// TODO Support start:end:step indexing
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.Int)
				override val returnType: Types = Types.String
				override val head: String = "[]"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringAt(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
			},
			new VocabMaker
			{
				override val arity: Int = 2
				override val childTypes: List[Types] = List(Types.String, Types.String)
				override val returnType: Types = Types.Bool
				override val head: String = "in"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new Contains(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
			},
			new VocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.String)
				override val returnType: Types = Types.Int
				override val head: String = "len"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringLength(children.head.asInstanceOf[StringNode])
			},
			new VocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.String)
				override val returnType: Types = Types.String
				override val head: String = "lower"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringLower(children.head.asInstanceOf[StringNode])
			},
			new VocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.String)
				override val returnType: Types = Types.Int
				override val head: String = "int"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringToInt(children.head.asInstanceOf[StringNode])
			},
			new VocabMaker
			{
				override val arity: Int = 1
				override val childTypes: List[Types] = List(Types.Int)
				override val returnType: Types = Types.String
				override val head: String = "str"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntToString(children.head.asInstanceOf[IntNode])
			},
			new VocabMaker
			{
				override val arity: Int = 3
				override val childTypes: List[Types] = List(Types.String, Types.Int, Types.Int)
				override val returnType: Types = Types.String
				override val head: String = "[:]"

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new TernarySubstring(
						children.head.asInstanceOf[StringNode],
						children(1).asInstanceOf[IntNode],
						children(2).asInstanceOf[IntNode])
			})
//			new VocabMaker
//			{
//				override val arity: Int = 4
//				override val childTypes: List[Types] = List(Types.String, Types.Int, Types.Int, Types.Int)
//				override val returnType: Types = Types.String
//				override val head: String = "[::]"
//
//				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
//					new QuaternarySubstring(
//						children.head.asInstanceOf[StringNode],
//						children(1).asInstanceOf[IntNode],
//						children(2).asInstanceOf[IntNode],
//						children(3).asInstanceOf[IntNode])
//			}

		VocabFactory(vocab.appendedAll(
			variables.
			  filter(variable => !variable._2.equals(Types.Unknown)).
			  map {
				  case (name, Types.String) => new VocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.String
					  override val head: String = name

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new StringVariable(name, contexts)
				  }
				  case (name, Types.Int) => new VocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.Int
					  override val head: String = name

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new IntVariable(name, contexts)
				  }
				  case (name, Types.Bool) => new VocabMaker
				  {
					  override val arity: Int = 0
					  override val childTypes: List[Types] = Nil
					  override val returnType: Types = Types.Bool
					  override val head: String = name

					  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						  new BoolVariable(name, contexts)
				  }
			  }
			))
	}
}