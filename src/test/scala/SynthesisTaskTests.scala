import edu.ucsd.snippy.SynthesisTask
import edu.ucsd.snippy.ast.Types
import edu.ucsd.snippy.predicates.SingleVariablePredicate
import edu.ucsd.snippy.vocab.BasicVocabMaker
import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite

class SynthesisTaskTests extends JUnitSuite
{
	@Test def inferEmptyListTypeFromOtherExamples(): Unit =
	{
		val task1 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["l"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "l": "[]"
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
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
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "l": "[]"
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
			  |      "l": "['a','b','3','1']"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(
			Types.StringList,
			task2.predicate.asInstanceOf[SingleVariablePredicate].retType)
	}

	@Test def listMapMismatchShouldFail(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "[]",
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
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

	@Test def inferEmptyMapTypeFromOtherExamples(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
			  |      "filters": "{}",
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
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

	@Test def onlyEmptyList(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["l"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "l": "[]"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(
			Types.StringList,
			task.predicate.asInstanceOf[SingleVariablePredicate].retType)
	}

	@Test def onlyEmptyMap(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
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

	@Test def emptyMapInInputVal(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "counts": "{}",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
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

	@Test def emptyListInInputVal(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "counts": "[]",
			  |      "filters": "{'a': 2, 'b': 2}"
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
			  |      "counts": "[1, 2, 3]",
			  |      "filters": "{'a': 2, 'b': 2}"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.IntList, task.parameters.find(kv => kv._1 == "counts").get._2)
	}

	@Test def onlyEmptyListInInputVal(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["filters"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "counts": "[]",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
			  |      "counts": "[]",
			  |      "filters": "{'a': 2, 'b': 2}",
			  |    }
			  |
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.StringList, task.parameters.find(kv => kv._1 == "counts").get._2)
	}

	@Test def stringLiteralsTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "x": "'x'",
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
			  |      "x": "'y'",
			  |    }
			  |  ]
			  |}""".stripMargin)

		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List(Some("x"))))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List(Some("y"))))
	}

	@Test def multivariableStringLiteralsTest(): Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x", "y"],
			  |  "previousEnvs": {},
			  |  "envs": [
			  |    {
			  |      "time": 1,
			  |      "#": "",
			  |      "x": "'x'",
			  |      "y": "'x'"
			  |    },
			  |    {
			  |      "time": 2,
			  |      "#": "",
			  |      "x": "'y'",
			  |      "y": "'y'",
			  |    },
			  |    {
			  |      "time": 3,
			  |      "#": "",
			  |      "x": "'a'",
			  |      "y": "'b'"
			  |    },
			  |    {
			  |      "time": 4,
			  |      "#": "",
			  |      "x": "'b'",
			  |      "y": "'a'"
			  |    }
			  |  ]
			  |}""".stripMargin)

		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List(Some("x"))))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List(Some("y"))))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List(Some("a"))))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List(Some("b"))))
	}
}
