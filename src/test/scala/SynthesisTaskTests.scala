import edu.ucsd.snippy.SynthesisTask
import edu.ucsd.snippy.ast.Types
import edu.ucsd.snippy.utils.{PartialOutputPredicate, SingleVariablePredicate}
import edu.ucsd.snippy.vocab.BasicVocabMaker
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

	@Test def stringLiteralsTest: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "x": "'x'",
			  |    },
			  |    {
			  |      "x": "'y'",
			  |    }
			  |  ]
			  |}""".stripMargin)

		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List("x")))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List("y")))
	}

	@Test def multivariableStringLiteralsTest: Unit =
	{
		val task = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["x", "y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |      "x": "'x'",
			  |      "y": "'x'"
			  |    },
			  |    {
			  |      "x": "'y'",
			  |      "y": "'y'",
			  |    },
			  |    {
			  |      "x": "'a'",
			  |      "y": "'b'"
			  |    },
			  |    {
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
					.values == List("x")))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List("y")))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List("a")))
		assertTrue(task.vocab.leaves().exists(
			maker => maker.arity == 0 &&
				maker.isInstanceOf[BasicVocabMaker] &&
				maker.asInstanceOf[BasicVocabMaker]
					.apply(Nil, Map().asInstanceOf[Map[String, Any]] :: Nil)
					.values == List("b")))
	}

	@Test def partialSpecStringTest: Unit =
	{
		val task1 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |		 "x": "'hello'",
			  |   	 "y": "''"
			  |    },
			  |    {
			  |      "x": "'hello'",
			  |		 "y": "'olle...'"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.String, task1.predicate.asInstanceOf[PartialOutputPredicate].retType)

		val task2 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |		 "x": "'hello'",
			  |   	 "y": "''"
			  |    },
			  |    {
			  |      "x": "'hello'",
			  |		 "y": "'...lleh'"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.String, task2.predicate.asInstanceOf[PartialOutputPredicate].retType)

		val task3 = SynthesisTask.fromString(
		"""{
		  |  "varNames": ["y"],
		  |  "previous_env": {},
		  |  "envs": [
		  |    {
		  |		 "x": "'hello'",
		  |   	 "y": "''"
		  |    },
		  |    {
		  |      "x": "'hello'",
		  |		 "y": "'o...eh'"
		  |    }
		  |  ]
		  |}""".stripMargin)
		assertEquals(Types.String, task3.predicate.asInstanceOf[PartialOutputPredicate].retType)

		val task4 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |		 "x": "'hello'",
			  |   	 "y": "''"
			  |    },
			  |    {
			  |      "x": "'hello'",
			  |		 "y": "'ol...e...'"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.String, task4.predicate.asInstanceOf[PartialOutputPredicate].retType)
	}

	@Test def partialSpecListTest: Unit =
	{
		val task1 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |		 "x": "[1,2,3,4,5]",
			  |   	 "y": "[]"
			  |    },
			  |    {
			  |      "x": "[1,2,3,4,5]",
			  |		 "y": "[5,4,...]"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.IntList, task1.predicate.asInstanceOf[PartialOutputPredicate].retType)

		val task2 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |		 "x": "[1,2,3,4,5]",
			  |   	 "y": "[]"
			  |    },
			  |    {
			  |      "x": "[1,2,3,4,5]",
			  |		 "y": "[5,4,...,1]"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.IntList, task2.predicate.asInstanceOf[PartialOutputPredicate].retType)

		val task3 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |		 "x": "[1,2,3,4,5]",
			  |   	 "y": "[]"
			  |    },
			  |    {
			  |      "x": "[1,2,3,4,5]",
			  |		 "y": "[...,2,1]"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.IntList, task3.predicate.asInstanceOf[PartialOutputPredicate].retType)

		val task4 = SynthesisTask.fromString(
			"""{
			  |  "varNames": ["y"],
			  |  "previous_env": {},
			  |  "envs": [
			  |    {
			  |		 "x": "[1,2,3,4,5]",
			  |   	 "y": "[]"
			  |    },
			  |    {
			  |      "x": "[1,2,3,4,5]",
			  |		 "y": "[5,...,3,...,1]"
			  |    }
			  |  ]
			  |}""".stripMargin)
		assertEquals(Types.IntList, task4.predicate.asInstanceOf[PartialOutputPredicate].retType)
	}

}
