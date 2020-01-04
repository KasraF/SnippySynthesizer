package sygus

import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}

import collection.JavaConverters._
import Logic.Logic
import sygus.SyGuSParser.TermContext
import ast._
import ast.Types.Types


object Logic extends Enumeration{
  type Logic = Value
  val LIA, SLIA, BV, SLIA_PBE = Value
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
  val functionReturnType = Types.withName(synthFun.sort().identifier().getText)
  val functionParameters = synthFun.sortedVar().asScala.map(svar => (svar.Symbol().getText -> Types.withName(svar.sort().identifier().getText))).toList

  val isPBE: Boolean = {
    val constraints = parsed.cmd().asScala.filter(cmd => cmd.getChild(1) != null && cmd.getChild(1).getText == "constraint").map(_.term())
    !constraints.isEmpty && constraints.forall(constraint => SygusFileTask.isExample(constraint,functionName))
  }
  lazy val examples: List[Example] = {
    val constraints = parsed.cmd().asScala.filter(cmd => cmd.getChild(1) != null && cmd.getChild(1).getText == "constraint").map(_.term())
    constraints.map(constraint => SygusFileTask.exampleFromConstraint(constraint,functionName,functionReturnType,functionParameters)).toList.distinct
  }

  val vocab: VocabFactory = {
    val nonTerminals = synthFun.grammarDef().groupedRuleList().asScala.map{nonTerminal =>
      nonTerminal.Symbol().getSymbol.getText -> Types.withName(nonTerminal.sort().identifier().getText)
    }.toMap
    val makers = synthFun.grammarDef().groupedRuleList().asScala.flatMap{ nonTerminal =>
      nonTerminal.gTerm().asScala.filter(vocabElem =>
        !vocabElem.bfTerm().bfTerm().isEmpty ||
        vocabElem.bfTerm().identifier() == null ||
        !nonTerminals.contains(vocabElem.bfTerm().identifier().Symbol().getText)
      ).map { vocabElem =>
        SygusFileTask.makeVocabMaker(vocabElem, Types.withName(nonTerminal.sort().identifier().getText),nonTerminals)
//        if (!) //operator or func name
//              SygusFileTask.makeVocabMaker(
//                  ,
//                  ,
//                  nonTerminal.sort().identifier().getText,
//                  vocabElem.bfTerm().bfTerm().asScala.map(child => nonTerminals(child.identifier().Symbol().getText))
//              ).mkString("|")

      }
    }.sortBy(maker => if (maker.arity > 0 && maker.returnType.toString == functionReturnType.toString) -1 else 0)
    VocabFactory(makers.toList)
  }

  def fit(program: ASTNode): (Int, Int) = {
    val expectedResults = examples.map(_.output)
    val k = program.values.zip(expectedResults).count(pair => pair._1 == pair._2)
    val n = expectedResults.length
    (k, n)
  }
}

