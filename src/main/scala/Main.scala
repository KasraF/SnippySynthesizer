import enumeration.InputsValuesManager
//import enumeration.InputsValuesManager
//import execution.Eval

object Main extends App {
  val filename = "C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\get-first-word.sl" //works
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\extract-text-between-parentheses.sl" //works
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\replace-one-character-with-another.sl" //works
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\37281007.sl" //works
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\get-last-name-from-name.sl" //works
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\count-total-characters-in-a-cell.sl" //works!
  //C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\count-line-breaks-in-cell.sl" //works!
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\remove-text-by-matching.sl" //works!
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\convert-numbers-to-text.sl" //works!
  //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\euphony\\convert-text-to-numbers.sl" //works!
  //"C:\\utils\\sygus-solvers\\SyGuS-Comp17\\PBE_Strings_Track\\univ_2_short.sl"
   //"C:\\utils\\sygus-solvers\\PBE_SLIA_Track\\from_2018\\bikes_small.sl"//args(0)
  val task = new SygusFileTask(scala.io.Source.fromFile(filename).mkString)
  assert(task.isPBE)
  val oeManager = new InputsValuesManager()
  val enumerator = new enumeration.Enumerator(task.vocab,oeManager,task.examples.map(_.input))
  for ((program,i) <- enumerator.zipWithIndex) {
    if (task.examples.zip(program.values).forall(pair => pair._1.output == pair._2)) {
      println(program.code)
      sys.exit(0)
    }

    if (i % 1000 == 0) {
      println(i + ": " + program.code)
    }
  }
}
