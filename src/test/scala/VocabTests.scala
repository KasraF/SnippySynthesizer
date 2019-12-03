import ast._
import org.junit.Test
import org.junit.Assert._
import org.scalatest.junit.JUnitSuite

class VocabTests  extends JUnitSuite{
  @Test def literalMaker(): Unit = {
    val vocabLine = "Literal|0|1"
    val maker: VocabMaker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[Literal])
    assertEquals("1", node.name)
  }
  @Test def vocabLiterals(): Unit = {
    val vocabString =
      """Literal|0|0
        #Literal|0|'a'
        #Literal|0|True""".stripMargin('#')
    val vocab = VocabFactory(vocabString)
    for(term <- vocab.leaves) {
      assertEquals(0,term.arity)
      val l = term(Nil)
      assertTrue(l.isInstanceOf[Literal])
    }
  }

  @Test def variableMaker(): Unit = {
    val vocabLine = "Variable|0|x"
    val maker: VocabMaker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[Variable])
    assertEquals("x",node.name)
  }

  @Test def vocabLeafTerms(): Unit = {
    val vocabString =
      """Variable|0|input
        #Literal|0|False
        #Variable|0|x""".stripMargin('#')
    val vocab = VocabFactory(vocabString)
    val vocabIterator = vocab.leaves
    assertTrue(vocabIterator.hasNext)
    val t1 = vocabIterator.next()(Nil)
    assertTrue(t1.isInstanceOf[Variable])
    assertEquals("input",t1.name)
    val t2 = vocabIterator.next()(Nil)
    assertTrue(t2.isInstanceOf[Literal])
    assertEquals("False",t2.name)
    val t3 = vocabIterator.next()(Nil)
    assertTrue(t3.isInstanceOf[Variable])
    assertEquals("x",t3.name)
    assertFalse(vocabIterator.hasNext)
  }

  @Test def binopMaker(): Unit = {
    val vocabLine = "BinOperator|2|and"
    val maker = VocabMaker(vocabLine)
    assertEquals(2,maker.arity)
    val node = maker(List(new Variable("x"), new Literal("1")))
    assertTrue(node.isInstanceOf[BinOperator])
    assertEquals("and",node.name)
    assertEquals("x and 1", node.code)
  }

  @Test def funcCallMaker(): Unit = {
    val vocabLine = "FunctionCall|0|foo"
    val maker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[FunctionCall])
    assertEquals(0, node.arity)
    assertEquals("foo", node.name)

    val vocabLine2 = "FunctionCall|3|ite"
    val maker2 = VocabMaker(vocabLine2)
    assertEquals(3,maker2.arity)
    val node2 = maker2(List(new Literal("True"), new Variable("x"), new Variable("y")))
    assertTrue(node2.isInstanceOf[FunctionCall])
    assertEquals(3,node2.arity)
    assertEquals("ite", node2.name)
    assertEquals("ite(True,x,y)", node2.code)
  }

  @Test def vocabMixedArities(): Unit = {
    val vocabString =
      """FunctionCall|2|foo
        #FunctionCall|0|bar
        #Variable|0|input
        #BinOperator|2|/""".stripMargin('#')

    val vocab = VocabFactory(vocabString)
    val leavesIter = vocab.leaves
    val l1 = leavesIter.next()(Nil)
    assertEquals("bar", l1.name)
    val l2 = leavesIter.next()(Nil)
    assertEquals("input", l2.name)
    assertFalse(leavesIter.hasNext)

    val nodesIter = vocab.nonLeaves
    val n1 = nodesIter.next()(List(new Literal("1"), new Literal("2")))
    assertEquals("foo(1,2)", n1.code)
    val n2 = nodesIter.next()(List(new Literal("5"), new Literal("10")))
    assertEquals("5 / 10", n2.code)
    assertFalse(nodesIter.hasNext)
  }
}
