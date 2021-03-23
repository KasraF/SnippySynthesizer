import edu.ucsd.snippy.{DebugPrints, Snippy}
import net.liftweb.json
import net.liftweb.json.JObject
import org.fusesource.jansi.Ansi

import java.io.File
import scala.io.Source.fromFile

object BenchmarksVerbose extends App
{
	@inline def repeat(s: String, count: Int): String = if (count > 1) s.repeat(count) else s

	def runBenchmark(dir: File, timeout: Int = 7): Unit = {
		dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
			.sorted
			.foreach(file => {
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				val buffer = new Ansi()

				try {
					// First, get all the data
					val taskStr = fromFile(file).mkString
					val task = json.parse(taskStr).asInstanceOf[JObject].values
					val (program, time, count) = Snippy.synthesize(taskStr, timeout) match {
						case (None, time: Int, count: Int) => ("Timeout", time, count)
						case (Some(program: String), time: Int, count: Int) => (program, time, count)
					}
					val lines = program.split('\n').map(_.replaceAll("\t", "    "))
					val lineLength = lines.map(_.length).max

					// Now pretty-print!
					var metaLine = f"+-[$name]-[${time / 1000.0}%.3f]-[$count]-"

					if (task.contains("solutions") && task("solutions").asInstanceOf[List[String]].nonEmpty) {
						val solutions = task("solutions").asInstanceOf[List[String]]
						if (solutions.contains(program)) {
							// Correct! Just print the program in green
							metaLine += repeat("-", lineLength - metaLine.length + 2)
							buffer.fgDefault()
								.a(metaLine)
								.newline()
							lines.foreach(line => buffer.a("| ").fgGreen().a(line).fgDefault().newline())
							buffer.fgDefault()
							buffer.a("+").a(repeat("-", lineLength + 2))
						} else {
							// Incorrect! Print the program in red and a correct solution next to it
							val solutionLines = solutions.head.split('\n').map(_.replaceAll("\t", "    "))
							val solLineLength = solutionLines.map(_.length).max

							buffer.fgDefault()
								.a(metaLine)
								.a(repeat("-", lineLength - metaLine.length + 3))
								.a("+-[solution]-")
								.a(repeat("-", solLineLength - 10))
								.newline()
							lines
								.map(line => line + " ".repeat(lineLength - line.length))
								.zipAll(solutionLines, " ".repeat(lineLength), "")
								.foreach {
									case (line, sol) =>
										buffer.a("| ")
										if (solutions.exists(_.contains(line))) {
											buffer.fgGreen()
										} else {
											buffer.fgRed()
										}
										buffer.a(line)
											.fgDefault()
											.a(repeat(" ", metaLine.length - lineLength - 1))
											.a("| ")
											.fgBlue()
											.a(sol)
											.fgDefault()
											.newline()
								}
							buffer.fgDefault()
							buffer.a("+").a(repeat("-", Math.max(lineLength + 2, metaLine.length))).a('+').a(repeat("-", solLineLength + 2))
						}
					} else {
						// Unknown. Just print the program in default
						metaLine += repeat("-", lineLength - metaLine.length + 2)
						buffer.fgDefault()
							.a(metaLine)
							.newline()
						lines.foreach(line => buffer.a("| ").a(line).newline())
						buffer.a("+").a(repeat("-", lineLength + 1))
					}
				} catch {
					case e: AssertionError => throw e
					case e: Throwable => buffer.a(e)
				}

				println(buffer.toString)
				Runtime.getRuntime.gc()
			})
	}

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
