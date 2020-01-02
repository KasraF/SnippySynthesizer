package pcShell

import java.util

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.atn.{ATNConfigSet, ATNSimulator}
import org.antlr.v4.runtime.dfa.DFA
import sygus._
import trace.DebugPrints.iprintln

object ShellMain extends App {
  val taskFilename = "src/test/benchmarks/syguscomp/join-first-and-last-name.sl"//args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(taskFilename).mkString)

  def escapeWSAndQuote(s: String) = { //		if ( s==null ) return s;
    "\"" + s.replace("\n", "\\n")
     .replace("\r", "\\r")
     .replace("\t", "\\t") + "\""
  }

  def getTokenErrorDisplay(t: Token): String = {
    if (t == null) return "<no token>"
    var s = t.getText
    if (s == null) if (t.getType == Token.EOF) s = "<EOF>"
    else s = "<" + t.getType + ">"
    escapeWSAndQuote(s)
  }
  class EOFExpectedException(recognizer: Parser) extends RecognitionException(recognizer,recognizer.getInputStream(), recognizer.getContext) {
    this.setOffendingToken(recognizer.getCurrentToken)
  }

  def prettyPrintSyntaxError(exception: RecognitionException) = {
    val offset = if (exception.isInstanceOf[LexerNoViableAltException]) exception.asInstanceOf[LexerNoViableAltException].getStartIndex + 2 else exception.getOffendingToken.getStartIndex + 1
    print(" " * (offset) + "^")
    if (exception.getOffendingToken != null && exception.getOffendingToken.getStopIndex - exception.getOffendingToken.getStartIndex > 1)
      print("-" * (exception.getOffendingToken.getStopIndex - exception.getOffendingToken.getStartIndex) + "^")
    println
    exception match {
      case e: NoViableAltException =>
        println("no viable alternative at input " + (if (e.getStartToken == Token.EOF) "<EOF>" else escapeWSAndQuote(e.getOffendingToken.getText)))
      case e: InputMismatchException =>
        println( s"mismatched input ${getTokenErrorDisplay(e.getOffendingToken())}, expecting ${e.getExpectedTokens().toString(e.getRecognizer.getVocabulary)}")
      case e: EOFExpectedException =>
        println( s"mismatched input ${getTokenErrorDisplay(e.getOffendingToken())}, expecting <EOF>")
      case e: LexerNoViableAltException =>
        println("bad token")
    }
  }

  Iterator.continually{
    print("> ")
    Console.in.readLine()
  }.takeWhile(_ != null).foreach { line =>
      if (!line.trim.isEmpty) try {
      val lexer = new SyGuSLexer(CharStreams.fromString(line))
      lexer.removeErrorListeners()
      lexer.addErrorListener(new ThrowingLexerErrorListener)
      val parser = new SyGuSParser(new BufferedTokenStream(lexer))
      parser.removeErrorListeners()
      parser.setErrorHandler(new BailErrorStrategy)
      val parsed = parser.bfTerm()
      if (parser.getCurrentToken.getType != Token.EOF)
        throw new EOFExpectedException(parser)
      val visitor = new ASTGenerator(task)
      val ast = visitor.visit(parsed)
      println(Tabulator.format(List("input","result","expected") +:
        task.examples.zip(ast.values).map(pair => List(pair._1.input.toList.map(kv =>
          s"${kv._1} -> ${if (kv._2.isInstanceOf[String]) escapeWSAndQuote(kv._2.toString) else kv._2}"
        ).mkString("\n"), pair._2, pair._1.output))))
    } catch {
      case e: RecognitionException => {
        prettyPrintSyntaxError(e)
      }
      case e: ResolutionException => {
        val startIdx = e.badCtx.getStart.getStartIndex
        val len = e.badCtx.getStop.getStopIndex - startIdx + 1
        println(" " * (startIdx + 2) + "^" + (if (len > 1) "-" * (len - 2) + "^" else ""))
        println("Cannot resolve program")
      }
      case e: ParseCancellationException =>{
        prettyPrintSyntaxError(e.getCause.asInstanceOf[RecognitionException])
      }
    }
  }

}
