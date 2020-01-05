import ast._
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite
import sygus._

class SimilarityMetricTests  extends JUnitSuite {

  @Test def trivialIs0: Unit = {
    val ast1: ASTNode = new StringLiteral("x",1)
    val ast2: ASTNode = new StringLiteral("x",1)
    assertEquals(0,SimilarityMetric.compute(ast1,ast2))

    val ast3 = new StringAt(ast1.asInstanceOf[StringNode],new IntLiteral(0,1))
    val ast4 = new StringLength(ast2.asInstanceOf[StringNode])

    assertEquals(0,SimilarityMetric.compute(ast3,ast4))
  }

  @Test def sameNontrivialIsTerms: Unit = {
    val same1 = new StringLength(new StringLiteral("abc",1))
    val same2 = new StringLength(new StringLiteral("abc",1))
    assertEquals(2,SimilarityMetric.compute(same1,same2))
    val ast1 = new IntAddition(same1,new IntLiteral(0,1))
    val ast2 = new IntSubtraction(new IntLiteral(18,1),same2)
    assertEquals(2,SimilarityMetric.compute(ast1,ast2))
  }

  @Test def sharedParentPlus1: Unit = {
    val same1 = new IntToString(new IntLiteral(2,1))
    val same2 = new IntToString(new IntLiteral(2,1))

    val top1 = new Contains(same1,new StringLiteral("x",1))
    val top2 = new Contains(new StringLiteral("y",1),same2)
    assertEquals(2,SimilarityMetric.compute(top1,top2))

    val top3 = new Contains(same2,new StringLiteral("z",1))
    assertEquals(3, SimilarityMetric.compute(top1,top3))

    val same3 = new StringConcat(new StringLiteral("123",1),new StringLiteral("456",1))
    val same4 = new StringConcat(new StringLiteral("123",1),new StringLiteral("456",1))
    assertEquals(3, SimilarityMetric.compute(same3,same4))
    val top4 = new IndexOf(same1,same3,new IntLiteral(4,1))
    val top5 = new IndexOf(same2,same4,new IntLiteral(18,1))
    assertEquals(6,SimilarityMetric.compute(top4,top5))
  }

  @Test def sharedParentOtherChildren: Unit = {
    val same1 = new IntToString(new IntLiteral(2,1))
    val same2 = new IntToString(new IntLiteral(2,1))

    val same3 = new StringConcat(new StringLiteral("123",1),new StringLiteral("456",1))
    val same4 = new StringConcat(new StringLiteral("123",1),new StringLiteral("456",1))

    val top1 = new Contains(same1,new StringITE(new BoolLiteral(true,1),same3,new StringLiteral("bbb",1)))
    val top3 = new Contains(same2,same4)
    assertEquals(1 + same1.terms + same3.terms, SimilarityMetric.compute(top1,top3))
  }

  @Test def bugFromTheWild: Unit = {
    val prog1 = "(str.substr _arg_0 0 (str.indexof _arg_0 \" \" 1))"
    val prog2 = "(ite (str.contains _arg_0 \"ssp.\") (str.substr _arg_0 0 (+ (str.indexof _arg_0 \"ssp.\" 1) -1)) (str.substr _arg_0 0 (str.indexof _arg_0 \" \" 1)))"
    assertTrue(prog2.contains(prog1)) //because of this:

    val tree1: ASTNode = Main.interpret("src/test/benchmarks/too-hard/41503046.sl",prog1).get._1
    val tree2: ASTNode = Main.interpret("src/test/benchmarks/too-hard/41503046.sl",prog2).get._1

    //we expect this:
    assertEquals(tree1.terms, SimilarityMetric.compute(tree1,tree2))
  }
}
