import ast.{StringReplace, StringVariable}
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite

class TestTest extends JUnitSuite{
  @Test def doTest: Unit = {
    //(str.replace _arg_0 (str.replace _arg_0 _arg_2 _arg_0) _arg_1)
    val inputs = Map("_arg_0" -> "I love apples", "_arg_1" -> "I hate bananas", "_arg_2" -> "banana") ::
      Map("_arg_0" -> "I love apples", "_arg_1" -> "I hate bananas", "_arg_2" -> "apple") :: Nil

    val arg0 = new StringVariable("_arg_0",inputs)
    val arg1 = new StringVariable("_arg_1",inputs)
    val arg2 = new StringVariable("_arg_2",inputs)
    val innerReplace = new StringReplace(arg0,arg2,arg0)
    val outerReplace = new StringReplace(arg0, innerReplace,arg1)
    assertEquals("(str.replace _arg_0 (str.replace _arg_0 _arg_2 _arg_0) _arg_1)",outerReplace.code)
    println(arg0.values)
    println(arg1.values)
    println(arg2.values)
    println(innerReplace.values)
    println(outerReplace.values)
  }

}
