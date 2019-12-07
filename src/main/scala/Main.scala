import enumeration.InputsValuesManager
import execution.Eval

object Main extends App {
  val filename = "C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\from_2018\\bikes_small.sl"//args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(filename).mkString)
  assert(task.isPBE)
  val enumerator = new enumeration.Enumerator(task.vocab,new InputsValuesManager(task.examples.map(_.input)))
  for (program <- enumerator) {
    val code = program.code
    if (task.examples.forall(ex => Eval(code,ex.input) == ex.output)) {
      println(code)
      sys.exit(0)
    }
  }
}
