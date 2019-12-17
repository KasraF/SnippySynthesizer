import org.junit.Test
import org.scalatestplus.junit.JUnitSuite
import org.junit.Assert._
import java.io.{File, FilenameFilter}

import ast.Types
import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}

import collection.JavaConverters._



class SygusGrammarTests extends JUnitSuite{
//  @Test def runDir: Unit = {
//    val dirs = List("C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track",
//                      "C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony",
//                      "C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\from_2018").map(new File(_))
//    val files = dirs.flatMap(d => d.listFiles(new FilenameFilter {
//      override def accept(dir: File, name: String): Boolean = name.endsWith(".sl")
//    }))
//    //val files = List(new File("C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\30732554.sl"))
//    files.foreach{f =>
//      //val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromFileName(f.getAbsolutePath))))
//      println(f.getAbsoluteFile)
//      val task = new SygusFileTask(scala.io.Source.fromFile(f).mkString)
//      println(task.vocab.leavesMakers.length)
//      println(task.vocab.nodeMakers.length)
//    }
//
//  }

  @Test def runParser1(): Unit = {
    val slFileContent = """(set-logic SLIA)
                          |(declare-var name String)
                          |
                          |(constraint (= (f "938-242-504") "938"))
                          |(constraint (= (f "308-916-545") "308"))
                          |(constraint (= (f "623-599-749") "623"))
                          |(constraint (= (f "981-424-843") "981"))
                          |(constraint (= (f "118-980-214") "118"))
                          |(constraint (= (f "244-655-094") "244"))
                          |
                          |(constraint (= (f "123-655-094") "123 "))
                          |
                          |(check-synth)""".stripMargin
    val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(slFileContent))))
    val sygus = parser.syGuS()
    assertEquals(10, sygus.cmd().size())
    assertEquals(List("set-logic","declare-var","constraint","constraint","constraint","constraint","constraint","constraint","constraint","check-synth"),sygus.cmd.asScala.map(c => if (c.children.size() == 1) c.getChild(0).getChild(1).getText else c.getChild(1).getText))
  }

  @Test def runParserOnGrammar(): Unit = {
    val slFileContent = """(synth-fun f ((name String)) String
                          |    ((Start String (ntString))
                          |     (ntString String (name " "
                          |                       (str.++ ntString ntString)
                          |                       (str.replace ntString ntString ntString)
                          |                       (str.at ntString ntInt)
                          |                       (int.to.str ntInt)
                          |                       (str.substr ntString ntInt ntInt)))
                          |      (ntInt Int (0 1 2 3 4 5
                          |                  (+ ntInt ntInt)
                          |                  (- ntInt ntInt)
                          |                  (str.len ntString)
                          |                  (str.to.int ntString)
                          |                  (str.indexof ntString ntString ntInt)))
                          |      (ntBool Bool (true false
                          |                    (str.prefixof ntString ntString)
                          |                    (str.suffixof ntString ntString)
                          |                    (str.contains ntString ntString)))))""".stripMargin
    val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(slFileContent))))
    val sygus = parser.syGuS()
    assertEquals(1, sygus.cmd().size())
  }

  @Test def parseWholeTask: Unit = {
    val slFileContent = """; commutative function
                          |
                          |(set-logic LIA)
                          |
                          |(synth-fun comm ((x Int) (y Int)) Int
                          |    ((Start Int (x
                          |                 y
                          |                 (+ Start Start)
                          |                 (- Start Start)
                          |                 ))
                          |          ))
                          |
                          |(declare-var x Int)
                          |(declare-var y Int)
                          |
                          |(constraint (= (comm x y) (comm y x)))
                          |
                          |
                          |(check-synth)
                          |
                          |; (+ x y) is a valid solution""".stripMargin
    val task = new SygusFileTask(slFileContent)
    assertEquals(Logic.LIA,task.logic)
    assertEquals("comm",task.functionName)
    assertEquals(Types.Int,task.functionReturnType)
    assertEquals(List("x" -> Types.Int,"y" -> Types.Int),task.functionParameters)
    assertFalse(task.isPBE)
  }

  @Test def parseWholeTaskPBE: Unit = {
    val slFileContent = """(set-logic SLIA)
                          |
                          |(synth-fun f ((name String)) String
                          |    ((Start String (ntString))
                          |     (ntString String (name " " "."
                          |                       (str.++ ntString ntString)
                          |                       (str.replace ntString ntString ntString)
                          |                       (str.at ntString ntInt)
                          |                       (str.substr ntString ntInt ntInt)))
                          |      (ntInt Int (0 1 2
                          |                  (+ ntInt ntInt)
                          |                  (- ntInt ntInt)
                          |                  (str.len ntString)
                          |                  (str.indexof ntString ntString ntInt)))
                          |      (ntBool Bool (true false
                          |                    (str.prefixof ntString ntString)
                          |                    (str.suffixof ntString ntString)))))
                          |
                          |
                          |(declare-var name String)
                          |
                          |(constraint (= (f "Nancy FreeHafer") "N.F."))
                          |(constraint (= (f "Andrew Cencici") "A.C."))
                          |(constraint (= "J.K." (f "Jan Kotas")))
                          |(constraint (= (f "Mariya Sergienko") "M.S."))
                          |
                          |(check-synth)
                          |""".stripMargin
    val task = new SygusFileTask(slFileContent)
    assertEquals(Logic.SLIA,task.logic)
    assertEquals("f",task.functionName)
    assertEquals(Types.String,task.functionReturnType)
    assertEquals(List("name" -> Types.String), task.functionParameters)
    assertTrue(task.isPBE)
    assertEquals(4,task.examples.length)
    assertEquals(Example(Map("name" -> "Nancy FreeHafer"),"N.F."),task.examples.head)
    assertEquals(Example(Map("name" -> "Jan Kotas"), "J.K."),task.examples(2))
    assertEquals(8, task.vocab.leaves.length)
    assertEquals(10, task.vocab.nonLeaves.length)
    assertEquals(List("name","\" \"","\".\"","0","1","2","true","false"),task.vocab.leaves.map(_.apply(Nil,task.examples.map(_.input)).code).toList)
  }

  @Test def equalityTest = {
    val slFileContent = """(set-logic SLIA)
                          |(synth-fun f ((col1 String) (col2 String)) String
                          |    ((Start String (ntString))
                          |     (ntString String (col1 col2 " " "," "USA" "PA" "CT" "CA" "MD" "NY"
                          |                       (str.++ ntString ntString)
                          |                       (str.replace ntString ntString ntString)
                          |                       (str.at ntString ntInt)
                          |                       (ite ntBool ntString ntString)
                          |                       (str.substr ntString ntInt ntInt)))
                          |      (ntInt Int (0 1 2
                          |                  (+ ntInt ntInt)
                          |                  (- ntInt ntInt)
                          |                  (str.len ntString)
                          |                  (str.indexof ntString ntString ntInt)))
                          |      (ntBool Bool (true false
                          |                    (str.prefixof ntString ntString)
                          |                    (str.suffixof ntString ntString)))))
                          |
                          |
                          |(declare-var col1 String)
                          |(declare-var col2 String)
                          |
                          |(constraint (= (f "University of Pennsylvania" "Phialdelphia, PA, USA")
                          |                  "University of Pennsylvania, Phialdelphia, PA, USA"))
                          |(constraint (= (f "Cornell University" "Ithaca, New York, USA")
                          |                  "Cornell University, Ithaca, New York, USA"))
                          |(constraint (= (f "Penn" "Philadelphia, PA, USA")
                          |                  "Penn, Philadelphia, PA, USA"))
                          |(constraint (= (f "University of Michigan" "Ann Arbor, MI, USA")
                          |                  "University of Michigan, Ann Arbor, MI, USA"))
                          |
                          |(check-synth)""".stripMargin
    val task = new SygusFileTask(slFileContent)
    assertEquals(Map("col1" -> "University of Pennsylvania","col2" -> "Phialdelphia, PA, USA"),task.examples.head.input)
    assertEquals("University of Pennsylvania, Phialdelphia, PA, USA",task.examples.head.output)

    val code = "(col1 + \",\") + (\" \" + col2)"

  }

  @Test def intsInVocab(): Unit = {
    val vocabFileContent = """(set-logic SLIA)
                             |(synth-fun f ((col1 String) (col2 String)) String
                             |    ((Start String (ntString))
                             |     (ntString String (col1 col2))
                             |      (ntInt Int ((+ ntInt ntInt)
                             |                  (- ntInt ntInt)))))""".stripMargin
    val task = new SygusFileTask(vocabFileContent)
    val children = task.vocab.leaves().map(l => l(Nil,Nil)).toList
    assertEquals(2,children.length)
    assertTrue(children.forall(c => c.nodeType == Types.String))
    assertTrue(task.vocab.nonLeaves().forall(m => !m.canMake(children)))
  }

  @Test def sortVocabByReturnType(): Unit = {
    val vocabFileContent1 = """(set-logic SLIA)
                             |(synth-fun f ((col1 String) (col2 String)) String
                             |    ((Start String (ntString))
                             |     (ntString String (col1 col2 (str.++ ntString ntString)))
                             |      (ntInt Int ((+ ntInt ntInt)
                             |                  (- ntInt ntInt)))))""".stripMargin
    val task1 = new SygusFileTask(vocabFileContent1)
    assertEquals(List(Types.String, Types.Int, Types.Int),task1.vocab.nonLeaves().map(_.returnType).toList)

    val vocabFileContent2 = """(set-logic SLIA)
                              |(synth-fun f ((col1 String) (col2 String)) Int
                              |    ((Start Int (ntInt))
                              |     (ntString String (col1 col2 (str.++ ntString ntString)))
                              |      (ntInt Int ((+ ntInt ntInt)
                              |                  (- ntInt ntInt)))))""".stripMargin
    val task2 = new SygusFileTask(vocabFileContent2)
    assertEquals(List(Types.Int,Types.Int,Types.String),task2.vocab.nonLeaves().map(_.returnType).toList)
  }

  @Test def filterExampleRepetition: Unit = {
    val grammarFile = """(set-logic SLIA)
                        |
                        |(synth-fun f ((name String)) String
                        |    ((Start String (ntString))
                        |     (ntString String (name " "
                        |                       (str.substr ntString ntInt ntInt)))
                        |      (ntInt Int (0 1 2 3 4 5
                        |                  (str.indexof ntString ntString ntInt)))
                        |      (ntBool Bool (true false
                        |                    (str.contains ntString ntString)))))
                        |
                        |(declare-var name String)
                        |
                        |(constraint (= (f "938-242-504") "504"))
                        |(constraint (= (f "938-242-504") "504"))
                        |(constraint (= (f "938-242-504") "504"))
                        |(constraint (= (f "308-916-545") "545"))
                        |(constraint (= (f "308-916-545") "545"))
                        |(constraint (= (f "308-916-545") "545"))
                        |(constraint (= (f "623-599-749") "749"))
                        |(constraint (= (f "623-599-749") "749"))
                        |(constraint (= (f "623-599-749") "749"))
                        |(constraint (= (f "981-424-843") "843"))
                        |(constraint (= (f "981-424-843") "843"))
                        |(constraint (= (f "981-424-843") "843"))""".stripMargin
    val task = new SygusFileTask(grammarFile)
    assertTrue(task.isPBE)
    assertEquals(4, task.examples.length)
    assertEquals(Example(Map("name" -> "938-242-504"),"504"),task.examples(0))
    assertEquals(Example(Map("name" -> "308-916-545"),"545"),task.examples(1))
    assertEquals(Example(Map("name" -> "623-599-749"),"749"),task.examples(2))
    assertEquals(Example(Map("name" -> "981-424-843"),"843"),task.examples(3))

  }
}
