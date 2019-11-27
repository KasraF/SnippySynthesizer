import java.util.Calendar

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.python.util.PythonInterpreter
import org.junit.Assert._

class EvalTests  extends JUnitSuite {

  @Test def startJython(): Unit = {
    val pyInterp = new PythonInterpreter()

    val before = Calendar.getInstance().getTimeInMillis
    for (i <- 0 until 10000) {
      val obj = pyInterp.eval("'Hello Python" + i.toString + " World!'")
      //println(obj)
    }

    val after = Calendar.getInstance().getTimeInMillis
    println("total time: " + (after - before))
    println((after - before).toDouble / 10000)
    pyInterp.cleanup()
    pyInterp.close()
  }


  @Test def multithreadJython(): Unit = {
    val pyInterp = new PythonInterpreter()
    val codes = List("'string'", "18", "False", "2 + 3 + 4","1","True","'abc'","18", "False", "2 + 3 + 4","1","True","'abc'")
    codes.par.foreach { c => pyInterp.eval(c) }
    pyInterp.cleanup()
    pyInterp.close()
  }

  @Test def variables(): Unit = {
    val pyInterp = new PythonInterpreter()
    pyInterp.set("x",2)
    assertEquals(4, pyInterp.eval("x + 2").asInt())
    pyInterp.getLocals.asInstanceOf[org.python.core.PyStringMap].clear()
    assertThrows[org.python.core.PyException](pyInterp.eval("x + 2"))
    pyInterp.cleanup()
    pyInterp.close()
  }
}