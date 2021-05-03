import edu.ucsd.snippy.ast.{DoubleNode, DoublesSum, ListAppend, ListVariable, Types}
import edu.ucsd.snippy.utils.Utils
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite
import org.junit.Assert._
import spire.syntax.all._

import scala.collection.mutable

class DoubleTests extends JUnitSuite
{

	import Utils.FuzzyDoubleEq

	@Test def compareDoubles(): Unit =
	{
		assertTrue(0.3 === 0.3000000000000000000000000001)
	}

	import spire.std.option._

	@Test def compareDoubleOptions(): Unit =
	{
		assertTrue(Some(0.3) === Some(0.3000000000000000000000000001))
	}

	import spire.std.seq._

	@Test def compareDoubleOptionLists(): Unit =
	{
		assertTrue(List(Some(0.3), Some(1.2)) === List(Some(0.3000000000000000000000000001), Some(1.2)))
	}

//	// TODO fixing this has a big performance hit.
//	@Test def setOfImplicitThing(): Unit =
//	{
//		val classValues: mutable.Set[List[Option[Any]]] = mutable.HashSet[List[Option[Any]]]()
//		classValues += List(Some(0.3), Some(1.2))
//		assertEquals(1, classValues.size)
//		classValues += List(Some(0.3000000000000000000000000001), Some(1.2))
//		assertEquals(2, classValues.size)
//	}

	@Test def programEnvToEnv(): Unit =
	{
		val contexts = List(
			Map("lastTwo" -> List(1.5, -0.6)),
			Map("lastTwo" -> List(-0.6, 0.9)),
			Map("lastTwo" -> List(0.9, 0.3)),
			Map("lastTwo" -> List(0.3, 1.2))
		)
		val outs = List(0.9, 0.3, 1.2, 1.5)
		val sumLastTwo = DoublesSum(ListVariable[Double]("lastTwo", contexts, Types.Double))
		assertTrue(sumLastTwo.values.zip(outs).forall(Utils.programConnects))
	}

	@Test def programEnvToEnvLists(): Unit =
	{
		val contexts = List(
			Map("lastTwo" -> List(1.5, -0.6), "out" -> List(1.5, -0.6)),
			Map("lastTwo" -> List(-0.6, 0.9), "out" -> List(1.5, -0.6, 0.9)),
			Map("lastTwo" -> List(0.9, 0.3), "out" -> List(1.5, -0.6, 0.9, 0.3)),
			Map("lastTwo" -> List(0.3, 1.2), "out" -> List(1.5, -0.6, 0.9, 0.3, 1.2))
		)
		val outs = List(List(1.5, -0.6, 0.9), List(1.5, -0.6, 0.9, 0.3), List(1.5, -0.6, 0.9, 0.3, 1.2), List(1.5, -0.6, 0.9, 0.3, 1.2, 1.5))
		val sumLastTwo = DoublesSum(ListVariable[Double]("lastTwo", contexts, Types.Double))
		val outPlusSum = ListAppend[Double, DoubleNode](ListVariable[Double]("out", contexts, Types.Double), sumLastTwo)
		assertTrue(outPlusSum.values.zip(outs).forall(Utils.programConnects))
	}
}
