import edu.ucsd.snippy.{DebugPrints, Snippy}
import net.liftweb.json
import net.liftweb.json.JObject

import java.io.File
import scala.io.Source.fromFile

object BenchmarksCSV extends App
{
	def runBenchmark(dir: File, benchTimeout: Int = 7, print: Boolean = true): Unit = {
		val suite = if (dir.getParentFile.getName == "resources") dir.getName else dir.getParentFile.getName + "/" + dir.getName
		dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
			.sorted
			.zipWithIndex
			.foreach(benchmark => {
				val file = benchmark._1
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				var time: Int = -1
				var correct: String = "Timeout"
				var variables = -1

				try {
					val taskStr = fromFile(file).mkString
					val task = json.parse(taskStr).asInstanceOf[JObject].values
					variables = task("varNames").asInstanceOf[List[String]].length

					Snippy.synthesize(taskStr, benchTimeout) match {
						case (Some(program: String), tim: Int, _) =>
							time = tim
							correct = task.get("solutions") match {
								case Some(solutions) if solutions.asInstanceOf[List[String]].contains(program) => "+"
								case Some(_) => "-"
								case None => "?"
							}
						case (None, _, _) => ()
					}
				} catch {
					case e: AssertionError => throw e
					case e: Throwable => sys.process.stderr.println(e)
				}

				if (print) println(s"$suite,$name,$variables,$time,$correct")
				Runtime.getRuntime.gc()
			})
	}

	val benchmarksDir = new File("src/test/resources")
	assert(benchmarksDir.isDirectory)

	DebugPrints.debug = false
	DebugPrints.info = false

	val timeout: Int = args.lastOption.map(_.toIntOption) match {
		case Some(Some(t)) => t
		case _ => 7
	}

	println("suite,name,variables,time,correct")
	val benchmarks = if (args.nonEmpty) {
		benchmarksDir.listFiles()
			.flatMap(f => if (f.isDirectory) f :: f.listFiles().toList else Nil)
			.filter(_.isDirectory)
			.filter(dir => if (dir.getParentFile.getName == "resources") args.contains(dir.getName)
			else args.contains(dir.getName) || args.contains(dir.getParentFile.getName))
			.toList
	} else {
		benchmarksDir.listFiles()
			.flatMap(f => if (f.isDirectory) f :: f.listFiles().toList else Nil)
			.filter(_.isDirectory)
			.sortBy(_.getName)(Ordering.String)
			.toList
	}

	// First, warm up
	benchmarks.foreach(this.runBenchmark(_, 1, print = false))

	// Then actually run
	benchmarks.foreach(this.runBenchmark(_, timeout))
}
