package edu.ucsd.snippy

import edu.ucsd.snippy.ast.ASTNode
import net.liftweb.json
import net.liftweb.json.JObject

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

	case class ExpectedEOFException() extends Exception

	// trace.DebugPrints.setDebug()
	val (file, timeout) = args match {
		case Array(task) => (new JFile(task), 7)
		case Array(task, timeout) => (new JFile(task), timeout.toInt)
	}

	val (program, time, count) = synthesize(file, timeout) match {
		case (None, time, count) => ("None", time, count)
		case (Some(program), time, count) => (program, time, count)
	}

	val writer = new BufferedWriter(new FileWriter(args.head + ".out"))
	writer.write(program)
	writer.close()

	// Check if the task has a solution!
	val task = json.parse(fromFile(file).mkString).asInstanceOf[JObject].values

	if (task.contains("solutions") && task("solutions").asInstanceOf[List[String]].nonEmpty) {
		val solutions = task("solutions").asInstanceOf[List[String]]
		if (solutions.contains(program)) {
			println(f"[+] [$count%d] [${time / 1000.0}%1.3f]\n$program")
		} else {
			println(f"[-] [$count%d] [${time / 1000.0}%1.3f]\n$program\n---\n${solutions.head}")
		}
	} else {
		println(f"[?] [$count%d] [${time / 1000.0}%1.3f]\n$program")
	}
}
