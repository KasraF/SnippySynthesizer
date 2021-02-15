package edu.ucsd.snippy

import edu.ucsd.snippy.ast.ASTNode

import java.io.{BufferedWriter, FileWriter}
import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.tools.nsc.io.JFile
import scala.util.control.Breaks._

object Snippy extends App
{
	case class RankedProgram(program: ASTNode, rank: Double) extends Ordered[RankedProgram]
	{
		override def compare(that: RankedProgram): Int = this.rank.compare(that.rank)
	}

	def synthesize(file: JFile, timeout: Int = 7) : (Option[String], Int, Int) =
	{
		synthesize(fromFile(file).mkString, timeout)
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

		var solution: Option[String] = None
		var rs: (Option[String], Int, Int) = (None, -1, 0)
		val deadline = timeout.seconds.fromNow

		breakable {
			for ((program, i) <- task.enumerator.zipWithIndex) {
				// Update the programs
				task.predicate.evaluate(program) match {
					case Some(code) =>
						solution = Some(code)
						rs = (Some(code), timeout * 1000 - deadline.timeLeft.toMillis.toInt, i)
						break
					case None => ()
				}

				if (!deadline.hasTimeLeft) {
					rs = (solution, timeout * 1000 - deadline.timeLeft.toMillis.toInt, i)
					break
				}
			}
		}

		rs
	}

	case class ExpectedEOFException() extends Exception

	// trace.DebugPrints.setDebug()
	val rs = args match {
		case Array(task) => synthesize(new JFile(task))
		case Array(task, timeout) => synthesize(new JFile(task), timeout.toInt)
	}

	val (program, time, count) = rs match {
		case (None, time, count) => ("None", time, count)
		case (Some(program), time, count) => (program, time, count)
	}

	val writer = new BufferedWriter(new FileWriter(args.head + ".out"))
	writer.write(program)
	println(f"[$count%d] [${time / 1000.0}%1.3f] $program")
	writer.close()

}
