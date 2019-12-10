import enumeration.InputsValuesManager
import execution.Eval

object Main extends App {
  val filename = "C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\convert-text-to-numbers.sl"
    //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\univ_2_short.sl"
    // "C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\from_2018\\bikes_small.sl"//args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(filename).mkString)
  assert(task.isPBE)
  val oeManager = new InputsValuesManager(task.examples.map(_.input))
  val enumerator = new enumeration.Enumerator(task.vocab,oeManager)
  for ((program,i) <- enumerator.zipWithIndex) {
    val code = program.code
    if (task.examples.forall(ex => Eval(code,ex.input) == ex.output)) {
      println(code)
      sys.exit(0)
    }

    if (i % 1000 == 0) {
      println(i + ": " + code)
    }
  }
}
