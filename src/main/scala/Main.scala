import ast.ASTNode
import enumeration.InputsValuesManager
import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams, RecognitionException, Token}
import sygus._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
//import enumeration.InputsValuesManager
//import execution.Eval
import util.control.Breaks._
import scala.concurrent.duration._
import trace.DebugPrints.{dprintln,iprintln}


object Main extends App {
  val filename = //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\univ_3_short.sl"
  //"src/test/benchmarks/too-hard/split-numbers-from-units-of-measure_2.sl"
  //"src/test/benchmarks/modified_benchmarks/name-combine_1.sl"
   "src/test/benchmarks/syguscomp/38871714.sl"
  //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\univ_2_short.sl"
   //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\stackoverflow4.sl"//args(0)

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

  case class ExpectedEOFException() extends Exception
  def interpret(task: SygusFileTask, str: String): ASTNode = {
    val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(str))))
    val parsed = parser.bfTerm()
    val visitor = new ASTGenerator(task)
    val ast = visitor.visit(parsed)
    if (parser.getCurrentToken.getType != Token.EOF) {
      throw ExpectedEOFException()
    }
    ast
  }
  def interpret(filename: String, str: String): Option[(ASTNode, List[Any])] = try {
    val task = new SygusFileTask(scala.io.Source.fromFile(filename).mkString)
    val ast = interpret(task,str)
    Some(ast, task.examples.map(_.output))
  } catch {
    case e: RecognitionException => {
      iprintln(s"Cannot parse program: ${e.getMessage}")
      None
    }
    case e: ResolutionException => {
      iprintln(s"Cannot resolve program: ${e.badCtx.getText}")
      None
    }
    case e: ExpectedEOFException => {
      iprintln("Expected <EOF>")
      None
    }
  }

  trace.DebugPrints.setDebug()
//  val (prog, _) = interpret(filename, "(str.++ firstname lastname)").get
//  println(prog.code)
//  println(prog.values)
  synthesize(filename).foreach(pr => println((pr._1.code,pr._2)))
}
