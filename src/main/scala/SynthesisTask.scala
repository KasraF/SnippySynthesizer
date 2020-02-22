package sygus

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

	override val vocab: VocabFactory = null
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
		new PythonPBETask(json)
	}
}