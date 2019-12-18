import ast.ASTNode
import enumeration.InputsValuesManager

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
//import enumeration.InputsValuesManager
//import execution.Eval
import util.control.Breaks._
import scala.concurrent.duration._


object Main extends App {
  val filename = //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\name-combine-3-long-repeat.sl"
   //"src/test/benchmarks/too-hard/split-numbers-from-units-of-measure_2.sl"
   //"src/test/benchmarks/modified_benchmarks/bikes_small_1.sl"
    "src/test/benchmarks/syguscomp/extract-text-between-parentheses.sl"
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
//  foundPrograms.toList.sortBy(pair => pair._1.count(identity)).foreach{case (specsHold,programs) =>
//      println(specsHold)
//      programs.foreach(p => println(p.code))
//      println
//  }

  val ranking: (ASTNode,List[Boolean]) => Double = {(program: ASTNode, sat: List[Boolean]) =>
    val fittingTheData = sat.count(identity).toDouble / sat.length
    val relevancy = task.functionParameters.map(_._1).count(argName => program.includes(argName)).toDouble / task.functionParameters.length
    val height = 1.0 / program.height
    val size = 1.0 / program.terms
    2 * fittingTheData + relevancy + size + height
  }
  val rankedProgs: List[(ASTNode,Double)] = foundPrograms.toList.flatMap{case (sat,progs) => progs.map(p => (p,ranking(p,sat)))}
  rankedProgs.sortBy(_._2).takeRight(100).foreach{rankedProgram =>
    println(List(rankedProgram._1.code,rankedProgram._2,rankedProgram._1.values,rankedProgram._1.height, rankedProgram._1.terms).mkString(","))
  }

}
