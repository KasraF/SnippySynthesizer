package edu.ucsd.snippy

import edu.ucsd.snippy.ast.ASTNode

import java.io.{BufferedWriter, FileWriter}
import scala.collection.mutable
import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.util.control.Breaks._

object Snippy extends App
{
	case class RankedProgram(program: ASTNode, rank: Double) extends Ordered[RankedProgram]
	{
		override def compare(that: RankedProgram): Int = this.rank.compare(that.rank)
	}

	def synthesize(filename: String) : Option[(String, Int)] =
	{
		val task: SynthesisTask = SynthesisTask.fromString(fromFile(filename).mkString)
		synthesizeFromTask(task)
	}

	def synthesizeFromTask(task: SynthesisTask, timeout: Int = 7) : Option[(String, Int)] =
	{
		// If the environment is empty, we might go into an infinite loop :/
		if (!task.contexts.exists(_.nonEmpty)) {
			return Some("None", 0)
		}

		val programs: collection.mutable.Map[String, Option[ASTNode]] = new mutable.HashMap[String, Option[ASTNode]]().addAll(task.predicates.keys.map(name => name -> None))
		var rs: Option[(String, Int)] = None
		val deadline = timeout.seconds.fromNow

		breakable {
			// for ((program, i) <- task.enumerator.zipWithIndex) {
			for (program <- task.enumerator) {
				if (!deadline.hasTimeLeft) {
					rs = Some(("None", timeout * 1000 - deadline.timeLeft.toMillis.toInt))
					break
				}

//				if (program.height == 4) {
//					rs = Some((program.code, timeout * 1000 - deadline.timeLeft.toMillis.toInt))
//					break
//				}

				// Update the programs
				task.predicates
					.filter(entry => programs(entry._1).isEmpty)
					.filter(entry => entry._2.evaluate(program, task))
					.foreach(entry => programs(entry._1) = Some(program))

				if (programs.forall(_._2.isDefined)) {
					// We are done!
					val programList = programs.map(entry => entry._1 -> entry._2.head).toList
					val lhs = programList.map(_._1).mkString(", ")
					val rhs = programList.map(_._2).map(PostProcessor.clean).map(_.code).mkString(", ")
					rs = Some(lhs + " = " + rhs, timeout * 1000 - deadline.timeLeft.toMillis.toInt)
					break
				}
			}
		}

		rs
	}

	case class ExpectedEOFException() extends Exception

	// trace.DebugPrints.setDebug()
	val (program, time) = synthesize(args.head) match {
		case None => ("None", -1)
		case Some((program: String, time: Int)) => (program, time)
	}

	val writer = new BufferedWriter(new FileWriter(args.head + ".out"))
	writer.write(program)
	println(f"[${time / 1000.0}%1.3f] $program")
	writer.close()
}
