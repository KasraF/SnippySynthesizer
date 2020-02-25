package trace

object DebugPrints {
  var debug = false
  var info = false
  def setDebug() = {
    debug = true
    info = true
  }
  def setInfo() = {
    debug = false
    info = true
  }
  def setNone() = {
    debug = false
    info = false
  }
  def dprintln(str: => Any) = if (debug) println(str)
  def iprintln(str: => Any) = if (info) println(str)
  def eprintln(str: => Any) = {
    val stackTrace = Thread.currentThread().getStackTrace()(2)
    val clazz = stackTrace.getClassName
    val method = stackTrace.getMethodName
    val line = stackTrace.getLineNumber
    System.err.println(s"$clazz.$method[$line] $str")
  }
}
