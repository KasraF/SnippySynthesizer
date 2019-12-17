import ast.ASTNode
import enumeration.InputsValuesManager

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
//import enumeration.InputsValuesManager
//import execution.Eval
import util.control.Breaks._
import scala.concurrent.duration._


object Main extends App {
  val filename = "src/test/benchmarks/modified_benchmarks/19274448_1.sl"
  //"src/test/benchmarks/modified_benchmarks/convert-numbers-to-text_1.sl"
  // "src/test/benchmarks/modified_benchmarks/change-negative-numbers-to-positive_1.sl"
  //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\univ_2_short.sl"
   //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\from_2018\\bikes_small.sl"//args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(filename).mkString)
  assert(task.isPBE)
  val oeManager = new InputsValuesManager()
  val enumerator = new enumeration.Enumerator(task.vocab,oeManager,task.examples.map(_.input))
  val foundPrograms: mutable.Map[List[Boolean],mutable.ListBuffer[ASTNode]] = mutable.HashMap()
  val deadline = 40.seconds.fromNow
  breakable{ for ((program,i) <- enumerator.zipWithIndex) {
    val results = task.examples.zip(program.values).map(pair => pair._1.output == pair._2)
    //There will only be one program matching 1...1, but portentially many for 1..101..1, do rank those as well?
    if (results.exists(identity)) {
      if (!foundPrograms.contains(results)) foundPrograms.put(results,ListBuffer())
      foundPrograms(results) += program
      if (results.forall(identity)) {
        println(program.code)
        break
      }
    }

    if (i % 1000 == 0) {
      println(i + ": " + program.code)
    }
    if (!deadline.hasTimeLeft)
      break
  }}
  foundPrograms.toList.sortBy(pair => pair._1.count(identity)).foreach{case (specsHold,programs) =>
      println(specsHold)
      programs.foreach(p => println(p.code))
      println
  }
}
