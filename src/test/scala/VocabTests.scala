import ast._
import org.junit.Test
import org.junit.Assert._
import org.scalatest.junit.JUnitSuite

class VocabTests  extends JUnitSuite{
  @Test def literalMaker(): Unit = {
    val vocabLine = "Literal|0|1"
    val maker: VocabMaker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    assertTrue(maker.canMake(Nil))
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[Literal])
    assertEquals("1", node.name)
  }
  @Test def literalMakerWithType(): Unit = {
    val vocabLine = "Literal|0|False|Bool"
    val maker: VocabMaker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[Literal])
    assertEquals("False", node.name)
    assertEquals(execution.Types.Bool,node.nodeType)
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
    assertTrue(maker.canMake(Nil))
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[Variable])
    assertEquals("x",node.name)
    assertEquals(execution.Types.Any,node.nodeType)
  }

  @Test def variableMakerWithType(): Unit = {
    val vocabLine = "Variable|0|x|Int"
    val maker: VocabMaker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    val node = maker(Nil)
    assertTrue(node.isInstanceOf[Variable])
    assertEquals("x",node.name)
    assertEquals(execution.Types.Int,node.nodeType)
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
    assertTrue(maker.canMake(List(new Variable("x"), new Literal("1"))))
    val node = maker(List(new Variable("x"), new Literal("1")))
    assertTrue(node.isInstanceOf[BinOperator])
    assertEquals("and",node.name)
    assertEquals("x and 1", node.code)
  }

  @Test def binopMakerWithType(): Unit = {
    val vocabLine = "BinOperator|2|and|Bool|Bool|Bool"
    val maker = VocabMaker(vocabLine)
    assertEquals(2,maker.arity)
    assertTrue(maker.canMake(List(new Variable("x", execution.Types.Bool), new Literal("False", execution.Types.Bool))))
    val node = maker(List(new Variable("x", execution.Types.Bool), new Literal("False", execution.Types.Bool)))
    assertTrue(node.isInstanceOf[BinOperator])
    assertEquals("and",node.name)
    assertEquals("x and False", node.code)
    assertEquals(execution.Types.Bool,node.nodeType)
  }

  @Test def binopMakerWithBadChildTypes(): Unit = {
    val vocabLine = "BinOperator|2|and|Bool|Bool|Bool"
    val maker = VocabMaker(vocabLine)
    assertEquals(2,maker.arity)
    assertFalse(maker.canMake(List(new Variable("x",execution.Types.Bool), new Literal("1",execution.Types.Int))))

  }

  @Test def binopMakerWithAnyhildTypes(): Unit = {
    val vocabLine = "BinOperator|2|and|Bool"
    val maker = VocabMaker(vocabLine)
    assertEquals(2,maker.arity)
    val node = maker(List(new Variable("x"), new Literal("1")))
    assertTrue(node.isInstanceOf[BinOperator])
    assertEquals("and",node.name)
    assertEquals("x and 1", node.code)
    assertEquals(execution.Types.Bool,node.nodeType)
  }

  @Test def funcCallMaker(): Unit = {
    val vocabLine = "FunctionCall|0|foo"
    val maker = VocabMaker(vocabLine)
    assertEquals(0,maker.arity)
    assertTrue(maker.canMake(Nil))
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

  @Test def funcCallMakerTypes(): Unit = {
    val vocabLine = "FunctionCall|3|ite|Any|Bool|Any|Any"
    val maker = VocabMaker(vocabLine)
    assertEquals(execution.Types.Any, maker.returnType)
    assertTrue(maker.canMake(List(new Variable("x",execution.Types.Bool),new Literal("1",execution.Types.Int), new Literal("2"))))
    assertFalse(maker.canMake(List(new Variable("x",execution.Types.Any),new Literal("1"), new Literal("2"))))
    assertFalse(maker.canMake(List(new Variable("x",execution.Types.Int),new Literal("1"), new Literal("2"))))

    val node = maker(List(new Variable("x",execution.Types.Bool),new Literal("1",execution.Types.Int), new Literal("2")))
    assertTrue(node.isInstanceOf[FunctionCall])
    assertEquals(execution.Types.Any,node.nodeType)
    assertEquals("ite(x,1,2)",node.code)

    val maker2 = VocabMaker("FunctionCall|0|foo|Int")
    assertTrue(maker2.canMake(Nil))
    assertEquals(execution.Types.Int,maker2.returnType)
    assertEquals(execution.Types.Int,maker2(Nil).nodeType)
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

  @Test def methodCallMaker(): Unit = {
    val vocabLine = "MethodCall|1|capitalize|String|String"
    val maker = VocabMaker(vocabLine)
    assertEquals(1, maker.arity)
    assertEquals(execution.Types.String,maker.returnType)
    assertTrue(maker.canMake(List(new Variable("x", execution.Types.String))))
    assertFalse(maker.canMake(List(new Variable("x", execution.Types.Int))))
    val node = maker(List(new Variable("x", execution.Types.String)))
    assertEquals(execution.Types.String,node.nodeType)
    assertEquals("x.capitalize()",node.code)

    val vocabLine2 = "MethodCall|2|startswith|Bool|String|String"
    val maker2 = VocabMaker(vocabLine2)
    assertEquals(2,maker2.arity)
    assertEquals(execution.Types.Bool,maker2.returnType)
    val node2 = maker2(List(new Literal("''",execution.Types.String),new Literal("''",execution.Types.String)))
    assertEquals(execution.Types.Bool,node2.nodeType)
  }

  @Test def randomAccessMaker(): Unit = {
    val vocabLine = "RandomAccess|2| "
    val maker = VocabMaker(vocabLine)
    assertEquals(2, maker.arity)
    assertTrue(maker.canMake(List(new Variable("x"),new Variable("y"))))
    val node = maker(List(new Variable("x"),new Variable("y")))
    assertEquals("x[y]",node.code)

    val vocabLine2 = "RandomAccess|2| |String|String|Int"
    val maker2 = VocabMaker(vocabLine2)
    assertFalse(maker2.canMake(List(new Variable("x"),new Variable("y"))))
    assertTrue(maker2.canMake(List(new Variable("x",execution.Types.String),new Variable("y",execution.Types.Int))))
    val node2 = maker2(List(new Variable("x",execution.Types.String),new Variable("y",execution.Types.Int)))
    assertEquals(execution.Types.String,node2.nodeType)
  }

  @Test def slicingMaker(): Unit = {
    val vocabLine = "Slicing|3| "
    val maker = VocabMaker(vocabLine)
    assertEquals(3, maker.arity)
    assertTrue(maker.canMake(List(new Variable("x"),new Variable("y"),new Literal("4"))))
    val node = maker(List(new Variable("x"),new Variable("y"),new Literal("4")))
    assertEquals("x[y:4]",node.code)

    val vocabLine2 = "Slicing|3| |String|String|Int|Int"
    val maker2 = VocabMaker(vocabLine2)
    assertEquals(3,maker2.arity)
    assertFalse(maker2.canMake(List(new Variable("x"),new Variable("y"),new Literal("4"))))
    assertTrue(maker2.canMake(List(new Variable("x",execution.Types.String),new Variable("y",execution.Types.Int),new Literal("4",execution.Types.Int))))
    val node2 = maker2(List(new Variable("x",execution.Types.String),new Variable("y",execution.Types.Int),new Literal("4",execution.Types.Int)))
    assertEquals(execution.Types.String,node2.nodeType)
  }

  @Test def macroMaker(): Unit = {
    val vocabLine = "Macro|3|??.replace(??,??,1)"
    val maker = VocabMaker(vocabLine)
    assertEquals(3, maker.arity)
    assertTrue(maker.canMake(List(new Variable("x"), new Variable("y"), new Variable("z"))))
    val node = maker(List(new Variable("x"), new Variable("y"), new Variable("z")))
    assertEquals("x.replace(y,z,1)",node.code)

    val vocabLine2 = "Macro|1|?? + 22|Int|Int"
    val maker2 = VocabMaker(vocabLine2)
    assertEquals(1,maker2.arity)
    val child = new BinOperator("*",new Variable("x"),new Literal("2"),execution.Types.Int)
    assertFalse(maker2.canMake(List(new Literal("3"))))
    assertTrue(maker2.canMake(List(child)))
    val node2 = maker2(List(child))
    assertEquals(execution.Types.Int,node2.nodeType)
    assertEquals("(x * 2) + 22", node2.code)
  }
}
