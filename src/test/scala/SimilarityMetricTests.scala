import ast._
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite

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

}
