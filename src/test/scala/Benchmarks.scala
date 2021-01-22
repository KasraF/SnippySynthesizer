import edu.ucsd.snippy.Snippy

import java.io.File

object Benchmarks extends App
{
	def runBenchmark(dir: File): Unit = {
		println("----- -------------------- --------------------------------------")
		dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
			.sorted
			.zipWithIndex
			.foreach(benchmark => {
				val file = benchmark._1
				val index = benchmark._2 + 1
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				print(f"($index%2d)  [$name%18s] ")

				try {
					Snippy.synthesize(file.getAbsolutePath) match {
						case (None, _) => println("Timeout")
						case (Some(program: String), time: Int) => println(f"[${time / 1000.0}%1.3f] $program")
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
	println("Index Name                 Program")

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
