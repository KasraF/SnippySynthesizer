package pcShell

import java.util

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.atn.{ATNConfigSet, ATNSimulator}
import org.antlr.v4.runtime.dfa.DFA
import sygus._
import trace.DebugPrints.iprintln

object ShellMain extends App {
  val taskFilename = "src/test/benchmarks/too-hard/39060015.sl"//args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(taskFilename).mkString)
  Iterator.continually{
    print("> ")
    Console.in.readLine()
  }.takeWhile(_ != null).foreach { line =>
      if (!line.trim.isEmpty) try {
      val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(line))))
      parser.removeErrorListeners()
      parser.setErrorHandler(new BailErrorStrategy)
      val parsed = parser.bfTerm()
      val visitor = new ASTGenerator(task)
      val ast = visitor.visit(parsed)
      Some(ast, task.examples.map(_.output))
    } catch {
      case e: RecognitionException => {
        println(s"Cannot parse program: ${e.getMessage}")
      }
      case e: ResolutionException => {
        println(s"Cannot resolve program: ${e.msg}")
      }
      case e: ParseCancellationException =>{
        println(s"Cannot parse program: ${e.getCause.asInstanceOf[RecognitionException]}")
      }
    }
  }

}
