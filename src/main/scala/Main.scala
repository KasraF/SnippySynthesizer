import ast.ASTNode
import enumeration.InputsValuesManager

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
//import enumeration.InputsValuesManager
//import execution.Eval
import util.control.Breaks._
import scala.concurrent.duration._
import trace.DebugPrints.{dprintln,iprintln}


object Main extends App {
  val filename = //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\name-combine-3-long-repeat.sl"
   //"src/test/benchmarks/too-hard/split-numbers-from-units-of-measure_2.sl"
   //"src/test/benchmarks/modified_benchmarks/bikes_small_1.sl"
    "src/test/benchmarks/syguscomp/split-numbers-from-units-of-measure_1.sl"
  //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\univ_2_short.sl"
   //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\from_2018\\bikes_small.sl"//uargs(0)
  def synthesize(filename: String) = {
     val task = new SygusFileTask(scala.io.Source.fromFile(filename).mkString)
     assert(task.isPBE)
     val oeManager = new InputsValuesManager()
     val enumerator = new enumeration.Enumerator(task.vocab, oeManager, task.examples.map(_.input))
     val foundPrograms: mutable.Map[List[Boolean], mutable.ListBuffer[ASTNode]] = mutable.HashMap()
     val deadline = 40.seconds.fromNow
     breakable {
       for ((program, i) <- enumerator.zipWithIndex) {
         val results = task.examples.zip(program.values).map(pair => pair._1.output == pair._2)
         //There will only be one program matching 1...1, but portentially many for 1..101..1, do rank those as well?
         if (results.exists(identity)) {
           if (!foundPrograms.contains(results)) foundPrograms.put(results, ListBuffer())
           foundPrograms(results) += program
           if (results.forall(identity)) {
             iprintln(program.code)
             break
           }
         }

         if (i % 1000 == 0) {
           dprintln(i + ": " + program.code)
         }
         if (!deadline.hasTimeLeft)
           break
       }
     }

     val ranking: (ASTNode, List[Boolean]) => Double = { (program: ASTNode, sat: List[Boolean]) =>
       val fittingTheData = sat.count(identity).toDouble / sat.length
       val relevancy = task.functionParameters.map(_._1).count(argName => program.includes(argName)).toDouble / task.functionParameters.length
       val height = 1.0 / (program.height + 1)
       val size = 1.0 / program.terms
       3 * fittingTheData + 2 * relevancy + size + height
     }
     val rankedProgs: List[(ASTNode, Double)] = foundPrograms.toList.flatMap { case (sat, progs) => progs.map(p => (p, ranking(p, sat))) }
     rankedProgs.sortBy(-_._2).take(100)
   }

  trace.DebugPrints.setInfo()
  synthesize(filename).foreach(pr => println((pr._1.code,pr._2)))

}
