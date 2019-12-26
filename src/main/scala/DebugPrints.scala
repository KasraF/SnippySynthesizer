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
}
