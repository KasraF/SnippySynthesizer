import edu.ucsd.snippy.{DebugPrints, Snippy}

import java.io.File

object Benchmarks extends App
{
	def runBenchmark(dir: File, timeout: Int = 7): Unit = {
		println("----- ------------------------ ------- ---------- --------------------------------------")
		dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
			.sorted
			.zipWithIndex
			.foreach(benchmark => {
				val file = benchmark._1
				val index = benchmark._2 + 1
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				print(f"($index%2d)  [$name%22s] ")

				try {
					Snippy.synthesize(file, timeout) match {
						case (None, time: Int, count: Int) => println(f"[${time / 1000.0}%.3f] [$count%8d] Timeout") // println(f"[$count%d] Timeout")
						case (Some(program: String), time: Int, count: Int) =>
							val str = if (program.contains('\n')) {
								program.split('\n').mkString('\n' + " ".repeat(50))
							} else {
								program
							}
							println(f"[${time / 1000.0}%.3f] [$count%8d] $str")
					}
				} catch {
					case e: AssertionError => throw e
					case e: Throwable => println(e)
				}

				Runtime.getRuntime.gc()
			})
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
	println("Index Name                     Time    Count      Program")

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
