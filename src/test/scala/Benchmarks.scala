import java.io.File

import sygus.Main

object Benchmarks extends App
{
	val benchmarks = new File("src/test/resources/benchmarks")
	assert(benchmarks.isDirectory)

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
	println("----- -------------------- --------------------------------------")
	benchmarks.listFiles()
	  .map(_.getAbsolutePath)
	  .filter(_.contains(".examples.json"))
	  .zipWithIndex
	  .foreach(benchmark => {
		  val file = benchmark._1
		  val index = benchmark._2 + 1
		  val name: String = file.substring(file.lastIndexOf("/") + 1, file.indexOf("."))
		  print(f"($index%2d)  [$name%18s] ")
		  Main.synthesize(file)
	  })
}
