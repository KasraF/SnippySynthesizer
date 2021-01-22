import edu.ucsd.snippy.SynthesisTask
import edu.ucsd.snippy.ast.Types
import edu.ucsd.snippy.utils.SingleVariablePredicate
import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite

class SynthesisTaskTests extends JUnitSuite
{
	@Test def inferEmptyListTypeFromOtherExamples: Unit =
	{
		val task1 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["l"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "l": "[]"
			  |    },
			  |    {
			  |      "l": "[1,2,3,1]"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(
			Types.IntList,
			task1.predicate.asInstanceOf[SingleVariablePredicate].retType)

		val task2 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["l"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "l": "[]"
			  |    },
			  |    {
			  |      "l": "['a','b','3','1']"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(
			Types.StringList,
			task2.predicate.asInstanceOf[SingleVariablePredicate].retType)
	}

	@Test def listMapMismatchShouldFail: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "[]",
			  |    },
			  |    {
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    }
			  |
			  |  ]
			  |}""".stripMargin)

		assertEquals(
			Types.Unknown,
			task.predicate.asInstanceOf[SingleVariablePredicate].retType)
	}

	@Test def inferEmptyMapTypeFromOtherExamples: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "{}",
			  |    },
			  |    {
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    }
			  |
			  |  ]
			  |}""".stripMargin)

		val returnType = task.predicate.asInstanceOf[SingleVariablePredicate].retType
		assertTrue(returnType.isInstanceOf[Types.Map])
		assertEquals(Types.String, returnType.asInstanceOf[Types.Map].keyType)
		assertEquals(Types.Int, returnType.asInstanceOf[Types.Map].valType)
	}

	@Test def onlyEmptyList: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["l"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "l": "[]"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(
			Types.StringList,
			task.predicate.asInstanceOf[SingleVariablePredicate].retType)
	}

	@Test def onlyEmptyMap: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "{}",
			  |    }
			  |  ]
			  |}""".stripMargin)
		val returnType = task.predicate.asInstanceOf[SingleVariablePredicate].retType
		assertTrue(returnType.isInstanceOf[Types.Map])
		assertEquals(Types.String, returnType.asInstanceOf[Types.Map].keyType)
		assertEquals(Types.Int, returnType.asInstanceOf[Types.Map].valType)
	}

	@Test def emptyMapInInputVal: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "counts": "{}",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    },
			  |    {
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    }
			  |
			  |  ]
			  |}""".stripMargin)
		val varType = task.parameters.find(kv => kv._1 == "counts").get._2.asInstanceOf[Types.Map]
		assertEquals(Types.String, varType.keyType)
		assertEquals(Types.Int, varType.valType)
	}

	@Test def emptyListInInputVal: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "counts": "[]",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    },
			  |    {
			  |      "counts": "[1, 2, 3]",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    }
			  |
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.IntList, task.parameters.find(kv => kv._1 == "counts").get._2)
	}

	@Test def onlyEmptyListInInputVal: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "counts": "[]",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    },
			  |    {
			  |      "counts": "[]",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    }
			  |
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.StringList, task.parameters.find(kv => kv._1 == "counts").get._2)
	}
}
