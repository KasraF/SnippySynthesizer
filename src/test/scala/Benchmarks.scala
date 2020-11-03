import java.io.File

import edu.ucsd.snippy.Snippy

object Benchmarks extends App
{
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

	benchmarks.listFiles().filter(_.isDirectory).foreach(
		dir => {
			println("----- -------------------- --------------------------------------")
			dir.listFiles()
			  .filter(_.getName.contains(".examples.json"))
			  .filter(!_.getName.contains(".out"))
			  .sorted
			  .zipWithIndex
			  .foreach(benchmark => {
				  val file = benchmark._1
				  val index = benchmark._2 + 1
				  val name: String = file.getName.substring(0,file.getName.indexOf('.'))
				  print(f"($index%2d)  [$name%18s] ")

				  try {
					  Snippy.synthesize(file.getAbsolutePath) match {
						  case None => println("Timeout")
						  case Some((program: String, time: Int)) => println(f"[${time / 1000.0}%1.3f] $program")
					  }
				  } catch {
					  case e: Throwable => println(e.getMessage)
				  }
			  })
		})
}
