import ast._
import org.junit.Test
import org.junit.Assert._
import org.scalatest.junit.JUnitSuite


class ASTTests extends JUnitSuite{
  @Test def literalNode(): Unit = {
    val literalNode: ASTNode = new Literal("1")
    assertEquals("1",literalNode.name)
    assertEquals("1", literalNode.code)
    assertEquals(0, literalNode.height)
  }

  @Test def variableNode(): Unit = {
    val varNode: ASTNode = new Variable("x")
    assertEquals("x",varNode.name)
    assertEquals("x", varNode.code)
    assertEquals(0, varNode.height)
  }

  @Test def binaryOpNode(): Unit = {
    val binOpNode: ASTNode = new BinOperator("+", new Literal("1"), new Literal("'b'"))
    assertEquals("+",binOpNode.name)
    assertEquals("1 + 'b'", binOpNode.code)
    assertEquals(1, binOpNode.height)

    val binOpNode2 = new BinOperator("*", new Literal("2"), binOpNode)
    assertEquals("*",binOpNode2.name)
    assertEquals("2 * (1 + 'b')", binOpNode2.code)
    assertEquals(2, binOpNode2.height)
  }

  @Test def funcCallNode(): Unit = {
    val funcNode: ASTNode = new FunctionCall("foo",0, List())
    assertEquals("foo",funcNode.name)
    assertEquals("foo()", funcNode.code)
    assertEquals(0,funcNode.height)

    val funcNode2 = new FunctionCall("bar",2,List(new Literal("1"),new Variable("x")))
    assertEquals("bar", funcNode2.name)
    assertEquals("bar(1,x)",funcNode2.code)
    assertEquals(1, funcNode2.height)
  }

  @Test def lambdaNode(): Unit = {
    val lambdaNode: ASTNode = new Lambda(List(), new Literal("False"))
    assertEquals("()", lambdaNode.name)
    assertEquals("lambda : False", lambdaNode.code)

    val x = new Variable("x")
    val y = new Variable("y")
    val lambdaNode2: ASTNode = new Lambda(List(x, y), new BinOperator("+",x,y))
    assertEquals("(x,y)",lambdaNode2.name)
    assertEquals("lambda x,y: x + y", lambdaNode2.code)
  }

  @Test def methodNode(): Unit = {
    val methodNode: ASTNode = new MethodCall("foo", 1, new Variable("x"),Nil)
    assertEquals("foo",methodNode.name)
    assertEquals(1,methodNode.height)
    assertEquals("x.foo()",methodNode.code)

    val methodNode2 = new MethodCall("bar",3,methodNode,List(new Variable("y"),methodNode))
    assertEquals("bar",methodNode2.name)
    assertEquals(2,methodNode2.height)
    assertEquals("x.foo().bar(y,x.foo())",methodNode2.code)
  }

  @Test def slicingNode(): Unit = {
    val slicingNode: ASTNode = new Slicing(new Variable("s"),new Literal("0"), new Literal("-1"))
    assertEquals(3,slicingNode.arity)
    assertEquals(1, slicingNode.height)
    assertEquals("s[0:-1]",slicingNode.code)
  }

  @Test def raccessNode(): Unit = {
    val raccessNode: ASTNode = new RandomAccess(new Variable("s"),new Literal("0"))
    assertEquals(2,raccessNode.arity)
    assertEquals(1,raccessNode.height)
    assertEquals("s[0]",raccessNode.code)
  }
  @Test def macroNode(): Unit = {
    val macroNode: ASTNode = new Macro("??.replace(??,??,1)".split("\\?\\?"),List(new Variable("s"),new Literal("'a'"),new Literal("'b'")))
    assertEquals(3, macroNode.arity)
    assertEquals(1, macroNode.height)
    assertEquals("s.replace('a','b',1)", macroNode.code)
  }
}
