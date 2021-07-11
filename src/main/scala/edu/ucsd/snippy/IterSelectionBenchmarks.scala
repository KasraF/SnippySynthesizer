package edu.ucsd.snippy
import java.io.File
import edu.ucsd.snippy.Snippy
import net.liftweb.json
import net.liftweb.json.{DefaultFormats, JObject}
import net.liftweb.json.JsonAST.JValue

import scala.io.Source.fromFile
import scala.util.Random

object IterSelectionBenchmarks extends App {
	def runBenchmarks(dir: File, timeout: Int, print: Boolean = true)
	{
		dir.listFiles()
			.filter(_.getName.contains(".fullex.json"))
			.sorted
			.foreach { file =>
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				val task = json.parse(fromFile(file).mkString).asInstanceOf[JObject].values
				val examples = collectExamples(task).filter(_._2.nonEmpty).sortBy(_._1._1.toInt)
				for (numExamples <- 1 to 4; numIters <- 1 to 5) {
					val participatingExamples = examples.take(numExamples)

					val taskCopy = task.filter(kv => kv._1 != "envs" && kv._1 != "previousEnvs") ++
						Map("previousEnvs" -> participatingExamples.map(_._1).toMap,
							"envs" -> participatingExamples.flatMap(_._2.take(numIters)))

					implicit val formats: DefaultFormats.type = net.liftweb.json.DefaultFormats
					val newTaskStr = json.prettyRender(json.Extraction.decompose(taskCopy))
					Snippy.synthesize(newTaskStr, timeout) match {
						case (None, _: Int, _: Int) =>
							if (print) println(s"$name,$numExamples,$numIters,Timeout")
						case (Some(program: String), _: Int, _: Int) =>
							val correct = task.get("solutions") match {
								case Some(solutions) if solutions.asInstanceOf[List[String]].contains(program) => '+'
								case Some(_) =>
									'-'
								case None =>
									'?'
							}
							if (print) println(s"$name,$numExamples,$numIters,$correct")
					}
				}
			}
	}

	def collectExamples(taskJson: Map[String, Any]) = {
		val examples = taskJson("previousEnvs").asInstanceOf[Map[String,Any]]
		for (example <- examples.toList) yield {
			val startTime = example._1.toInt
			val envs = taskJson("envs").asInstanceOf[List[Any]].sortBy{case m: Map[String,Any] => m("time").asInstanceOf[BigInt].toInt}
			val exampleEnvs = envs
				.dropWhile{case m: Map[String,Any] => m("time").asInstanceOf[BigInt] < startTime}
				.takeWhile{case m: Map[String,Any] => m("time").asInstanceOf[BigInt] == startTime || m("#").asInstanceOf[String].toInt > 0}
			(example,exampleEnvs)
		}
	}

	val timeout = 7 //seconds
	val dir = new File("src/test/resources/frangel_all_iters/")

	println("name,examples,iters,correct")

	// First, warm up!
	runBenchmarks(dir, timeout, print = false)

	// Then actually run. :)
	runBenchmarks(dir, timeout)
}
