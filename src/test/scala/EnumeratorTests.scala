import ast.ASTNode
import enumeration.{Enumerator, InputsValuesManager, OEValuesManager}
import org.scalatestplus.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._

class EnumeratorTests  extends JUnitSuite{
  @Test def enumerateVocabNoOE: Unit = {
    val vocab = ast.VocabFactory(???
//      """Variable|0|input
//        #Literal|0|False
//        #Literal|0|0
//        #Literal|0|1
//        #BinOperator|2|+
//        #BinOperator|2|<=
//        #FunctionCall|1|str""".stripMargin('#')
    )
    val enumerator = new Enumerator(vocab, new OEValuesManager {
      override def isRepresentative(program: ASTNode): Boolean = true
    },Nil)
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
    val vocab = ast.VocabFactory(???
//      """Variable|0|x
//        #Literal|0|False
//        #Literal|0|0
//        #Literal|0|1
//        #BinOperator|2|+
//        #BinOperator|2|<=
//        #FunctionCall|1|str""".stripMargin('#')
    )
    val inputValues: Map[String,AnyRef] = Map("x" -> 1.asInstanceOf[AnyRef])
    val enumerator = new Enumerator(vocab, new InputsValuesManager,inputValues :: Nil)
    assertTrue(enumerator.hasNext)
    assertEquals("x",enumerator.next().code)
    assertEquals("False",enumerator.next().code)
    assertEquals("0",enumerator.next().code)
    assertEquals("x + x",enumerator.next().code)
    assertEquals("x <= x",enumerator.next().code)

  }

  @Test def enumerateOEWithTwoValues: Unit = {
    val vocab = ast.VocabFactory(???
//      """Variable|0|x
//        #Literal|0|False
//        #Literal|0|0
//        #Literal|0|1
//        #BinOperator|2|+
//        #BinOperator|2|<=
//        #FunctionCall|1|str""".stripMargin('#')
    )
    val inputValues: List[Map[String,AnyRef]] = List(Map("x" -> 1.asInstanceOf[AnyRef]), Map("x" -> 0.asInstanceOf[AnyRef]))
    val enumerator = new Enumerator(vocab, new InputsValuesManager,inputValues)
    assertTrue(enumerator.hasNext)
    assertEquals("x",enumerator.next().code)
    assertEquals("False",enumerator.next().code)
    assertEquals("0",enumerator.next().code)
    assertEquals("1", enumerator.next().code)
    assertEquals("x + x",enumerator.next().code)
    assertEquals("x + 1",enumerator.next().code)
    assertEquals("1 + 1",enumerator.next().code)
    assertEquals("x <= x",enumerator.next().code)
    assertTrue(enumerator.hasNext)
  }

  @Test def runOutOfEnumeration: Unit = {
    val vocab = ast.VocabFactory( ???
//      """Literal|0|0
//        #BinOperator|2|+""".stripMargin('#')
    )
    val enumerator = new Enumerator(vocab, new InputsValuesManager, Nil)
    assertTrue(enumerator.hasNext)
    assertEquals("0", enumerator.next.code)
    assertFalse(enumerator.hasNext)
  }

  @Test def enumerationWithTypes: Unit = {
    val vocab = ast.VocabFactory(???
//      """Literal|0|0|Int
//        #Literal|0|1|Int
//        #Literal|0|False|Bool
//        #Literal|0|True|Bool
//        #BinOperator|2|and|Bool|Bool|Bool
//        #BinOperator|2|<=|Bool|Int|Int
//        #BinOperator|2|==|Bool|Int|Int
//        #BinOperator|2|+|Int|Int|Int""".stripMargin('#')
    )
    val enumerator = new Enumerator(vocab, new OEValuesManager {
      override def isRepresentative(program: ASTNode): Boolean = true
    },Nil)
    assertEquals("0",enumerator.next().code)
    assertEquals("1",enumerator.next().code)
    assertEquals("False",enumerator.next().code)
    assertEquals("True",enumerator.next().code)
    assertEquals("False and False", enumerator.next.code)
    assertEquals("False and True", enumerator.next.code)
    assertEquals("True and False", enumerator.next.code)
    assertEquals("True and True", enumerator.next.code)
    assertEquals("0 <= 0", enumerator.next.code)
    assertEquals("0 <= 1", enumerator.next.code)
    assertEquals("1 <= 0", enumerator.next.code)
    assertEquals("1 <= 1", enumerator.next.code)
    assertEquals("0 == 0", enumerator.next.code)
  }
}
