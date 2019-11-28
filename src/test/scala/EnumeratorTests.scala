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
    assertEquals("False + input",enumerator.next().code)
    assertEquals("False + False",enumerator.next().code)
    assertEquals("False + 0",enumerator.next().code)
    assertEquals("False + 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("0 + input",enumerator.next().code)
    assertEquals("0 + False",enumerator.next().code)
    assertEquals("0 + 0",enumerator.next().code)
    assertEquals("0 + 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("1 + input",enumerator.next().code)
    assertEquals("1 + False",enumerator.next().code)
    assertEquals("1 + 0",enumerator.next().code)
    assertEquals("1 + 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("input <= input",enumerator.next().code)
    assertEquals("input <= False",enumerator.next().code)
    assertEquals("input <= 0",enumerator.next().code)
    assertEquals("input <= 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("False <= input",enumerator.next().code)
    assertEquals("False <= False",enumerator.next().code)
    assertEquals("False <= 0",enumerator.next().code)
    assertEquals("False <= 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("0 <= input",enumerator.next().code)
    assertEquals("0 <= False",enumerator.next().code)
    assertEquals("0 <= 0",enumerator.next().code)
    assertEquals("0 <= 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("1 <= input",enumerator.next().code)
    assertEquals("1 <= False",enumerator.next().code)
    assertEquals("1 <= 0",enumerator.next().code)
    assertEquals("1 <= 1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("str(input)",enumerator.next().code)
    assertEquals("str(False)",enumerator.next().code)
    assertEquals("str(0)",enumerator.next().code)
    assertEquals("str(1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)

    assertEquals("input + (input + input)", enumerator.next().code)
    assertTrue(enumerator.hasNext)
  }

  @Test def enumerateVocabWithOE: Unit = {
    
  }
}
