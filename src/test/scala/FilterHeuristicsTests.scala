
import enumeration.EnumerationHeuristics
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite

class FilterHeuristicsTests extends JUnitSuite{
//  @Test def filterConcatingStringLiteralTwice(): Unit = {
//    val innerPlus = new BinOperator("+",new Variable("x", Types.String),new Literal("'a'",Types.String),Types.String)
//    val outerPlus = new BinOperator("+",innerPlus,new Literal("'a'",Types.String),Types.String)
//    assertFalse(EnumerationHeuristics.keep(outerPlus))
//    val innerPlus2 = new BinOperator("+", new Literal("'a'",Types.String), new Variable("y",Types.String),Types.String)
//    val outerPlus2 = new BinOperator("+",innerPlus,innerPlus2,Types.String)
//    assertFalse(EnumerationHeuristics.keep(outerPlus2))
//    val outerPlus3 = new BinOperator("+",new Literal("'b'",Types.String),innerPlus, Types.String)
//    assertTrue(EnumerationHeuristics.keep(outerPlus3))
//
//    val spMaker = VocabMaker("""Literal|0|" "|String""")
//    val plMaker = VocabMaker("""BinOperator|2|+|String|String|String""")
//    val sp = spMaker(Nil)
//    val spsp = plMaker(List(sp,sp))
//    assertFalse(EnumerationHeuristics.keep(spsp))
//  }

}
