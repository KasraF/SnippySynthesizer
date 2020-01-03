import Main.RankedProgram
import sygus.SygusFileTask

object Solutions {

  lazy val solutions = scala.io.Source.fromFile("src/test/benchmarks/solutions.txt").getLines().map(line =>
    (line.substring(0,line.indexOf(' ')),line.substring(line.indexOf(' ') + 1))).toList.groupBy(_._1).toList.map(pair => (pair._1,pair._2.map(le => le._2))).toMap

}

object BenchmarksHarness extends App {
  def runBenchmarks(dirname: String, filenameToGoldStandard: String => String): List[String] = for (file <- new java.io.File(dirname).listFiles().toList) yield {
    val programs = Main.synthesize(file.getAbsolutePath)
    val origFilename = filenameToGoldStandard(file.getName)
    val task = new SygusFileTask(scala.io.Source.fromFile(file).mkString)
    val goldStandard = Solutions.solutions.withDefaultValue(Nil)(origFilename).map{ solution =>
      Main.interpret(task,solution)
    }

    file.getName + (if (programs.isEmpty) Console.RED + " NO RESULTS" + Console.RESET else if (goldStandard.exists(gold => gold.code == programs.head.program.code))
      " PASSED"
    else Console.RED + programs.zipWithIndex.dropWhile{case (RankedProgram(tree,rate),idx) => !goldStandard.exists(gold => gold.code == tree.code)}.headOption.map{case (RankedProgram(tree,rate),idx) =>
      " " + tree.code + " " + rate + " (" + idx + ")" + " [" + programs.head.program.code + " " + programs.head.rank + "]"}.getOrElse(" NOT FOUND")  + Console.RESET )

//    if (goldStandard.isEmpty) file.getName + " NO GOLD STANDARD\n" + programs.take(10).map{case RankedProgram(prog, rate) => prog.code + " " + rate}
//      .mkString("\n") + "\n"
//    else file.getName + ":\n" + programs.take(10).map{case RankedProgram(prog, rate) =>
//      prog.code + " " + rate + " " +  goldStandard.map(goldProg => (goldProg.code,ast.SimilarityMetric.compute(prog,goldProg))).maxBy(_._2)}
//        .mkString("\n") + "\n"
  }

  val origBenchmarks: List[String] = Nil //runBenchmarks("src/test/benchmarks/syguscomp",identity)

  val contradictionBenchmarks = runBenchmarks("src/test/benchmarks/modified_benchmarks/contradiction", filename => filename.dropRight(5) + ".sl")

  val garbageBenchmarks = runBenchmarks("src/test/benchmarks/modified_benchmarks/returns_garbage", filename => filename.dropRight(5) + ".sl")

  val tooHardBenchmarks = Nil//runBenchmarks("src/test/benchmarks/too-hard",identity)

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