import Main.RankedProgram
import ast.ASTNode
import sygus.SygusFileTask

object Solutions {

  lazy val solutions = scala.io.Source.fromFile("src/test/benchmarks/solutions.txt").getLines().map(line =>
    (line.substring(0,line.indexOf(' ')),line.substring(line.indexOf(' ') + 1))).toList.groupBy(_._1).toList.map(pair => (pair._1,pair._2.map(le => le._2))).toMap

}

object BenchmarksHarness extends App {
  def runBenchmarks(dirname: String,
                    filenameToGoldStandard: String => String,
                    resultPrinter: (List[RankedProgram], List[ASTNode]) => String
                   ): List[String] = for (file <- new java.io.File(dirname).listFiles().toList) yield {
    val programs = Main.synthesize(file.getAbsolutePath)
    val origFilename = filenameToGoldStandard(file.getName)
    val task = new SygusFileTask(scala.io.Source.fromFile(file).mkString)
    val goldStandard = Solutions.solutions.withDefaultValue(Nil)(origFilename).map{ solution =>
      Main.interpret(task,solution)
    }
    file.getName + resultPrinter(programs.toList,goldStandard)
  }

  val regularBenchmarkPrinter: (List[RankedProgram], List[ASTNode]) => String = { (programs,goldStandard) =>
    if (programs.isEmpty) Console.RED + " NO RESULTS" + Console.RESET else if (goldStandard.exists(gold => gold.code == programs.head.program.code))
      " PASSED"
    else Console.RED + programs.zipWithIndex.dropWhile{case (RankedProgram(tree,rate),idx) => !goldStandard.exists(gold => gold.code == tree.code)}.headOption.map{case (RankedProgram(tree,rate),idx) =>
      " " + tree.code + " " + rate + " (" + idx + ")" + " [" + programs.head.program.code + " " + programs.head.rank + "]"}.getOrElse(" NOT FOUND")  + Console.RESET
  }
  val origBenchmarks: List[String] = runBenchmarks("src/test/benchmarks/syguscomp",identity,regularBenchmarkPrinter)

  val contradictionBenchmarks = runBenchmarks("src/test/benchmarks/modified_benchmarks/contradiction", filename => filename.dropRight(5) + ".sl",regularBenchmarkPrinter)

  val garbageBenchmarks = runBenchmarks("src/test/benchmarks/modified_benchmarks/returns_garbage", filename => filename.dropRight(5) + ".sl",regularBenchmarkPrinter)

  val tooHardBenchmarks = runBenchmarks("src/test/benchmarks/too-hard",identity, { (programs,goldStandard) =>
    if (goldStandard.isEmpty) " NO GOLD STANDARD\n" + programs.take(10).map{case RankedProgram(prog, rate) => prog.code + " " + rate}
      .mkString("\n") + "\n"
    else ":\n" + programs.take(10).map{case RankedProgram(prog, rate) =>
      prog.code + " " + rate + " " +  goldStandard.map(goldProg => (goldProg.code,ast.SimilarityMetric.compute(prog,goldProg))).maxBy(_._2)}
      .mkString("\n") + "\n"
  })

  println("Original benchmarks:")
  origBenchmarks.foreach(println)
  println
  println("Modified benchmarks:")
  contradictionBenchmarks.foreach(println)
  println
  println("Modified benchmarks (returns garbage):")
  garbageBenchmarks.foreach(println)
  println
  println("Too hard benchmarks:")
  tooHardBenchmarks.foreach(println)
  println
}