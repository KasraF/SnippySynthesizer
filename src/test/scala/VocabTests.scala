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
    val node = maker(Nil,Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[BoolLiteral])
    assertEquals(List(false), node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def intLiteralMaker(): Unit =  ???
  @Test def stringLiteralMaker(): Unit = ???
}
