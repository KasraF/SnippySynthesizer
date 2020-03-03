package sygus

import java.io.{BufferedWriter, FileWriter}

import ast.ASTNode
import enumeration.InputsValuesManager
import pcShell.ConsolePrints._
import trace.DebugPrints.dprintln

import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.util.control.Breaks._

object Main extends App
{
	case class RankedProgram(program: ASTNode, rank: Double) extends Ordered[RankedProgram]
	{
		override def compare(that: RankedProgram): Int = this.rank.compare(that.rank)
	}

	def synthesize(filename: String) : Option[(ASTNode, Int)] =
	{
		val task: SynthesisTask = PythonPBETask.fromString(fromFile(filename).mkString)
		synthesizeFromTask(task)
	}

	def synthesizeFromTask(task: SynthesisTask, timeout: Int = 5) : Option[(ASTNode, Int)] =
	{
		var rs: Option[(ASTNode, Int)] = None
		val oeManager = new InputsValuesManager()
		val enumerator = new enumeration.Enumerator(
			task.vocab,
			oeManager,
			task.examples.map(_.input))
		val deadline = timeout.seconds.fromNow

		breakable {
			for ((program, i) <- enumerator.zipWithIndex) {
				if (program.nodeType == task.returnType) {
					val results = task.examples
					                  .zip(program.values)
					                  .map(pair => pair._1.output == pair._2)
					if (results.forall(identity)) {
						rs = Some(
							(PostProcessor.clean(program),
							  timeout * 1000 - deadline.timeLeft.toMillis.toInt))
						break
					}
				}

				if ((consoleEnabled && in.ready()) || !deadline.hasTimeLeft) break
				dprintln(s"[$i] (${program.height}) ${program.code}")
			}
		}

		rs
	}

	case class ExpectedEOFException() extends Exception

	// trace.DebugPrints.setDebug()
	val (program, time) = synthesize(args.head) match {
		case None => ("None", -1)
		case Some((program: ASTNode, time: Int)) => (program.code, time)
	}

	val writer = new BufferedWriter(new FileWriter(args.head + ".out"))
	writer.write(program);
	println(f"[${time / 1000.0}%1.3f] $program");
	writer.close()
}
