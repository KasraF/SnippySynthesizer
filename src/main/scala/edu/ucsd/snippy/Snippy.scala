package edu.ucsd.snippy

import edu.ucsd.snippy.ast.ASTNode
import net.liftweb.json
import net.liftweb.json.Formats

import java.io.File
import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.io.StdIn
import scala.sys.process.stderr
import scala.tools.nsc.io.JFile
import scala.util.control.Breaks._

class SynthResult(
	val id: Int,
	val success: Boolean,
	val program: Option[String])

object Snippy extends App
{
	case class RankedProgram(program: ASTNode, rank: Double) extends Ordered[RankedProgram]
	{
		override def compare(that: RankedProgram): Int = this.rank.compare(that.rank)
	}

	def synthesize(file: JFile, timeout: Int = 7) : (Option[String], Int, Int) =
	{
		val buff = fromFile(file)
		val rs = synthesize(buff.mkString, timeout)
		buff.close()
		rs
	}

	def synthesize(taskStr: String, timeout: Int): (Option[String], Int, Int) =
	{
		synthesize(SynthesisTask.fromString(taskStr), timeout)
	}

	def synthesize(task: SynthesisTask, timeout: Int) : (Option[String], Int, Int) =
	{
		// If the environment is empty, we might go into an infinite loop :/
		if (!task.contexts.exists(_.nonEmpty)) {
			return (Some("None"), 0, 0)
		}

		var rs: (Option[String], Int, Int) = (None, -1, 0)
		val deadline = timeout.seconds.fromNow

		breakable {
			for ((solution, i) <- task.enumerator.zipWithIndex) {
				solution match {
					case Some(assignment) =>
						rs = (Some(assignment.code), timeout * 1000 - deadline.timeLeft.toMillis.toInt, i)
						break
					case _ => ()
				}

				if (!deadline.hasTimeLeft) {
					rs = (None, timeout * 1000 - deadline.timeLeft.toMillis.toInt, i)
					break
				}
			}
		}

		rs
	}

	def synthesizeIO(timeout: Int = 7): Unit = {
		val stdout = scala.sys.process.stdout
		val stdin = scala.sys.process.stdin
		// TODO What is this?
		implicit val formats: Formats = json.DefaultFormats

		val taskStr = StdIn.readLine()
		val task = SynthesisTask.fromString(taskStr)
		var code: Option[String] = None

		if (task.contexts.exists(_.nonEmpty)) {
			val deadline = timeout.seconds.fromNow

			breakable {
				for (solution <- task.enumerator) {
					solution match {
						case Some(assignment) =>
							code = Some(assignment.code())
							break
						case _ => ()
					}

					if (!deadline.hasTimeLeft || stdin.available() != 0) {
						break
					}
				}
			}
		}

		val solution = new SynthResult(0, code.isDefined, code)
		stdout.println(json.Serialization.write(solution)(json.DefaultFormats))
		stdout.flush()
	}

	case class ExpectedEOFException() extends Exception

	DebugPrints.setNone()

//		// TODO Move the benchmarks to resources and read them off the jar to warm up the JVM.
//		val file = new File(args.head)
//		if (file.exists() && file.isDirectory) {
//			file.listFiles()
//				.filter(_.isDirectory)
//				.flatMap(_.listFiles())
//				.filter(_.getName.contains(".examples.json"))
//				.filter(!_.getName.contains(".out"))
//				.foreach(bench => synthesize(bench, 1))
//		}

	if (args.isEmpty) {
		// Listen on stdin for synthesis tasks
		while (true) try synthesizeIO() catch {
			case e: Throwable =>
				println("ERROR")
				stderr.println(e.toString)
		}
	} else {
		val solution = args.toList match {
			case file :: timeout :: _ => synthesize(new JFile(file), timeout.toInt)
			case file :: _ => synthesize(new JFile(file))
		}

		solution match {
			case (Some(solution), time, count) => println(s"[$time] [$count] $solution")
			case (None, time, count) =>  println(s"[$time] [$count] None")
		}
	}
}
