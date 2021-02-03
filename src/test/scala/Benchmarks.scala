import edu.ucsd.snippy.Snippy

import java.io.File

object Benchmarks extends App
{
	def runBenchmark(dir: File): Unit = {
		println("----- ------------------- ------- ---------- --------------------------------------")
		dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
			.sorted
			.zipWithIndex
			.foreach(benchmark => {
				val file = benchmark._1
				val index = benchmark._2 + 1
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				print(f"($index%2d)  [$name%17s] ")

				try {
					Snippy.synthesize(file) match {
						case (None, time: Int, count: Int) => println(f"[${time / 1000.0}%.3f] [$count%8d] Timeout") // println(f"[$count%d] Timeout")
						case (Some(program: String), time: Int, count: Int) =>
							val str = if (program.contains('\n')) {
								program.split('\n').mkString('\n' + " ".repeat(45))
							} else {
								program
							}
							println(f"[${time / 1000.0}%.3f] [$count%8d] $str")
					}
				} catch {
					case e: Throwable => println(e)
				}

				Runtime.getRuntime.gc()
			})
	}

	println(
		"       _____            ______      \n" +
			"      /  ___|           | ___ \\     \n" +
			"      \\ `--. _   _ _ __ | |_/ /   _ \n" +
			"       `--. \\ | | | '_ \\|  __/ | | |\n" +
			"      /\\__/ / |_| | | | | |  | |_| |\n" +
			"      \\____/ \\__, |_| |_\\_|   \\__, |\n" +
			"              __/ |            __/ |\n" +
			"             |___/            |___/ \n" +
			"-------------------------------------------\n" +
			"| Python Synthesizer for Projection Boxes |\n" +
			"-------------------------------------------\n")
	println("Index Name                Time    Count      Program")

	val benchmarks = new File("src/test/resources")
	assert(benchmarks.isDirectory)

	if (args.nonEmpty) {
		benchmarks.listFiles()
			.filter(_.isDirectory)
			.filter(dir => args.contains(dir.getName))
			.foreach(this.runBenchmark)
	} else {
		benchmarks.listFiles()
			.filter(_.isDirectory)
			.sortBy(_.getName)(Ordering.String)
			.foreach(this.runBenchmark)
	}
}
