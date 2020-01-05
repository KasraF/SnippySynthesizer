package pcShell

import java.io.{InputStreamReader, PrintWriter}

import jline.console.ConsoleReader

object ConsolePrints {

  var consoleEnabled: Boolean = false

  lazy val reader: ConsoleReader = {
    val r = new ConsoleReader()
    r.setPrompt("> ")
    r.setHistoryEnabled(true)
    r
  }

  lazy val out: PrintWriter = new PrintWriter(reader.getOutput, true)
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
