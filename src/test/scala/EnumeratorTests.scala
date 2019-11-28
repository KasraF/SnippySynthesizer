import enumeration.Enumerator
import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._

class EnumeratorTests  extends JUnitSuite{
  @Test def enumerateVocabNoOE: Unit = {
    val vocab = new ast.VocabFactory(
      """Variable|0|input
        #Literal|0|False
        #Literal|0|0
        #Literal|0|1
        #BinOperator|2|+
        #BinOperator|2|<=
        #BinOperator|2|!=
        #FunctionCall|1|str""".stripMargin('#')
    )
    val enumerator = new Enumerator(vocab)
    assertTrue(enumerator.hasNext)
    assertEquals("input",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("False",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("0",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("input + input",enumerator.next().code)
    assertEquals("input + False",enumerator.next().code)
    assertEquals("input + 0",enumerator.next().code)
    assertEquals("input + 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
  }
}
