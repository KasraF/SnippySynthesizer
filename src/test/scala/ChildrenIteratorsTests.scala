import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration.{ChildrenIterator, Contexts}
import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite


class ChildrenIteratorsTests extends JUnitSuite
{
	@Test def pairsIterator(): Unit =
	{
		val nodes = List(IntLiteral(1, 1), IntLiteral(2, 1), IntLiteral(3, 1))
		val chit = new ChildrenIterator(nodes, List(Types.Int, Types.Int), 1)
		assertTrue(chit.hasNext)
		assertEquals(List("1", "1"), chit.next().map(_.code))
		assertEquals(List("1", "2"), chit.next().map(_.code))
		assertEquals(List("1", "3"), chit.next().map(_.code))
		assertEquals(List("2", "1"), chit.next().map(_.code))
		assertEquals(List("2", "2"), chit.next().map(_.code))
		assertEquals(List("2", "3"), chit.next().map(_.code))
		assertEquals(List("3", "1"), chit.next().map(_.code))
		assertEquals(List("3", "2"), chit.next().map(_.code))
		assertEquals(List("3", "3"), chit.next().map(_.code))
		assertFalse(chit.hasNext)
	}

	@Test def onesIterator(): Unit =
	{
		//Limit by height
		val nodes = List(IntLiteral(1, 1), IntLiteral(2, 1), IntLiteral(3, 1), IntAddition(IntVariable("x", Map("x" -> 0) :: Nil), IntLiteral(1, 1)))
		val chit = new ChildrenIterator(nodes, List(Types.Int), 2)
		assertTrue(chit.hasNext)
		assertEquals(List("x + 1"), chit.next().map(_.code))
		assertFalse(chit.hasNext)
	}

	@Test def pairsHeightFiltered(): Unit =
	{
		val nodes = List(IntLiteral(1, 1), IntLiteral(2, 1), IntLiteral(3, 1), IntAddition(IntVariable("x", Map("x" -> 0) :: Nil), IntLiteral(1, 1)))
		val chit = new ChildrenIterator(nodes, List(Types.Int, Types.Int), 2)
		assertTrue(chit.hasNext)
		assertEquals(List("1", "x + 1"), chit.next().map(_.code))
		assertEquals(List("2", "x + 1"), chit.next().map(_.code))
		assertEquals(List("3", "x + 1"), chit.next().map(_.code))
		assertEquals(List("x + 1", "1"), chit.next().map(_.code))
		assertEquals(List("x + 1", "2"), chit.next().map(_.code))
		assertEquals(List("x + 1", "3"), chit.next().map(_.code))
		assertEquals(List("x + 1", "x + 1"), chit.next().map(_.code))
		assertFalse(chit.hasNext)
	}

	@Test def costRollingFourChildren(): Unit =
	{
		val nodes = List(
			new IntNode {
				override val values: List[Option[Int]] = List(Some(0))
				override val code: String = "0"
				override val height: Int = 0
				override val terms: Int = 1
				override val children: Iterable[ASTNode] = Nil
				override protected val parenless: Boolean = true

				override val usesVariables: Boolean = false

				override def includes(varName: String): Boolean = false

				override def updateValues(contexts: Contexts): IntNode = null

				override def cost: Int = 1
			},
			new IntNode {
				override val values: List[Option[Int]] = List(Some(1))
				override val code: String = "1"
				override val height: Int = 0
				override val terms: Int = 1
				override val children: Iterable[ASTNode] = Nil
				override protected val parenless: Boolean = true

				override val usesVariables: Boolean = false

				override def includes(varName: String): Boolean = false

				override def updateValues(contexts: Contexts): IntNode = null

				override def cost: Int = 1

			},
			new IntNode {
				override val values: List[Option[Int]] = List(Some(2))
				override val code: String = "x"
				override val height: Int = 0
				override val terms: Int = 1
				override val children: Iterable[ASTNode] = Nil
				override protected val parenless: Boolean = true

				override def includes(varName: String): Boolean = false

				override val usesVariables: Boolean = false

				override def cost: Int = 1

				override def updateValues(contexts: Contexts): IntNode = null
			})
	}
}
