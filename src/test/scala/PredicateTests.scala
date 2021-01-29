import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.utils.{MultiEdge, MultilineMultivariablePredicate, Node, SingleEdge}
import edu.ucsd.snippy.{Snippy, SynthesisTask}
import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite

import scala.collection.mutable

class PredicateTests extends JUnitSuite
{
	def getField[T](obj: Object, name: String): Option[T] =
		{
			if (obj.getClass.getDeclaredFields.map(f => f.getName).contains(name)) {
				val field = obj.getClass.getDeclaredField(name)
				field.setAccessible(true)
				Some(field.get(obj).asInstanceOf[T])
			} else {
				None
			}
		}

	@Test
	def singlePredicateTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "x": "1",
			  |      "y": "2"
			  |    }
			  |  ]
			  |}""".stripMargin)
		val pred = task.predicate

		assertEquals("SingleVariablePredicate", pred.getClass.getSimpleName)
		assertEquals(Some("x"), getField(pred, "varName"))
		assertEquals(Some(Types.Int), getField(pred, "retType"))
		assertEquals(Some(List(1)), getField(pred, "values"))

		val solution = Snippy.synthesizeFromTask(task, 7, false)

		assert(solution._1.isDefined)

		// This is optional, as long as we get a correct solution
		assertEquals("x = y - 1", solution._1.get)
	}

	@Test
	def twoVariablePredicateTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x", "y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "z": "1",
			  |      "x": "2",
			  |      "y": "3"
			  |    }
			  |  ]
			  |}""".stripMargin)
		val pred = task.predicate

		assertEquals("MultilineMultivariablePredicate", pred.getClass.getSimpleName)
		assert(getField(pred, "graphStart").isDefined)

		// Check the entire graph!
		val graphStart: Node = getField(pred, "graphStart").get
		var graphEnd: Node = null
		var intermediateNodes: List[Node] = Nil

		assertEquals(3, graphStart.edges.length)
		assertEquals(graphStart.isEnd, false)
		assertEquals(List(Map("z" -> 1)), graphStart.state)

		graphStart.edges.foreach(edge => {
			assertEquals(false, edge.isComplete)
			assertEquals(graphStart, edge.parent)

			edge match {
				case SingleEdge(program, variable, outputType, parent, child) =>
					// The only single edges from the starting node should be the two edges to
					// intermediate contexts.
					assertEquals(None, program)
					assertTrue(variable == "x" || variable == "y")
					assertEquals(Types.Int, outputType)
					assertFalse(child.isEnd)

					// Only the single variable should be different between the nodes
					assertTrue(parent.state
						           .zip(child.state)
						           .map(tup => (tup._1.get(variable), tup._2.get(variable)))
						           .forall { case (l, r) => (l.isEmpty && r.isDefined) || l != r })
					assertTrue(parent.state
						           .zip(child.state)
						           .map(tup => (tup._1 - variable, tup._2 - variable))
						           .forall { case (l, r) => l == r })
					intermediateNodes = child :: intermediateNodes
				case MultiEdge(programs, outputTypes, parent, child) =>
					// The only multiedge should be to the ending context
					assertEquals(Map("x" -> None, "y" -> None), programs)
					assertEquals(Map("x" -> Types.Int, "y" -> Types.Int), outputTypes)
					assertTrue(child.isEnd)
					// TODO Check that the correct variables are different between parent and child
					graphEnd = child
			}
		})

		assertNotEquals(null, graphEnd)
		assertEquals(2, intermediateNodes.length)

		intermediateNodes.foreach(node => {
			assertFalse(node.isEnd)
			assertEquals(1, node.edges.length)

			val edge = node.edges.head

			assertTrue(edge.isInstanceOf[SingleEdge])
			assertEquals(edge.child, graphEnd)
		})

		val solution = Snippy.synthesizeFromTask(task, 7, false)

		assert(solution._1.isDefined)

		// This is optional, as long as we get a correct solution
		assertEquals("y = z + z + z\nx = z + z", solution._1.get)
	}

	@Test
	def twoVariablePredicateUpdateTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x", "y"],
			  |  "previous_env": {
			  |      "#": "",
			  |      "x": "1",
			  |      "y": "1",
			  |  },
			  |  "envs": [
			  |    {
			  |      "#": "0",
			  |      "x": "2",
			  |      "y": "3"
			  |    }
			  |  ]
			  |}""".stripMargin)
		val pred = task.predicate

		assertEquals("MultilineMultivariablePredicate", pred.getClass.getSimpleName)
		assert(getField(pred, "graphStart").isDefined)

		val graphStart: Node = getField(pred, "graphStart").get

		assertEquals(3, graphStart.edges.length)
		assertEquals(graphStart.isEnd, false)
		assertEquals(List(Map("x" -> 1, "y" -> 1)), graphStart.state)

		val solution = Snippy.synthesizeFromTask(task, 7, false)

		assert(solution._1.isDefined)

		// This is optional, as long as we get a correct solution
		assertEquals("x = x + x\ny = x + y", solution._1.get)
	}
}
