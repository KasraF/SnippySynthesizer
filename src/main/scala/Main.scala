package sygus

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

	def synthesize(filename: String) =
	{
		val task: SynthesisTask = PythonPBETask.fromString(fromFile(filename).mkString)
		synthesizeFromTask(task)
	}

	def synthesizeFromTask(task: SynthesisTask, timeout: Int = 5) : Seq[RankedProgram] =
	{
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
						println(program.code)
						break
					}
				}

				if ((consoleEnabled && in.ready()) || !deadline.hasTimeLeft) {
					println("None")
					break
				}

				if (i % 1000 == 0) dprintln(s"[$i] (${program.height}) ${program.code}")
			}
		}

		List()
	}

	case class ExpectedEOFException() extends Exception

	// trace.DebugPrints.setDebug()
	synthesize(args.head)
}
