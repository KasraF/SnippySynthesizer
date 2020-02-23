package sygus

import ast.ASTNode
import enumeration.{InputsValuesManager, ProgramRanking}
import pcShell.ConsolePrints._
import trace.DebugPrints.{dprintln, iprintln}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.io.Source.fromFile
import scala.util.control.Breaks._

object Main extends App
{

	case class RankedProgram(program: ASTNode, rank: Double) extends Ordered[RankedProgram]
	{
		override def compare(that: RankedProgram): Int =
			this.rank.compare(that.rank)
	}

	def synthesize(filename: String) =
	{
		val task: SynthesisTask = PythonPBETask.fromString(fromFile(filename).mkString)
		synthesizeFromTask(task)
	}

	def synthesizeFromTask(task: SynthesisTask, timeout: Int = 150000) =
	{
		val oeManager = new InputsValuesManager()
		val enumerator = new enumeration.Enumerator(
			task.vocab,
			oeManager,
			task.examples.map(_.input))
		val deadline = timeout.seconds.fromNow
		val ranks = mutable.ListBuffer[RankedProgram]()
		val t0 = System.nanoTime()

		breakable {
			for ((program, i) <- enumerator.zipWithIndex) {
				if (program.nodeType == task.returnType) {
					val results = task.examples
					                  .zip(program.values)
					                  .map(pair => pair._1.output == pair._2)
					//There will only be one program matching 1...1, but portentially many for 1..101..1, do rank those as well?
					if (results.exists(identity)) {
						//           if (!foundPrograms.contains(results)) foundPrograms.put(results, ListBuffer())
						//           foundPrograms(results) += program
						val rank = ProgramRanking.ranking(
							program,
							task.examples.map(_.output),
							task.parameters.map(_._1))
						val ranked = RankedProgram(program, rank)
						val ip = ranks.search(ranked)
						if (ip.insertionPoint > 0 || ranks.length < 50) {
							ranks.insert(ip.insertionPoint, ranked)
						}
						if (ranks.length > 50) ranks.remove(0)
						if (results.forall(identity)) {
							iprintln(program.code)
							cprintln(s"\rCurrent best: ${
								ranks
									.takeRight(1)
									.map { r =>
										showFit(task.fit(r.program))
									}
									.mkString("")
							}", infoColor)
							break
						}
					}
				}

				if (i % 1000 == 0) {
					dprintln(i + ": " + program.code)
					cprint(s"\rCurrent best: ${
						ranks
							.takeRight(1)
							.map { r =>
								showFit(task.fit(r.program))
							}
							.mkString("")
					}", infoColor)
				}
				if ((consoleEnabled && in.ready()) || !deadline.hasTimeLeft) {
					cprintln("")
					break
				}
			}
		}
		val t1 = System.nanoTime()
		iprintln(s"${t1 - t0}ns")
		iprintln(ranks.length)
		iprintln(ranks)
		//val rankedProgs: List[(ASTNode, Double)] = foundPrograms.toList.flatMap { case (sat, progs) => progs.map(p => (p, ProgramRanking.ranking(p, task.examples.map(_.output), task.functionParameters.map(_._1)))) }
		ranks.reverse
		//rankedProgs.sortBy(-_._2).take(50).map(p => RankedProgram(p._1,p._2))
	}

	case class ExpectedEOFException() extends Exception

	// trace.DebugPrints.setDebug()
	synthesize(args.head).foreach(pr => println((pr.program.code, pr.rank)))
}
