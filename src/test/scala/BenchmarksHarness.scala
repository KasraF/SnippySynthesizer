object Solutions {

  lazy val solutions = scala.io.Source.fromFile("src/test/benchmarks/solutions.txt").getLines().map(line =>
    (line.substring(0,line.indexOf(' ')),line.substring(line.indexOf(' ') + 1))).toList.groupBy(_._1).toList.map(pair => (pair._1,pair._2.map(le => le._2))).toMap

}

object BenchmarksHarness extends App {
  def runBenchmarks(dirname: String, filenameToGoldStandard: String => String): List[String] = for (file <- new java.io.File(dirname).listFiles().toList) yield {
    val programs = Main.synthesize(file.getAbsolutePath)
    val origFilename = filenameToGoldStandard(file.getName)
    val goldStandard = Solutions.solutions.withDefaultValue(Nil)(origFilename)

    /*file.getName + (if (programs.isEmpty) Console.RED + " NOT FOUND" + Console.RESET else if (goldStandard.contains(programs.head._1.code))
      " PASSED"
    else Console.RED + programs.zipWithIndex.dropWhile{case ((tree,rate),idx) => !goldStandard.contains(tree.code)}.headOption.map{case ((tree,rate),idx) =>
      " " + tree.code + " " + rate + " (" + idx + ")" + " [" + programs.head._1.code + " " + programs.head._2 + "]"}.getOrElse(" NOT FOUND")  + Console.RESET )*/

    if (goldStandard.isEmpty) file.getName + "NO GOLD STANDARD"
    else file.getName + ":\n" + programs.take(5).map{case (prog, rate) =>
      prog.code + " " + rate + " " +  goldStandard.map(goldProg => (goldProg,ast.SimilarityMetric.compute(prog,goldProg))/*.toDouble / Math.max(prog.terms,goldProg.terms)*/).maxBy(_._2)}
        .mkString("\n") + "\n"
  }

  val origBenchmarks: List[String] = Nil //runBenchmarks("src/test/benchmarks/syguscomp",identity)

  val contradictionBenchmarks = Nil //runBenchmarks("src/test/benchmarks/modified_benchmarks/contradiction", filename => filename.dropRight(5) + ".sl")

  val garbageBenchmarks = Nil //runBenchmarks("src/test/benchmarks/modified_benchmarks/returns_garbage", filename => filename.dropRight(5) + ".sl")

  val tooHardBenchmarks = runBenchmarks("src/test/benchmarks/too-hard",identity)

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