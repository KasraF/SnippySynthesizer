package pcShell

import java.io.File

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime._
import pcShell.Tabulator.ColorString
import sygus._

import scala.util.control.Exception.allCatch
import ConsolePrints._
import jline.console.completer.{Completer, StringsCompleter}
import sygus.Main.RankedProgram



object ShellMain extends App {
  val taskFilename = args(0)
  if (args.length > 1 && args(1) == "--out")
    outFile = Some(File.createTempFile("bester_", ".log",new java.io.File(".")))
  val task: SynthesisTask = PythonPBETask.fromString(scala.io.Source.fromFile(taskFilename).mkString)
  var currentResults: scala.collection.immutable.List[String] = Nil

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

  class SygusCompleter(task: SynthesisTask) extends Completer {
    lazy val tokens: List[String] = task.vocab.leavesMakers.map(_.head) ++ task.vocab.nodeMakers.map(_.head)

    override def complete(buffer: String, cursor: Int, candidates: java.util.List[CharSequence]): Int = { // buffer could be null
      if (buffer == null) tokens.foreach(t => candidates.add(t))
      else {
        val (preCursor, postCursor) = buffer.splitAt(cursor)
        val (pref, suf) = preCursor.splitAt(preCursor.lastIndexWhere(c => c.isWhitespace || c == '(') + 1)
        val results = tokens.filter(_.startsWith(suf))
        results.foreach(t => candidates.add(pref + t + postCursor))
      }
      if (candidates.isEmpty) -1
      else 0
    }
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

  def escapeIfString(elem: Any): String = elem match {
    case str: String => escapeWSAndQuote(str)
    case _ => elem.toString
  }

//  import jline.TerminalFactory
//  jline.TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, classOf[UnsupportedTerminal])
  consoleEnabled = true
  reader.addCompleter(new SygusCompleter(task))

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
              // evalExpr(p)
            }
          }
        }
      } // else evalExpr(trimmedLine)
    }
  }
}
