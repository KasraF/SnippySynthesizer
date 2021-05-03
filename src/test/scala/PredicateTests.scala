import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.predicates.{MultiEdge, Node, SingleEdge}
import edu.ucsd.snippy.{Snippy, SynthesisTask}
import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite

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
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
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

		val solution = Snippy.synthesize(task, 7)

		assert(solution._1.isDefined)

		// This is optional, as long as we get a correct solution
		assertEquals("x = 1", solution._1.get)
	}

	@Test
	def twoVariablePredicateTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x", "y"],
			  |  "previousEnvs": {
			  |    "1": {
			  |      "#": "",
			  |      "z": "1",
			  |      "x": "0",
			  |      "y": "0"
			  |    }
			  |  },
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "0",
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
		assertEquals(List(Map("z" -> 1, "x" -> 0, "y" -> 0)), graphStart.state)

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

		val solution = Snippy.synthesize(task, 7)

		assert(solution._1.isDefined)

		// This is optional, as long as we get a correct solution
		// assertEquals("y = x + 3\nx = y - 1", solution._1.get)
	}

	@Test
	def twoVariablePredicateUpdateTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x", "y"],
			  |  "previousEnvs": {
			  |    "1": {
			  |      "#": "",
			  |      "x": "1",
			  |      "y": "1",
			  |    }
			  |  },
			  |  "envs": [
			  |    {
			  |      "time": 1,
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

		val solution = Snippy.synthesize(task, 7)

		assert(solution._1.isDefined)

		// This is optional, as long as we get a correct solution
		assertEquals("x += x\ny = x + y", solution._1.get)
	}

	@Test
	def threeVariablePredicateTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x", "y", "z"],
			  |  "previousEnvs": {
			  |    "1": {
			  |      "#": "",
			  |      "x": "1",
			  |      "y": "1",
			  |      "z": "1"
			  |    }
			  |  },
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "0",
			  |      "x": "2",
			  |      "y": "3",
			  |      "z": "4"
			  |    }
			  |  ]
			  |}""".stripMargin)
		val pred = task.predicate

		assertEquals("MultilineMultivariablePredicate", pred.getClass.getSimpleName)
		assert(getField(pred, "graphStart").isDefined)

		// Check the entire graph!
		val graphStart: Node = getField(pred, "graphStart").get
		var graphEnd: Node = null
		var lvlOneNodes: Map[Set[String], Node] = Map()
		var lvlTwoNodes: Map[Set[String], Node] = Map()

		// We should have 7 edges: 3 single, 4 multi
		assertEquals(7, graphStart.edges.length)
		assertEquals(3, graphStart.edges.count(_.isInstanceOf[SingleEdge]))
		assertEquals(4, graphStart.edges.count(_.isInstanceOf[MultiEdge]))

		assertEquals(graphStart.isEnd, false)
		assertEquals(List(Map("x" -> 1, "y" -> 1, "z" -> 1)), graphStart.state)

		var seen = Set[Node]()

		graphStart.edges.foreach(edge => {
			assertEquals(false, edge.isComplete)
			assertEquals(graphStart, edge.parent)

			assertFalse(seen.contains(edge.child))
			seen = seen + edge.child

			edge match {
				case SingleEdge(program, variable, outputType, parent, child) =>
					// The only single edges from the starting node should be the three edges to
					// intermediate contexts.
					assertEquals(None, program)
					assertTrue(variable == "x" || variable == "y" || variable == "z")
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

					assertFalse(lvlOneNodes.contains(Set(variable)))
					lvlOneNodes += Set(variable) -> child
				case MultiEdge(programs, outputTypes, parent, child) =>
					val variables = programs.keys.toList
					programs.size match {
						case 2 =>
							// Intermediate node
							assertEquals(programs.size, outputTypes.size)

						case 3 =>
							// Ending node
							assertEquals(Map("x" -> None, "y" -> None, "z" -> None), programs)
							assertEquals(Map("x" -> Types.Int, "y" -> Types.Int, "z" -> Types.Int), outputTypes)
							assertTrue(child.isEnd)

							assertNull(graphEnd)
							graphEnd = child
						case _ => fail()
					}

					assertFalse(lvlTwoNodes.contains(variables.toSet))
					if (programs.size < 3) {
						lvlTwoNodes += variables.toSet -> child
					} else {
						graphEnd =child
					}
			}
		})

		assertNotNull(graphEnd)
		assertEquals(3, lvlOneNodes.size)
		assertEquals(3, lvlTwoNodes.size)

		lvlOneNodes.foreach { case (variables: Set[String], node: Node) =>
			assertEquals(1, variables.size)
			val variable = variables.head

			assertFalse(node.isEnd)
			assertTrue(node.edges.length == 3)

			node.edges.foreach {
				case SingleEdge(program, variable: String, outputType, parent, child) =>
					assertEquals(None, program)
					assertEquals(outputType, Types.Int)
					assertEquals(node, parent)
					assertEquals(lvlTwoNodes(variables + variable), child)
					assertNotEquals(variable, variables.head)
				case MultiEdge(programs, outputTypes, parent, child) =>
					programs.values.forall(_.isEmpty)
					outputTypes.values.forall(_ == Types.Int)
					assertEquals(node, parent)
					assertEquals(graphEnd, child)
					assertEquals(2, programs.size)
					assertFalse(programs.keySet.contains(variable))
					assertEquals(Set("x", "y", "z"), programs.keySet + variable)
			}
		}

		lvlTwoNodes.foreach { case (variables: Set[String], node: Node) =>
			assertEquals(2, variables.size)
			assertFalse(node.isEnd)
			assertTrue(node.edges.length == 1)

			node.edges.head match {
				case SingleEdge(program, variable: String, outputType, parent, child) =>
					assertEquals(None, program)
					assertEquals(outputType, Types.Int)
					assertEquals(node, parent)
					assertEquals(graphEnd, child)
					assertFalse(variables.contains(variable))
				case _ => fail()
			}
		}

		val solution = Snippy.synthesize(task, 7)

		assert(solution._1.isDefined)

		// This is optional, as long as we get a correct solution
		// assertEquals("x = x + x\nz = x + x\ny = x + y", solution._1.get)
	}
}
