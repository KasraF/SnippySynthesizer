//package query

import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}

import collection.JavaConverters._
import Logic.Logic
import SyGuSParser.TermContext
import ast.{VocabFactory, VocabMaker}
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

  lazy val vocab: VocabFactory = {
    val nonTerminals = synthFun.grammarDef().groupedRuleList().asScala.map{nonTerminal =>
      nonTerminal.Symbol().getSymbol.getText -> nonTerminal.sort().identifier().getText
    }.toMap
    val makers = synthFun.grammarDef().groupedRuleList().asScala.flatMap{ nonTerminal =>
      nonTerminal.gTerm().asScala.filter(vocabElem =>
        !vocabElem.bfTerm().bfTerm().isEmpty ||
        vocabElem.bfTerm().identifier() == null ||
        !nonTerminals.contains(vocabElem.bfTerm().identifier().Symbol().getText)
      ).map { vocabElem =>
        if (!vocabElem.bfTerm().bfTerm().isEmpty) //operator or func name
            VocabMaker(
              (SygusFileTask.funcNameToPythonDesc(
                  vocabElem.bfTerm().identifier().Symbol().getText,
                  vocabElem.bfTerm().bfTerm().size(),
                  nonTerminal.sort().identifier().getText) ++
              vocabElem.bfTerm().bfTerm().asScala.map(child => nonTerminals(child.identifier().Symbol().getText))).mkString("|"))
        else if (vocabElem.bfTerm().literal() != null)
          VocabMaker("Literal|0|" + vocabElem.bfTerm().literal().getText + "|" + nonTerminal.sort().identifier().getText)
        else //variable
          VocabMaker("Variable|0|" + vocabElem.bfTerm().identifier().Symbol().getText + "|" + nonTerminal.sort().identifier().getText)
      }
    }
    VocabFactory(makers)
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

  def funcNameToPythonDesc(funcName: String, arity: Int, sort: String): List[Any] =
    //translation table based on the semantics in http://cvc4.cs.stanford.edu/wiki/Strings
    //and src/theory/evaluator.cpp in CVC4
    funcName match {
      case "str.++" => { //theoretically this can be of arity > 2?
        assert(arity == 2 && sort == "String")
        List("BinOperator",arity,"+",sort)
      }
      case "str.len" => {
        assert(arity == 1 && sort == "Int")
        List("FunctionCall", arity, "len", sort)
      }
      case "str.contains" => {
        assert(arity == 2 && sort == "Bool")
        List("BinOperator", arity, "in", sort)
      }
      case "str.indexof" => {
        assert(arity == 3 && sort == "Int")
        List("MethodCall", arity, "find",sort)
      }
      case "str.replace" => {
        //according to src/util/regexp.cpp#L434, this is replace first
        assert(arity == 3 && sort == "String")
        //?.replace(?,?,1) where 1 == first
        ???
      }
      case "str.replaceall" => {
        //?.replace(?,?)
        assert(arity == 3 && sort == "String")
        List("MethodCall", arity, "replaceall",sort)
      }
      case "str.substr" => {
        //?[?:?]
        assert(arity == 3 && sort == "String")
        List("Slicing", arity, " ", sort)
      }
      case "str.prefixof" => {
        assert(arity == 2 && sort == "Bool")
        List("MethodCall",2,"startswith",sort)
      }
      case "str.suffixof" => {
        assert(arity == 2 && sort == "Bool")
        List("MethodCall",2,"endswith",sort)
      }
      case "str.to.int" => {
        assert(arity == 1 && sort == "Int")
        List("FunctionCall",arity,"int",sort)
      }
      case "int.to.str" => {
        assert(arity == 1 && sort == "String")
        List("FunctionCall",arity,"str",sort)
      }
      case "str.at" => {
        assert(arity == 2 && sort == "String")
        List("RandomAccess",arity," ",sort) //actually wants the behavior of x[y:y+1]?
      }
      case _ => List("FunctionCall",arity,funcName,sort)
    }

  def opNameToPython(funcName: String, logic: Logic): String = funcName
}