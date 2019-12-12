import ast.{ASTNode, Types}
import enumeration.{Enumerator, InputsValuesManager, OEValuesManager}
import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}
import org.scalatestplus.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._

import collection.JavaConverters._

class EnumeratorTests  extends JUnitSuite{
  @Test def enumerateVocabNoOE: Unit = {
    val grammar =
      """((ntInt Int (input))
        | (ntBool Bool (false))
        | (ntInt Int (0 1 (+ ntInt ntInt))
        | (ntBool Bool ((<= ntInt ntInt)))
        | (ntString String ((int.to.str ntInt))))
      """.stripMargin
    val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(grammar))))
    val grammarDef = parser.grammarDef()
    val nonTerminals = grammarDef.groupedRuleList().asScala.map{nonTerminal =>
      nonTerminal.Symbol().getSymbol.getText -> Types.withName(nonTerminal.sort().identifier().getText)
    }.toMap
    val vocab = ast.VocabFactory(
      grammarDef.groupedRuleList().asScala.flatMap{nonTerminal => nonTerminal.gTerm().asScala.map(vocabElem =>
        SygusFileTask.makeVocabMaker(vocabElem, Types.withName(nonTerminal.sort().identifier().getText),nonTerminals))}.toList
    )
    assertEquals(4,vocab.leaves.size)
    assertEquals(3,vocab.nonLeaves().size)
    val enumerator = new Enumerator(vocab, new OEValuesManager {
      override def isRepresentative(program: ASTNode): Boolean = true
    },Map("input"->0) :: Nil)
    assertTrue(enumerator.hasNext)
    assertEquals("input",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("false",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("0",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("1",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("(+ input input)",enumerator.next().code)
    assertEquals("(+ input 0)",enumerator.next().code)
    assertEquals("(+ input 1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("(+ 0 input)",enumerator.next().code)
    assertEquals("(+ 0 0)",enumerator.next().code)
    assertEquals("(+ 0 1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("(+ 1 input)",enumerator.next().code)
    assertEquals("(+ 1 0)",enumerator.next().code)
    assertEquals("(+ 1 1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("(<= input input)",enumerator.next().code)
    assertEquals("(<= input 0)",enumerator.next().code)
    assertEquals("(<= input 1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("(<= 0 input)",enumerator.next().code)
    assertEquals("(<= 0 0)",enumerator.next().code)
    assertEquals("(<= 0 1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("(<= 1 input)",enumerator.next().code)
    assertEquals("(<= 1 0)",enumerator.next().code)
    assertEquals("(<= 1 1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)
    assertEquals("(int.to.str input)",enumerator.next().code)
    assertEquals("(int.to.str 0)",enumerator.next().code)
    assertEquals("(int.to.str 1)",enumerator.next().code)
    assertTrue(enumerator.hasNext)

    assertEquals("(+ input (+ input input))", enumerator.next().code)
    assertTrue(enumerator.hasNext)
  }

  @Test def enumerateVocabWithOE: Unit = {
    val grammar =
      """((ntInt Int (x))
        | (ntBool Bool (false))
        | (ntInt Int (0 1 (+ ntInt ntInt)))
        | (ntBool Bool ((<= ntInt ntInt)))
        | (ntString String ((int.to.str ntInt))))""".stripMargin
    val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(grammar))))
    val grammarDef = parser.grammarDef()
    val nonTerminals = grammarDef.groupedRuleList().asScala.map{nonTerminal =>
      nonTerminal.Symbol().getSymbol.getText -> Types.withName(nonTerminal.sort().identifier().getText)
    }.toMap
    val vocab = ast.VocabFactory(
      grammarDef.groupedRuleList().asScala.flatMap{nonTerminal => nonTerminal.gTerm().asScala.map(vocabElem =>
        SygusFileTask.makeVocabMaker(vocabElem, Types.withName(nonTerminal.sort().identifier().getText),nonTerminals))}.toList
    )
    val inputValues: Map[String,AnyRef] = Map("x" -> 1.asInstanceOf[AnyRef])
    val enumerator = new Enumerator(vocab, new InputsValuesManager,inputValues :: Nil)
    assertTrue(enumerator.hasNext)
    assertEquals("x",enumerator.next().code)
    assertEquals("false",enumerator.next().code)
    assertEquals("0",enumerator.next().code)
    assertEquals("(+ x x)",enumerator.next().code)
    assertEquals("(<= x x)",enumerator.next().code)

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
