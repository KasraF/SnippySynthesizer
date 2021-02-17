package edu.ucsd.snippy

import edu.ucsd.snippy.ast.ASTNode
import net.liftweb.json
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonParser

import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.io.StdIn
import scala.tools.nsc.io.JFile
import scala.util.control.Breaks._
import edu.ucsd.snippy.utils.{SingleVariablePredicate, PartialOutputPredicate}

class SynthResult(
	val program: Option[String],
	val done: Boolean,
) {}

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

//			for (program <- task.enumerator) {
//				if (program.height == 4) {
//					rs = Some((program.code, timeout * 1000 - deadline.timeLeft.toMillis.toInt))
//					break
//				}

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

	def synthesizeIO(task: SynthesisTask, timeout: Int): Unit =
	{
		// If the environment is empty, we might go into an infinite loop :/
		if (!task.contexts.exists(_.nonEmpty)) {
			sys.exit(1)
		}

		val stdout = scala.sys.process.stdout
		val stdin = scala.sys.process.stdin

		for (program <- task.enumerator) {
			task.predicate.evaluate(program) match {
				case Some(code) =>
					val done = !task.predicate.isInstanceOf[PartialOutputPredicate]
					stdout.println(json.Serialization.write(new SynthResult(Some(code), done))(json.DefaultFormats))
					stdout.flush()
					if (done) return
				case None => ()
			}

			if (program.height >= 5 || stdin.available() != 0) {
				return
			}
		}
	}

	case class ExpectedEOFException() extends Exception

	args match {
		case Array(taskFile) =>
			val task = SynthesisTask.fromString(fromFile(new JFile(taskFile)).mkString)
			synthesizeIO(task, 7)
		case Array(taskFile, timeout) =>
			val task = SynthesisTask.fromString(fromFile(new JFile(taskFile)).mkString)
			synthesizeIO(task, timeout.toInt)
	}
}
