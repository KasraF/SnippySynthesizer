package sygus

import ast.Types.Types
import ast._
import net.liftweb.json.JsonAST.{JObject, JValue}
import net.liftweb.json.JsonParser

trait SynthesisTask {
	val returnType: ast.Types.Value
	val parameters: List[(String, ast.Types.Value)]
	val vocab: VocabFactory
	val examples: List[Example]

	def fit(program: ASTNode): (Int, Int)
}

class PythonExample(var env: Map[String, String]) {
	env = env.filter(pair => PythonExample.reserved_names.contains(pair._1))
}

object PythonExample {
	val reserved_names: Set[String] =
		Set("time", "#", "$", "lineno", "prev_lineno", "next_lineno", "__run_py__")
}

class PythonPBETask(content: String) extends SynthesisTask {
	override val returnType: ast.Types.Value = ast.Types.Bool
	override val parameters: List[(String, ast.Types.Value)] = List()

	override val vocab: VocabFactory = PythonPBETask.vocabFactory()
	override val examples: List[Example] = List()

	override def fit(program: ASTNode): (Int, Int) = (1,1)
}

object PythonPBETask {
	def fromString(json: String) : PythonPBETask = {
		var x: JValue = JsonParser.parse(json)
		var examples =
			x.children
			 .map(obj => obj.asInstanceOf[JObject])
			 .map(obj => obj.values)
			 .map(values => values.filter(entry => !PythonExample.reserved_names.contains(entry._1)))
		println(examples)
		new PythonPBETask(json)
	}

	private def vocabFactory() : VocabFactory =
	{
		val vocab: List[VocabMaker] =
			List() :+
			  // Literals
			  new VocabMaker {
				  override val arity: Int = 0
				  override val childTypes: List[Types] = Nil
				  override val returnType: Types = Types.String
				  override val head: String = " "

				  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					  new StringLiteral(" ", contexts.length)
			  } :+
			  new VocabMaker {
				  override val arity: Int = 0
				  override val childTypes: List[Types] = Nil
				  override val returnType: Types = Types.Int
				  override val head: String = "0"

				  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					  new IntLiteral(0, contexts.length)
			  } :+
			  new VocabMaker {
				  override val arity: Int = 0
				  override val childTypes: List[Types] = Nil
				  override val returnType: Types = Types.Int
				  override val head: String = "1"

				  override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					  new IntLiteral(1, contexts.length)
			  }

		VocabFactory(vocab)
	}
}