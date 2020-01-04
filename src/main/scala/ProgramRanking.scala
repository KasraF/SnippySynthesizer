package enumeration

import ast.ASTNode

object ProgramRanking {

  def ranking(program: ASTNode, expectedResults: List[Any], parameters: List[String]) : Double = {
    //fitting the data
    val (foundResults,distanceFromNotFound) = program.values.zip(expectedResults).foldLeft((0,0.0)){case ((found,distances),(elem1,elem2)) =>
      if (elem1 == elem2) (found + 1,distances)
      else if (elem1.isInstanceOf[String] && elem2.isInstanceOf[String]) {
        val s1 = elem1.asInstanceOf[String]
        val s2 = elem2.asInstanceOf[String]
        val difference = 1 - levenshtein(s1,s2).toDouble / Math.max(s1.length,s2.length)
        (found,distances + difference)
      }
      else (found,distances)
    }
    val fittingTheData = foundResults.toDouble / expectedResults.length
    val distanceFromData = if (expectedResults.length == foundResults) 0 else distanceFromNotFound / (expectedResults.length - foundResults)
    val relevancy = parameters.count(argName => program.includes(argName)).toDouble / parameters.length
    val height = 1.0 / (program.height + 1)
    val size = 1.0 / program.terms
    3 * fittingTheData + 2 * relevancy + 1 * distanceFromData + size + height
  }
  def levenshtein(str1: String, str2: String): Int = {
    val lenStr1 = str1.length
    val lenStr2 = str2.length

    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)

    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j

    for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j - 1)) 0 else 1

      d(i)(j) = min(
        d(i-1)(j  ) + 1,     // deletion
        d(i  )(j-1) + 1,     // insertion
        d(i-1)(j-1) + cost   // substitution
      )
    }

    d(lenStr1)(lenStr2)
  }

  def min(nums: Int*): Int = nums.min

}
