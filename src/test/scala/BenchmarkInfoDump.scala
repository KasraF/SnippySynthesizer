import java.io.File

import net.liftweb.json
import net.liftweb.json.JObject

import scala.collection.mutable
import scala.io.Source.fromFile

object BenchmarkInfoDump extends App {

	val benchmarksDir = new File("src/test/resources")
	assert(benchmarksDir.isDirectory)
	println("benchmark group,examples,files,iterations,loop examples,vars")
	val dirs = benchmarksDir.listFiles().flatMap(f => if (f.isDirectory) f :: f.listFiles.toList else Nil)
	for (dir <- dirs)
		if (dir.isDirectory)
			benchmarkDirStatistics(dir)

	def benchmarkDirStatistics(dir: File): Unit = {
		val fs = dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
		if (fs.isEmpty) return
		var files = 0
		val examples = mutable.ArrayBuffer[Int]()
		val loopExamples = mutable.ArrayBuffer[Int]()
		val iterations = mutable.ArrayBuffer[Int]()
		val vars = mutable.ArrayBuffer[Int]()
		fs.sorted
		  .foreach { file =>
			  files += 1
			  val taskStr = fromFile(file).mkString
			  val task = json.parse(taskStr).asInstanceOf[JObject].values
			  val loopEx = task("previousEnvs").asInstanceOf[Map[String, Any]].count { pe =>
				  val peIter = task("envs").asInstanceOf[List[Map[String, Any]]].find(e => e("time") == pe._1.toInt)
				  peIter match {
					  case None => false
					  case Some(value) => value("#") != ""
				  }
			  }
			  examples += (
				task("envs").asInstanceOf[List[Map[String, Any]]].count(e => e("#") == "") +
				loopEx)
			  loopExamples += loopEx
			  iterations += task("envs").asInstanceOf[List[Map[String, Any]]].count(e => e("#") != "")
			  vars += task("varNames").asInstanceOf[List[Any]].length
		  }
		val dirName = if (dir.getParentFile.getName == "resources") dir.getName else dir.getParentFile.getName
		println(s"$dirName,${examples.sum.toDouble},$files,${if (loopExamples.sum == 0) 0 else iterations.sum.toDouble},${loopExamples.sum},${vars.sum}")
	}
}
