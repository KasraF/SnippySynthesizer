package pcShell

import java.io.PrintWriter

import jline.console.ConsoleReader
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime._
import pcShell.Tabulator.ColorString
import sygus._

import scala.util.control.Exception.allCatch
import ConsolePrints._
import sygus.Main.RankedProgram



object ShellMain extends App {
  val taskFilename = args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(taskFilename).mkString)
  var currentResults: List[String] = Nil

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
    out.print(errorColor)
    out.print(" " * (offset) + "^")
    if (exception.getOffendingToken != null && exception.getOffendingToken.getStopIndex - exception.getOffendingToken.getStartIndex > 1)
      out.print("-" * (exception.getOffendingToken.getStopIndex - exception.getOffendingToken.getStartIndex) + "^")
    out.println
    exception match {
      case e: NoViableAltException =>
        out.println("no viable alternative at input " + (if (e.getStartToken == Token.EOF) "<EOF>" else escapeWSAndQuote(e.getOffendingToken.getText)))
      case e: InputMismatchException =>
        out.println( s"mismatched input ${getTokenErrorDisplay(e.getOffendingToken())}, expecting ${e.getExpectedTokens().toString(e.getRecognizer.getVocabulary)}")
      case e: EOFExpectedException =>
        out.println( s"mismatched input ${getTokenErrorDisplay(e.getOffendingToken())}, expecting <EOF>")
      case e: LexerNoViableAltException =>
        out.println("bad token")
    }
    out.print(Console.RESET)
  }

  def escapeIfString(elem: Any): String = if (elem.isInstanceOf[String]) escapeWSAndQuote(elem.asInstanceOf[String]) else elem.toString

  def evalExpr(s: String) = try {
    val lexer = new SyGuSLexer(CharStreams.fromString(s))
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
    cprintln(Tabulator.format(List("input","result","expected") +:
      task.examples.zip(ast.values).map(pair => List(pair._1.input.toList.map(kv =>
        s"${kv._1} -> ${escapeIfString(kv._2.toString)}"
      ).mkString("\n"),
        if (pair._2 == pair._1.output) ColorString(escapeIfString(pair._2), Console.GREEN_B) else ColorString(escapeIfString(pair._2), Console.RED_B),
        escapeIfString(pair._1.output)))))
  } catch {
    case e: RecognitionException => {
      prettyPrintSyntaxError(e)
    }
    case e: ResolutionException => {
      val startIdx = e.badCtx.getStart.getStartIndex
      val len = e.badCtx.getStop.getStopIndex - startIdx + 1
      cprintln(" " * (startIdx + 2) + "^" + (if (len > 1) "-" * (len - 2) + "^" else ""), errorColor)
      cprintln("Cannot resolve program", errorColor)
    }
    case e: ParseCancellationException =>{
      prettyPrintSyntaxError(e.getCause.asInstanceOf[RecognitionException])
    }
  }

//  import jline.TerminalFactory
//  jline.TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, classOf[UnsupportedTerminal])
  consoleEnabled = true
  val hint: String = "Input a program to evaluate or try\n" +
    "  :synt (:s)    -- to synthesize a program\n" +
    "  :quit (:q)    -- to quit\n" +
    "  :1, :2, ...   -- for most recent synthesis results"
  cprintln(s"Welcome to Besto! $hint", infoColor)

  var line: String = null
  while ((line = reader.readLine()) != null) {
    val trimmedLine = line.trim
    if (!trimmedLine.isEmpty) {
      if (line.trim.startsWith(":")) trimmedLine.drop(1) match {
        case "quit" | "q" => sys.exit(0)
        case "synt" | "s" => {
          cprintln("Synthesizing... (Press any key to interrupt)", infoColor)
          val results = Main.synthesizeFromTask(task, 40).take(5)
          if (results.isEmpty) {
            cprintln("No results, try waiting a bit longer", infoColor)
          } else {
            currentResults = results.map(_.program.code).toList
            val fits = results.map{r: RankedProgram => task.fit(r.program)}
            currentResults.zip(fits).zipWithIndex.foreach(
              { case ((p, fit), i) => cprintln(s"$infoColor${i + 1}:${Console.RESET} $p $infoColor${showFit(fit)}${Console.RESET}")}
            )
          }
        }
        case s => allCatch opt s.toInt match {
          case None => cprintln(s"Invalid command. $hint", errorColor)
          case Some(idx) => currentResults.lift(idx - 1) match {
            case None => {
              cprintln(s"There are only ${currentResults.length} results currently available", errorColor)
              reader.getHistory.removeLast()
            }
            case Some(p) => {
              cprintln(p)
              reader.getHistory.removeLast()
              reader.getHistory.add(p)
              evalExpr(p)
            }
          }
        }
      } else evalExpr(trimmedLine)
    }
  }
}
