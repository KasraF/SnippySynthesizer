object Solutions {

  lazy val solutions = scala.io.Source.fromFile("src/test/benchmarks/solutions.txt").getLines().map(line =>
    (line.substring(0,line.indexOf(' ')),line.substring(line.indexOf(' ') + 1))).toList.groupBy(_._1).toList.map(pair => (pair._1,pair._2.map(le => le._2))).toMap

}

object BenchmarksHarness extends App {
  val res = for (originalFile <- Solutions.solutions.keys.map(k => new java.io.File("src/test/benchmarks/syguscomp/" + k))) yield {//new java.io.File("src/test/benchmarks/syguscomp").listFiles()) yield {
    val programs = Main.synthesize(originalFile.getAbsolutePath)
    val goldStandard = Solutions.solutions.withDefaultValue(Nil)(originalFile.getName)

    originalFile.getName + (if (goldStandard.contains(programs.head._1.code))
      " PASSED"
    else programs.dropWhile{case (tree,rate) => !goldStandard.contains(tree.code)}.headOption.map{case (tree,rate) =>
      " " + tree.code + " " + rate + "(" + programs.head._1.code + " " + programs.head._2 + ")"}.getOrElse(" NOT FOUND"))


  }

  res.foreach(println)
}