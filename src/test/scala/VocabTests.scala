import ast._
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite

class VocabTests  extends JUnitSuite{
  @Test def literalMakerWithType(): Unit =  ??? /*{
    val vocabLine = "Literal|0|False|Bool"
    val maker: VocabMaker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[Literal])
    assertEquals("False", node.name)
    assertEquals(execution.Types.Bool,node.nodeType)
  }*/
}
