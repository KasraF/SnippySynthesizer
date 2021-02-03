import edu.ucsd.snippy.PostProcessor
import edu.ucsd.snippy.ast._
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite


class PostprocessorTests  extends JUnitSuite{
  @Test def constantFoldIntOperation: Unit = {
    val expr = new IntAddition(
      new IntSubtraction(
        new IntDivision(new IntLiteral(2, 1), new IntLiteral(3, 1)),
        new IntLiteral(-1, 1)), new IntLiteral(8, 1)
    )
    val postProcessed = PostProcessor.clean(expr)
    assertEquals("9",postProcessed.code)
  }
  @Test def constantFoldIntOperationOneVar1: Unit = {
    val x = new IntVariable("x",Map("x" -> 2) :: Nil)
    val expr = new IntAddition(
      new IntSubtraction(
        new IntDivision(x, new IntLiteral(3, 1)),
        new IntLiteral(-1, 1)
      ),
      new IntLiteral(8, 1)
    )
    val postProcessed = PostProcessor.clean(expr)
    assertEquals("x // 3 - -1 + 8",postProcessed.code)
  }
  @Test def constantFoldIntOperationOneVar2: Unit = {
    val x = new IntVariable("x",Map("x" -> 2) :: Nil)
    val expr = new IntAddition(
      new IntSubtraction(
        new IntDivision(new IntLiteral(2, 1), x),
        new IntLiteral(-1, 1)
      ),
      new IntLiteral(8, 1)
    )
    val postProcessed = PostProcessor.clean(expr)
    assertEquals("2 // x - -1 + 8",postProcessed.code)
  }
  @Test def constantFoldIntOperationOneVar3: Unit = {
    val x = new IntVariable("x",Map("x" -> 2) :: Nil)
    val expr = new IntAddition(
      new IntSubtraction(
        new IntDivision(new IntLiteral(2, 1), new IntLiteral(3, 1)),
        x
      ), new IntLiteral(8, 1)
    )
    val postProcessed = PostProcessor.clean(expr)
    assertEquals("-x + 8",postProcessed.code)
  }
  @Test def constantFoldIntOperationOneVar4: Unit = {
    val x = new IntVariable("x",Map("x" -> 2) :: Nil)
    val expr = new IntAddition(
      new IntSubtraction(
        new IntDivision(new IntLiteral(2, 1), new IntLiteral(3, 1)),
        new IntLiteral(-1, 1)
      ), x
    )
    val postProcessed = PostProcessor.clean(expr)
    assertEquals("1 + x",postProcessed.code)
  }
  @Test def constantFoldStringToInt: Unit = {
    val expr = new IntAddition(
      new IntSubtraction(
        new IntDivision(new IntLiteral(2, 1), new IntLiteral(3, 1)),
        new IntLiteral(-1, 1)
      ),
      new StringToInt(new StringLiteral("8",1))
    )
    val postProcessed = PostProcessor.clean(expr)
    assertEquals("9",postProcessed.code)
  }
  @Test def constantFoldingStrings1: Unit = {
    val expr = new IntToString(new IntLiteral(-8,1))
    assertEquals("\"-8\"",PostProcessor.clean(expr).code)
  }
  @Test def constantFoldingStrings1WithVar: Unit = {
    val x = new IntVariable("x", Map("x" -> -8) :: Nil)
    val expr = new IntToString(x)
    assertEquals("str(x)",PostProcessor.clean(expr).code)
  }

  @Test def constantFoldingStrings2: Unit = {
    val expr = new Find(new StringLiteral("",1),new StringLiteral(" ",1))
    assertEquals("-1",PostProcessor.clean(expr).code)
  }
  @Test def constantFoldingStrings2Var: Unit = {
    val x = new StringVariable("x", Map("x" -> "") :: Nil)
    val expr = new Find(x,new StringLiteral(" ",1))
    assertEquals("x.find(\" \")",PostProcessor.clean(expr).code)
    val expr2 = new Find(new StringLiteral("",1),x)
    assertEquals("\"\".find(x)",PostProcessor.clean(expr2).code)
  }
  @Test def constantFoldingStrings3: Unit = {
    val expr = new StringConcat(
      new BinarySubstring(
        new StringLiteral("abc",1),
        new IntLiteral(1,1)
      ),
      new TernarySubstring(
        new StringLiteral("abcde",1),
        new IntLiteral(1,1),
        new IntSubtraction(new Length(new StringLiteral("abcde",1)), new IntLiteral(2,1))
      )
    )
    assertEquals("\"bbc\"",PostProcessor.clean(expr).code)
  }
  @Test def constantFoldingBoolean1: Unit = {
    val expr = new LessThanEq(new IntLiteral(1,1), new IntLiteral(2,1))
    assertEquals("True",PostProcessor.clean(expr).code)
    val expr2 = new GreaterThan(new IntLiteral(1,1), new IntLiteral(2,1))
    assertEquals("False",PostProcessor.clean(expr2).code)
  }

  @Test def constantFoldingBoolean1WithVar: Unit = {
    val n = new IntVariable("n", Map("n" -> 2) :: Nil)
    val expr = new LessThanEq(n, new IntLiteral(2,1))
    assertEquals("n <= 2",PostProcessor.clean(expr).code)
    val expr2 = new GreaterThan(new IntLiteral(1,1), n)
    assertEquals("1 > n",PostProcessor.clean(expr2).code)
  }

  @Test def constantFoldingBoolean2: Unit = {
    val expr = new Contains(new StringLiteral("abc",1), new StringLiteral("",1))
    assertEquals("False",PostProcessor.clean(expr).code)
  }
}
