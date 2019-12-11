import ast._
import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite

class VocabTests  extends JUnitSuite{
  def readVocabElem(elemLine: String) = {
    val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(elemLine))))
    val ruleList = parser.groupedRuleList()
    assert(ruleList.gTerm().size() == 1)
    val res = ruleList.gTerm(0)
    assert(res != null)
    (res,ruleList.sort().identifier().getText)
  }
  val nonTerminals = Map("ntBool" -> Types.Bool,"ntInt" -> Types.Int, "ntString" -> Types.String)

  @Test def boolLiteralMaker(): Unit =  {
    val vocabLine = "(ntBool Bool (false))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    val node = maker(Nil,Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[BoolLiteral])
    assertEquals(List(false), node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def intLiteralMaker(): Unit =  {
    val vocabLine = "(ntInt Int (-1))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    val node = maker(Nil,Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntLiteral])
    assertEquals(List(-1),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def stringLiteralMaker(): Unit = {
    val vocabLine = "(ntString String (\" \"))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.String,maker.returnType)
    val node = maker(Nil,Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringLiteral])
    assertEquals(List(" "),node.values)
    assertEquals(Types.String,node.nodeType)
  }

  @Test def boolVarMaker: Unit = {
    val vocabLine = "(ntBool Bool (b))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    val node = maker(Nil,Map("b" -> false) :: Map("b" -> true) :: Nil)
    assertTrue(node.isInstanceOf[BoolVariable])
    assertEquals(List(false,true),node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def intVarMaker: Unit = {
    val vocabLine = "(ntInt Int (_arg0_))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    val node = maker(Nil,Map("_arg0_" -> 0) :: Map("_arg0_" -> -88) :: Nil)
    assertTrue(node.isInstanceOf[IntVariable])
    assertEquals(List(0,-88),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def stringVarMaker: Unit = {
    val vocabLine = "(ntString String (str))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.String,maker.returnType)
    val node = maker(Nil,Map("str" -> "") :: Map("str" -> "abc") :: Nil)
    assertTrue(node.isInstanceOf[StringVariable])
    assertEquals(List("","abc"),node.values)
    assertEquals(Types.String,node.nodeType)
  }

  @Test def intToStringMaker: Unit = ???
  @Test def stringToIntMaker: Unit = ???
  @Test def stringLenMaker: Unit = ???

  @Test def strConcatMaker: Unit = ???
  @Test def strAtMaker: Unit = ???
  @Test def intAddMaker:Unit = ???
  @Test def intSubMaker: Unit = ???

  @Test def strReplaceMaker: Unit = ???
  @Test def stringITEMaker: Unit = ???
  @Test def intITEMaker: Unit = ???
  @Test def substringMaker: Unit = ???
  @Test def indexOfMaker: Unit = ???
}
