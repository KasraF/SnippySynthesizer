package edu.ucsd.snippy

import net.liftweb.json
import net.liftweb.json.JObject

import java.io.File
import java.time.{Duration, LocalDateTime}
import java.util.concurrent._
import scala.io.Source.fromFile

object BenchmarksCSV extends App
{
	val executorService: ExecutorService = Executors.newCachedThreadPool()

	def runBenchmark(dir: File, benchTimeout: Int = 7, pnt: Boolean = true): Unit = {
		val suite = if (dir.getParentFile.getName == "resources") "" else dir.getParentFile.getName
		val group = dir.getName
		dir.listFiles()
			.filter(_.getName.contains(".examples.json"))
			.filter(!_.getName.contains(".out"))
			.sorted
			.zipWithIndex
			.foreach(benchmark => {
				val file = benchmark._1
				val name: String = file.getName.substring(0, file.getName.indexOf('.'))
				var time: Int = -1
				var count: Int = 0
				var correct: String = "Timeout"
				var variables = -1

				try {
					val taskStr = fromFile(file).mkString
					val task = json.parse(taskStr).asInstanceOf[JObject].values
					variables = task("varNames").asInstanceOf[List[String]].length

					if (pnt) print(s"$suite,$group,$name,$variables,")

					val start = LocalDateTime.now()
					val callable: Callable[(Option[String], Int, Int)] = () => Snippy.synthesize(taskStr, benchTimeout)
					val promise = this.executorService.submit(callable)
					val rs = try {
						promise.get(benchTimeout + 10, TimeUnit.SECONDS)
					} catch {
						case _: TimeoutException => (None, Duration.between(start, LocalDateTime.now()).toMillis.toInt, -1)
						case _: InterruptedException => (None, Duration.between(start, LocalDateTime.now()).toMillis.toInt, -1)
						case e: ExecutionException => throw e.getCause
					} finally {
						promise.cancel(true)
					}

					rs match {
						case (Some(program: String), tim: Int, coun: Int) =>
							time = tim
							count = coun

							correct = task.get("solutions") match {
								case Some(solutions) if solutions.asInstanceOf[List[String]].contains(program) => "+"
								case Some(_) => "-"
								case None => "?"
							}
						case (None, _, coun: Int) =>
							count = coun
					}
				} catch {
					case e: AssertionError => throw e
					case _: java.lang.OutOfMemoryError => correct = "OutOfMemory"
					case _: Throwable => correct = "Error" //sys.process.stderr.println(e)
				}

				if (pnt) println(s"$time,$count,$correct")
				Runtime.getRuntime.gc()
			})
	}

	val benchmarksDir = new File("src/test/resources")
	assert(benchmarksDir.isDirectory)

	DebugPrints.debug = false
	DebugPrints.info = false

	var filterArgs = args
	val timeout: Int = args.lastOption.map(_.toIntOption) match {
		case Some(Some(t)) => {
			filterArgs = args.dropRight(1)
			t
		}
		case _ => 5 * 60
	}

	println("suite,group,name,variables,time,count,correct")
	val benchmarks = if (filterArgs.nonEmpty) {
		benchmarksDir.listFiles()
			.flatMap(f => if (f.isDirectory) f :: f.listFiles().toList else Nil)
			.filter(_.isDirectory)
			.filter(dir => if (dir.getParentFile.getName == "resources") filterArgs.contains(dir.getName)
					       else filterArgs.contains(dir.getName) || filterArgs.contains(dir.getParentFile.getName))
			.toList
	} else {
		benchmarksDir.listFiles()
			.flatMap(f => if (f.isDirectory) f :: f.listFiles().toList else Nil)
			.filter(_.isDirectory)
			.sortBy(_.getName)(Ordering.String)
			.toList
	}

	// First, warm up
	benchmarks.foreach(this.runBenchmark(_, 30, pnt = false))

	// Then actually run
	benchmarks.foreach(this.runBenchmark(_, timeout))
}
