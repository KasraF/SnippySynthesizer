import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration.{ChildrenIterator, Contexts, NestedChildrenIterator, ProbCosts}
import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class NestedChildrenIteratorsTests extends JUnitSuite
{
	@Test def nestedIterator1(): Unit = {
	{
		var main = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
		var mini = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
		val contexts = new Contexts(List(
			Map("name" -> "Sorin Lerner", "var" -> "Sorin Lerner"),
			Map("name" -> "Nadia", "var" -> "Nadia")))
		main += (1 -> ArrayBuffer(IntLiteral(0, 2)))
		mini += (1 -> ArrayBuffer(
			StringVariable("name", contexts.contexts),
			StringVariable("var", contexts.contexts)))

		val chit = new NestedChildrenIterator(List(Types.Iterable(Types.Any)), 1, contexts, main, mini)
		assertTrue(chit.hasNext)
		assertEquals(List("name"), chit.next().map(_.code))
		assertEquals(List("var"), chit.next().map(_.code))
		assertFalse(chit.hasNext)
	}
	}

	@Test def indicesTest(): Unit = {
		val indices1 = ProbCosts.getIndices(1).toList.map(c => c.toList)
		val indices2 = ProbCosts.getIndices(2).toList.map(c => c.toList)
		val indices3 = ProbCosts.getIndices(3).toList.map(c => c.toList)

		assertEquals(indices1, List(List(0)))
		assertEquals(indices2, List(List(0), List(1), List(0,1)))
		assertEquals(indices3, List(List(0), List(1), List(2), List(0,1), List(0,2), List(1,2), List(0,1,2)))
	}

	@Test def nestedIterator2(): Unit =
	{
		var main = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
		var mini = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
		val contexts = new Contexts(List(
			Map("name" -> "SL", "var" -> "SL"),
			Map("name" -> "N", "var" -> "N"),
			Map("name" -> "SB", "var" -> "SB")))

		main += (1 -> ArrayBuffer(StringLiteral("s", 3), IntLiteral(0, 3)))
		mini += (1 -> ArrayBuffer(StringVariable("name", contexts.contexts), StringVariable("var", contexts.contexts)))
		val chit = new NestedChildrenIterator(List(Types.String, Types.String), 2, contexts, main, mini)
		assertTrue(chit.hasNext)
		assertEquals(List("name", "\"s\""), chit.next().map(_.code))
		assertEquals(List("var", "\"s\""), chit.next().map(_.code))
		assertEquals(List("\"s\"", "name"), chit.next().map(_.code))
		val child = chit.next()
		assertEquals(List("\"s\"", "var"), child.map(_.code))
		assertEquals(List(List("s", "s", "s"), List("SL", "N", "SB")), child.map(_.values.map(_.get)))
		assertEquals(List("name", "name"), chit.next().map(_.code))
		assertEquals(List("name", "var"), chit.next().map(_.code))
		assertEquals(List("var", "name"), chit.next().map(_.code))
		assertEquals(List("var", "var"), chit.next().map(_.code))

		assertFalse(chit.hasNext)
	}

	@Test def nestedIterator3(): Unit =
	{
		var main = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
		var mini = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
		val contexts = new Contexts(List(
			Map("name" -> "SL", "var" -> "SL"),
			Map("name" -> "N", "var" -> "N"),
			Map("name" -> "SB", "var" -> "SB")))

		main += (1 -> ArrayBuffer(StringLiteral("s", 3), IntLiteral(0, 3)))
		mini += (1 -> ArrayBuffer(StringVariable("name", contexts.contexts), StringVariable("var", contexts.contexts)))
		val chit = new NestedChildrenIterator(List(Types.String, Types.String, Types.String), 3, contexts, main, mini)
		assertTrue(chit.hasNext)
		assertEquals(List("name", "\"s\"", "\"s\""), chit.next().map(_.code))
		assertEquals(List("var", "\"s\"", "\"s\""), chit.next().map(_.code))
		assertEquals(List("\"s\"", "name", "\"s\""), chit.next().map(_.code))
		assertEquals(List("\"s\"", "var", "\"s\""), chit.next().map(_.code))
		assertEquals(List("\"s\"", "\"s\"", "name"), chit.next().map(_.code))
		assertEquals(List("\"s\"", "\"s\"", "var"), chit.next().map(_.code))
		assertEquals(List("name", "name", "\"s\""), chit.next().map(_.code))
		assertEquals(List("name", "var", "\"s\""), chit.next().map(_.code))
		assertEquals(List("var", "name", "\"s\""), chit.next().map(_.code))
		assertEquals(List("var", "var", "\"s\""), chit.next().map(_.code))
		assertEquals(List("name", "\"s\"", "name"), chit.next().map(_.code))
		assertEquals(List("name", "\"s\"", "var"), chit.next().map(_.code))
		assertEquals(List("var", "\"s\"", "name"), chit.next().map(_.code))
		assertEquals(List("var", "\"s\"", "var"), chit.next().map(_.code))
		assertEquals(List("\"s\"", "name", "name"), chit.next().map(_.code))
		assertEquals(List("\"s\"", "name", "var"), chit.next().map(_.code))
		assertEquals(List("\"s\"", "var", "name"), chit.next().map(_.code))
		assertEquals(List("\"s\"", "var", "var"), chit.next().map(_.code))

		assertEquals(List("name", "name", "name"), chit.next().map(_.code))
		assertEquals(List("name", "name", "var"), chit.next().map(_.code))
		assertEquals(List("name", "var", "name"), chit.next().map(_.code))
		assertEquals(List("name", "var", "var"), chit.next().map(_.code))
		assertEquals(List("var", "name", "name"), chit.next().map(_.code))
		assertEquals(List("var", "name", "var"), chit.next().map(_.code))
		assertEquals(List("var", "var", "name"), chit.next().map(_.code))
		assertEquals(List("var", "var", "var"), chit.next().map(_.code))
		assertFalse(chit.hasNext)
	}



	@Test def onesIterator(): Unit =
	{
		//Limit by height
		val nodes = List(
			IntLiteral(1, 1),
			IntLiteral(2, 1),
			IntLiteral(3, 1),
			IntAddition(
				IntVariable("x", Map("x" -> 0) :: Nil),
				IntLiteral(1, 1)))
		val chit = new ChildrenIterator(nodes, List(Types.Int), 2)
		assertTrue(chit.hasNext)
		assertEquals(List("x + 1"), chit.next().map(_.code))
		assertFalse(chit.hasNext)
	}

	@Test def pairsHeightFiltered(): Unit =
	{
		val nodes = List(
			IntLiteral(1, 1),
			IntLiteral(2, 1),
			IntLiteral(3, 1),
			IntAddition(
				IntVariable("x", Map("x" -> 0) :: Nil),
				IntLiteral(1, 1)))
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
			new IntNode
			{
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
			}, new IntNode
			{
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

			}, new IntNode
			{
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
