import ast.Types
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite
import org.junit.Assert._
import sygus.PythonPBETask

class SynthesisTaskTests  extends JUnitSuite{
  @Test def inferEmptyListTypeFromOtherExamples: Unit = {
    val task = PythonPBETask.fromString(
      """{
        |  "varName": "l",
        |  "envs": [
        |    {
        |      "l": "[]"
        |    },
        |    {
        |      "l": "[1,2,3,1]"
        |    }
        |  ]
        |}""".stripMargin)
    assertEquals(Types.IntList, task.returnType)

    val task2 = PythonPBETask.fromString(
      """{
        |  "varName": "l",
        |  "envs": [
        |    {
        |      "l": "[]"
        |    },
        |    {
        |      "l": "['a','b','3','1']"
        |    }
        |  ]
        |}""".stripMargin)
    assertEquals(Types.StringList, task2.returnType)
  }

  @Test def listMapMismatchShouldFail: Unit = {
    val task = PythonPBETask.fromString("""{
                                          |  "varName": "filters",
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
    assertEquals(Types.Unknown,task.returnType)
  }

  @Test def inferEmptyMapTypeFromOtherExamples: Unit = {
    val task = PythonPBETask.fromString("""{
                                          |  "varName": "filters",
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
    assertTrue(task.returnType.isInstanceOf[Types.Map])
    assertEquals(Types.String,task.returnType.asInstanceOf[Types.Map].keyType)
    assertEquals(Types.Int,task.returnType.asInstanceOf[Types.Map].valType)
  }

  @Test def onlyEmptyList: Unit = {
    val task = PythonPBETask.fromString(
      """{
        |  "varName": "l",
        |  "envs": [
        |    {
        |      "l": "[]"
        |    }
        |  ]
        |}""".stripMargin)
    assertEquals(Types.StringList, task.returnType)
  }

  @Test def onlyEmptyMap: Unit = {
    val task = PythonPBETask.fromString("""{
                                          |  "varName": "filters",
                                          |  "envs": [
                                          |    {
                                          |      "counts": "{'a': 2, 'b': 2, 'c': 1, 'd': 1, 'e': 1}",
                                          |      "filters": "{}",
                                          |    }
                                          |  ]
                                          |}""".stripMargin)
    assertTrue(task.returnType.isInstanceOf[Types.Map])
    assertEquals(Types.String,task.returnType.asInstanceOf[Types.Map].keyType)
    assertEquals(Types.Int,task.returnType.asInstanceOf[Types.Map].valType)
  }

  @Test def emptyMapInInputVal: Unit = {
    val task = PythonPBETask.fromString("""{
                                          |  "varName": "filters",
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
    val varType = task.parameters.find(kv => kv._1 ==  "counts").get._2.asInstanceOf[Types.Map]
    assertEquals(Types.String,varType.keyType)
    assertEquals(Types.Int,varType.valType)
  }

  @Test def emptyListInInputVal: Unit = {
    val task = PythonPBETask.fromString("""{
                                          |  "varName": "filters",
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
    assertEquals(Types.IntList, task.parameters.find(kv => kv._1 ==  "counts").get._2)
  }

  @Test def onlyEmptyListInInputVal: Unit = {
    val task = PythonPBETask.fromString("""{
                                          |  "varName": "filters",
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
    assertEquals(Types.StringList, task.parameters.find(kv => kv._1 ==  "counts").get._2)
  }
}
