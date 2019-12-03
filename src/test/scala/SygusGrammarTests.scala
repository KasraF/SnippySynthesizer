import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import java.io.{File, FilenameFilter}

import org.antlr.v4.runtime.{BufferedTokenStream,CharStreams}
import collection.JavaConverters._



class SygusGrammarTests extends JUnitSuite{
//  @Test def runDir: Unit = {
//    val dir = new File("C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track")
//    val files = dir.listFiles(new FilenameFilter {
//      override def accept(dir: File, name: String): Boolean = name.endsWith(".sl")
//    })
//    files.foreach{f =>
//      val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromFileName(f.getAbsolutePath))))
//      println(parser.syGuS().cmd().asScala.map(c => c.getText).mkString(","))
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
}
