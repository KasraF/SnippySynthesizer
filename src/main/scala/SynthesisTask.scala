package sygus

import ast.Types.Types
import ast._
import net.liftweb.json.JsonAST.{JObject, JValue}
import net.liftweb.json.JsonParser

trait SynthesisTask
{
    val returnType: ast.Types.Value
    val parameters: List[(String, ast.Types.Value)]
    val vocab: VocabFactory
    val examples: List[Example]

    def fit(program: ASTNode): (Int, Int)
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

class PythonPBETask(content: String) extends SynthesisTask
{
    override val returnType: ast.Types.Value = Types.String
    override val parameters: List[(String, ast.Types.Value)] = List(("s", Types.String))

    override val vocab: VocabFactory = PythonPBETask.vocabFactory(null)
    override val examples: List[Example] = List(Example(Map(("s", "Hello")), "H"))

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
        var x: JValue = JsonParser.parse(json)
        var examples =
            x.children
             .map(obj => obj.asInstanceOf[JObject])
             .map(obj => obj.values)
             .map(values => values.filter(entry => !PythonExample.reserved_names.contains(entry._1)))
        new PythonPBETask(json)
    }

    private def vocabFactory(variables: Map[String, String]): VocabFactory =
    {
        val vocab: List[VocabMaker] = List(
	        new VocabMaker
	        {
		        override val arity: Int = 0
		        override val childTypes: List[Types] = Nil
		        override val returnType: Types = Types.String
		        override val head: String = "s"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new StringVariable("s", contexts)
	        },
	        // Literals
	        new VocabMaker
	        {
		        override val arity: Int = 0
		        override val childTypes: List[Types] = Nil
		        override val returnType: Types = Types.String
		        override val head: String = " "

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new StringLiteral(" ", contexts.length)
	        },
	        new VocabMaker
	        {
		        override val arity: Int = 0
		        override val childTypes: List[Types] = Nil
		        override val returnType: Types = Types.Int
		        override val head: String = "0"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new IntLiteral(0, contexts.length)
	        },
	        new VocabMaker
	        {
		        override val arity: Int = 0
		        override val childTypes: List[Types] = Nil
		        override val returnType: Types = Types.Int
		        override val head: String = "1"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new IntLiteral(1, contexts.length)
	        },
	        // Binary Ops
	        new VocabMaker {
		        override val arity: Int = 2
		        override val childTypes: List[Types] = List(Types.Int, Types.Int)
		        override val returnType: Types = Types.Int
		        override val head: String = "+"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new IntAddition(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
	        },
	        new VocabMaker {
		        override val arity: Int = 2
		        override val childTypes: List[Types] = List(Types.Int, Types.Int)
		        override val returnType: Types = Types.Int
		        override val head: String = "-"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new IntSubtraction(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
	        },
	        new VocabMaker {
		        override val arity: Int = 2
		        override val childTypes: List[Types] = List(Types.Int, Types.Int)
		        override val returnType: Types = Types.Int
		        override val head: String = "<="

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new IntLessThanEq(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
	        },
	        new VocabMaker {
		        override val arity: Int = 2
		        override val childTypes: List[Types] = List(Types.Int, Types.Int)
		        override val returnType: Types = Types.Int
		        override val head: String = "=="

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new IntEquals(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
	        },
	        new VocabMaker {
		        override val arity: Int = 2
		        override val childTypes: List[Types] = List(Types.String, Types.String)
		        override val returnType: Types = Types.String
		        override val head: String = "+"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new StringConcat(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
	        },
	        new VocabMaker {
		        // TODO Support start:end:step indexing
		        override val arity: Int = 2
		        override val childTypes: List[Types] = List(Types.String, Types.Int)
		        override val returnType: Types = Types.String
		        override val head: String = "[]"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new StringAt(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
	        },
	        new VocabMaker {
		        override val arity: Int = 2
		        override val childTypes: List[Types] = List(Types.String, Types.String)
		        override val returnType: Types = Types.Bool
		        override val head: String = "in"

		        override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
			        new Contains(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
	        },
	        // Functions
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
	        })

        VocabFactory(vocab)
    }
}