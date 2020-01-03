import enumeration.ProgramRanking
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite

class DistanceMetricTests  extends JUnitSuite{
  @Test def editDistanceALot: Unit = {
    val t0 = System.nanoTime()
    for (i <- 0 to 10000) {
      ProgramRanking.levenshtein("akhfahgladkhglkafdjhglkadfg" + i, i + "akdflkajfdhva9dflkjvadflkjv")
    }
    val t1 = System.nanoTime()
    println(s"${t1-t0}ns")
  }

}
