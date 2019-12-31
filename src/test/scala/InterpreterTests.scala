import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite

class InterpreterTests extends JUnitSuite {
  @Test def stringProg(): Unit = {
    val progStr = "(ite (str.prefixof col1 col2) col1 (str.substr col1 0 (str.indexof col2 \" \" (+ 1 1))))"
    val task = new SygusFileTask(slFileContent)
    val parsed = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(progStr)))).bfTerm()
    val visitor = new ASTGenerator(task)
    val ast = visitor.visit(parsed)
    assert(ast != null)
  }

  @Test def intProg(): Unit = {
    val progStr = "(str.indexof col2 (str.++ col1 col2) 0)"
    val task = new SygusFileTask(slFileContent)
    val parsed = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(progStr)))).bfTerm()
    val visitor = new ASTGenerator(task)
    val ast = visitor.visit(parsed)
    assert(ast != null)
  }

  @Test def parseSyGuS(): Unit = {
    assert(!parseBenchmarks("src/test/benchmarks/syguscomp", identity))
  }

  @Test def parseContradiction(): Unit = {
    assert(!parseBenchmarks("src/test/benchmarks/modified_benchmarks/contradiction", filename => filename.dropRight(5) + ".sl"))
  }

  @Test def parseGarbage(): Unit = {
    assert(!parseBenchmarks("src/test/benchmarks/modified_benchmarks/returns_garbage", filename => filename.dropRight(5) + ".sl"))
  }

  @Test def parseTooHard(): Unit = {
    assert(!parseBenchmarks("src/test/benchmarks/too-hard",identity))
  }

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

  def parseBenchmarks(dirname: String, filenameToGoldStandard: String => String): Boolean = {
    var failed: Boolean = false
    for {
      file <- new java.io.File(dirname).listFiles().toList
      origFilename = filenameToGoldStandard(file.getName)
      gold <- Solutions.solutions.withDefaultValue(Nil)(origFilename)
      prog = Main.interpret(file.getAbsolutePath, gold)
    } {
      prog match {
        case None => {
          println(s"Failed to parse solution for $origFilename: $gold")
          failed = true
        }
        case Some(p) =>
          println(s"OK: ${p.code}")
      }
    }
    failed
  }

}
