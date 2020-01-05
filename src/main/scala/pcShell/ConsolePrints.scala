package pcShell

import java.io.{FileOutputStream, InputStreamReader, OutputStream, PrintWriter}

import jline.console.ConsoleReader
import org.apache.commons.io.output.{NullOutputStream, TeeOutputStream, WriterOutputStream}

object ConsolePrints {

  var consoleEnabled: Boolean = false
  var outFile: Option[java.io.File] = None

  lazy val reader: ConsoleReader = {
    val r = new ConsoleReader()
    r.setPrompt("> ")
    r.setHistoryEnabled(true)
    r
  }

  lazy val out: PrintWriter =  outFile.map{f =>
    val pw = new PrintWriter( new TeeOutputStream(new WriterOutputStream( reader.getOutput), new FileOutputStream(f)), true)
    pw.println(s"Otuput file: ${f.getAbsolutePath}")
    pw
  }.getOrElse(
    new PrintWriter(reader.getOutput,true))

  lazy val in: InputStreamReader = new InputStreamReader(System.in)

  val errorColor: String = Console.RED
  val infoColor: String = Console.YELLOW

  def cprint(str: String, color: String = "") = if (consoleEnabled) {
    val coloredString = if (color.isEmpty) str else s"${color}$str${Console.RESET}"
    out.print(coloredString)
    out.flush()
  }

  def cprintln(str: String, color: String = "") = if (consoleEnabled) {
    val coloredString = if (color.isEmpty) str else s"${color}$str${Console.RESET}"
    out.println(coloredString)
  }

  def showFit(fit: (Int, Int)): String = s"[${fit._1}/${fit._2}]"

}
