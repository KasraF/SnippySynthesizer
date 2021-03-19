import edu.ucsd.snippy.{DebugPrints, Snippy}
import net.liftweb.json
import net.liftweb.json.JObject

import java.io.File
import scala.io.Source.fromFile

object Benchmarks extends App
{
	def runBenchmark(dir: File, timeout: Int = 7): Unit = {
		println("----- - ----------------------- ------- ---------- --------------------------------------")
		var total = 0
		var failed = 0
		var timeout = 0
		var unknown = 0
		dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
			.sorted
			.zipWithIndex
			.foreach(benchmark => {
				total += 1
				val file = benchmark._1
				val index = benchmark._2 + 1
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				var printStr: String = ""

				try {
					val taskStr = fromFile(file).mkString
					val task = json.parse(taskStr).asInstanceOf[JObject].values

					Snippy.synthesize(taskStr, timeout) match {
						case (None, time: Int, count: Int) =>
							printStr = f"($index%2d) [?] [$name%22s] [${time / 1000.0}%.3f] [$count%8d] Timeout"
							timeout += 1
						case (Some(program: String), time: Int, count: Int) =>
							val correct = task.get("solutions") match {
								case Some(solutions) if solutions.asInstanceOf[List[String]].contains(program) => '+'
								case Some(_) =>
									failed += 1
									'-'
								case None =>
									unknown += 1
									'?'
							}
							printStr = f"($index%2d) [$correct] [$name%22s] [${time / 1000.0}%.3f] [$count%8d] "
							val str = if (program.contains('\n')) {
								program.split('\n').mkString('\n' + " ".repeat(printStr.length))
							} else {
								program
							}
							printStr += str
					}
				} catch {
					case e: AssertionError => throw e
					case e: Throwable => printStr += e
				}

				println(printStr)
				Runtime.getRuntime.gc()
			})
		println(f"${dir.getName}: $total total, $timeout timeouts, $failed failed, $unknown unknown")
	}

	println(
		" _____       _      ______      \n" +
		"/  ___|     (_)     | ___ \\     \n" +
		"\\ `--. _ __  _ _ __ | |_/ /   _ \n" +
		" `--. \\ '_ \\| | '_ \\|  __/ | | |\n" +
		"/\\__/ / | | | | |_) | |  | |_| |\n" +
		"\\____/|_| |_|_| .__/\\_|   \\__, |\n" +
		"              | |          __/ |\n" +
		"              |_|         |___/ \n" +
		"+------------------------------+\n" +
		"| Snippet synthesis for Python |\n" +
		"+------------------------------+")
	println("Index V Name                     Time    Count      Program")

	val benchmarks = new File("src/test/resources")
	assert(benchmarks.isDirectory)

	DebugPrints.debug = false
	DebugPrints.info = false

	if (args.nonEmpty) {
		val timeout = args.last.toIntOption match {
			case Some(t) => t
			case _ => 7
		}

		benchmarks.listFiles()
			.filter(_.isDirectory)
			.filter(dir => args.contains(dir.getName))
			.foreach(this.runBenchmark(_, timeout))
	} else {
		benchmarks.listFiles()
			.filter(_.isDirectory)
			.sortBy(_.getName)(Ordering.String)
			.foreach(this.runBenchmark(_))
	}
}