object SygusFileTask{
  def makeVocabMaker(vocabElem: SyGuSParser.GTermContext, retType: Types, nonTerminalTypes: Map[String,Types]): VocabMaker =
    if (vocabElem.bfTerm().literal() != null) //literal
      retType match {
        case Types.Int => {
          val lit = vocabElem.bfTerm().literal().Numeral().getText.toInt
          new VocabMaker {
            override val arity: Int = 0
            override val childTypes: List[Types] = Nil
            override val returnType: Types = retType
            override val head: String = lit.toString

            override def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode = new IntLiteral(lit, contexts.length)
          }
        }
        case Types.String => {
          val lit = literalToAny(vocabElem.bfTerm().literal(),retType).asInstanceOf[String]
          new VocabMaker {
            override val arity: Int = 0
            override val childTypes: List[Types] = Nil
            override val returnType: Types = retType
            override val head: String = '"' + lit + '"'

            override def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode = new StringLiteral(lit, contexts.length)
          }
        }
        case Types.Bool => {
          val lit = vocabElem.bfTerm().literal().BoolConst().getSymbol.getText.toBoolean
          new VocabMaker {
            override val arity: Int = 0
            override val childTypes: List[Types] = Nil
            override val returnType: Types = retType
            override val head: String = lit.toString

            override def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode = new BoolLiteral(lit, contexts.length)
          }
        }
      }
    else if (vocabElem.bfTerm().identifier() != null && vocabElem.bfTerm().bfTerm().isEmpty)
      if (retType == Types.Int && vocabElem.bfTerm().identifier().getText.matches("-[1-9][0-9]*")) {//actually a negative number
        val lit = vocabElem.bfTerm().identifier().getText.toInt
        new VocabMaker {
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
          override val returnType: Types = retType
          override val head: String = lit.toString

          override def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode = new IntLiteral(lit, contexts.length)
        }
      }
      else {
      val varname = vocabElem.bfTerm().identifier().Symbol().getText
      retType match {
        case Types.Int => new VocabMaker {
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
          override val returnType: Types = retType
          override val head: String = varname

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode = new IntVariable(varname,contexts)
        }
        case Types.String => new VocabMaker {
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
          override val returnType: Types = retType
          override val head: String = varname

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode = new StringVariable(varname,contexts)
        }
        case Types.Bool => new VocabMaker {
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
          override val returnType: Types = retType
          override val head: String = varname

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode = new BoolVariable(varname,contexts)
        }
      }
    }
    else {
      val funcName = vocabElem.bfTerm().identifier().Symbol().getText
      val arity = vocabElem.bfTerm().bfTerm().size()
      val childrenTypes = vocabElem.bfTerm().bfTerm().asScala.map(child => nonTerminalTypes(child.identifier().Symbol().getText)).toList

      (funcName,retType,arity) match {
        case ("str.++",Types.String,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new StringConcat(children(0).asInstanceOf[StringNode],children(1).asInstanceOf[StringNode])
        }
        case ("str.replace",Types.String,3) => new VocabMaker {
          override val arity: Int = 3
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new StringReplace(children(0).asInstanceOf[StringNode],children(1).asInstanceOf[StringNode],children(2).asInstanceOf[StringNode])
        }
        case ("str.at",Types.String,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new StringAt(children(0).asInstanceOf[StringNode],children(1).asInstanceOf[IntNode])
        }
        case ("int.to.str",Types.String,1) => new VocabMaker {
          override val arity: Int = 1
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new IntToString(children(0).asInstanceOf[IntNode])
        }
        case ("ite",Types.String,3) => new VocabMaker {
          override val arity: Int = 3
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new StringITE(children(0).asInstanceOf[BoolNode],children(1).asInstanceOf[StringNode],children(2).asInstanceOf[StringNode])
        }
        case ("str.substr",Types.String,3) => new VocabMaker {
          override val arity: Int = 3
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new Substring(children(0).asInstanceOf[StringNode],children(1).asInstanceOf[IntNode],children(2).asInstanceOf[IntNode])
        }
        case ("+",Types.Int,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new IntAddition(children(0).asInstanceOf[IntNode],children(1).asInstanceOf[IntNode])
        }
        case ("-", Types.Int,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new IntSubtraction(children(0).asInstanceOf[IntNode],children(1).asInstanceOf[IntNode])
        }
        case ("str.len",Types.Int,1) => new VocabMaker {
          override val arity: Int = 1
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new StringLength(children(0).asInstanceOf[StringNode])
        }
        case ("str.to.int",Types.Int,1) => new VocabMaker {
          override val arity: Int = 1
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new StringToInt(children(0).asInstanceOf[StringNode])
        }
        case ("ite",Types.Int,3) => new VocabMaker {
          override val arity: Int = 3
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new IntITE(children(0).asInstanceOf[BoolNode],children(1).asInstanceOf[IntNode],children(2).asInstanceOf[IntNode])
        }
        case ("str.indexof",Types.Int,3) => new VocabMaker {
          override val arity: Int = 3
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new IndexOf(children(0).asInstanceOf[StringNode],children(1).asInstanceOf[StringNode],children(2).asInstanceOf[IntNode])
        }
        case ("<=", Types.Bool,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new IntLessThanEq(children(0).asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
        }
        case ("=",Types.Bool,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new IntEquals(children(0).asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
        }
        case ("str.prefixof", Types.Bool,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new PrefixOf(children(0).asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
        }
        case ("str.suffixof", Types.Bool,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new SuffixOf(children(0).asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
        }
        case ("str.contains", Types.Bool,2) => new VocabMaker {
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
          override val head: String = funcName

          override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
            new Contains(children(0).asInstanceOf[StringNode],children(1).asInstanceOf[StringNode])
        }
      }
    }

  def literalToAny(literal: SyGuSParser.LiteralContext, returnType: Types): Any = returnType match {
    case Types.String => literal.StringConst().getSymbol.getText.drop(1).dropRight(1)//unescape
    case Types.Int => literal.Numeral().getText.toInt
    case Types.Bool => literal.BoolConst().toString.toBoolean
  }

  def exampleFromConstraint(constraint: TermContext, functionName: String, retType: Types, parameters: Seq[(String,Types)]): Example = {
    val lhs = constraint.term(0)
    val rhs = constraint.term(1)
    if (isFuncApp(lhs,functionName) && rhs.literal() != null)
      Example(parameters.zip(lhs.term.asScala).map(kv => kv._1._1 -> literalToAny(kv._2.literal(),kv._1._2).asInstanceOf[AnyRef]).toMap,literalToAny(rhs.literal(),retType))
    else if (lhs.literal != null && isFuncApp(rhs,functionName))
      Example(parameters.zip(rhs.term.asScala).map(kv => kv._1._1 -> literalToAny(kv._2.literal(),kv._1._2).asInstanceOf[AnyRef]).toMap,literalToAny(lhs.literal(),retType))
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