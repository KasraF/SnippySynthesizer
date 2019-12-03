//package query

import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}

import collection.JavaConverters._
import Logic.Logic
import SyGuSParser.TermContext
import execution.{Eval, Example}
import org.python.core.{PyInteger, PyObject, PyString, PyType}


object Logic extends Enumeration{
  type Logic = Value
  val LIA, SLIA, BV, SLIA_PBE = Value
}

object SortTypes extends Enumeration{
  type SortTypes = Value
  val String,Int, Bool = Value
}

class SygusFileTask(content: String) {
  private val parsed = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(content)))).syGuS()
  private val synthFun = parsed.cmd().asScala.filter(cmd => cmd.getChild(1) != null && cmd.getChild(1).getText == "synth-fun").head

  val logic: Logic = {
    val setLogic = parsed.cmd().asScala.filter(cmd => cmd.smtCmd() != null).map(_.smtCmd()).filter(cmd => cmd.getChild(1).getText == "set-logic")
    assert(setLogic.length == 1)
    Logic.withName(setLogic.head.Symbol().getText)
  }
  val functionName = synthFun.Symbol(0).getSymbol.getText
  val functionReturnType = SortTypes.withName(synthFun.sort().identifier().getText)
  val functionParameters = synthFun.sortedVar().asScala.map(svar => (svar.Symbol().getText -> SortTypes.withName(svar.sort().identifier().getText)))

  val isPBE: Boolean = {
    val constraints = parsed.cmd().asScala.filter(cmd => cmd.getChild(1) != null && cmd.getChild(1).getText == "constraint").map(_.term())
    !constraints.isEmpty && constraints.forall(constraint => SygusFileTask.isExample(constraint,functionName))
  }
  lazy val examples: List[Example] = {
    val constraints = parsed.cmd().asScala.filter(cmd => cmd.getChild(1) != null && cmd.getChild(1).getText == "constraint").map(_.term())
    constraints.map(constraint => SygusFileTask.exampleFromConstraint(constraint,functionName,functionReturnType,functionParameters)).toList
  }
}

object SygusFileTask{
  def toPyType(literal: SyGuSParser.LiteralContext, returnType: SortTypes.SortTypes): PyObject = returnType match {
    case SortTypes.String => Eval(literal.StringConst().toString,Map.empty)
  }

  def exampleFromConstraint(constraint: TermContext, functionName: String, retType: SortTypes.SortTypes, parameters: Seq[(String,SortTypes.SortTypes)]): Example = {
    val lhs = constraint.term(0)
    val rhs = constraint.term(1)
    if (isFuncApp(lhs,functionName) && rhs.literal() != null)
      Example(parameters.zip(lhs.term.asScala).map(kv => kv._1._1 -> toPyType(kv._2.literal(),kv._1._2).asInstanceOf[AnyRef]).toMap,toPyType(rhs.literal(),retType))
    else if (lhs.literal != null && isFuncApp(rhs,functionName))
      Example(parameters.zip(rhs.term.asScala).map(kv => kv._1._1 -> toPyType(kv._2.literal(),kv._1._2).asInstanceOf[AnyRef]).toMap,toPyType(lhs.literal(),retType))
    else ???
  }
  def isFuncApp(context: SyGuSParser.TermContext,functionName: String): Boolean = {
    context.identifier() != null && context.identifier().Symbol().getText == functionName
  }
  def isExample(constraint: TermContext,functionName: String): Boolean = {
    if (constraint.identifier() != null && constraint.identifier().getText != "=") false
    else {
      val lhs = constraint.term(0)
      val rhs = constraint.term(1)
      (isFuncApp(lhs,functionName) && rhs.literal() != null) || (lhs.literal != null && isFuncApp(rhs,functionName))
    }
  }
}