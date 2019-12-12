import ast._
import org.antlr.v4.runtime.{BufferedTokenStream, CharStreams}
import org.junit.Test
import org.junit.Assert._
import org.scalatestplus.junit.JUnitSuite

class VocabTests  extends JUnitSuite{
  def readVocabElem(elemLine: String) = {
    val parser = new SyGuSParser(new BufferedTokenStream(new SyGuSLexer(CharStreams.fromString(elemLine))))
    val ruleList = parser.groupedRuleList()
    assert(ruleList.gTerm().size() == 1)
    val res = ruleList.gTerm(0)
    assert(res != null)
    (res,ruleList.sort().identifier().getText)
  }
  val nonTerminals = Map("ntBool" -> Types.Bool,"ntInt" -> Types.Int, "ntString" -> Types.String)

  @Test def boolLiteralMaker(): Unit =  {
    val vocabLine = "(ntBool Bool (false))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    val node = maker(Nil,Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[BoolLiteral])
    assertEquals(List(false), node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def intLiteralMaker(): Unit =  {
    val vocabLine = "(ntInt Int (-1))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    val node = maker(Nil,Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntLiteral])
    assertEquals(List(-1),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def stringLiteralMaker(): Unit = {
    val vocabLine = "(ntString String (\" \"))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.String,maker.returnType)
    val node = maker(Nil,Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringLiteral])
    assertEquals(List(" "),node.values)
    assertEquals(Types.String,node.nodeType)
  }

  @Test def boolVarMaker: Unit = {
    val vocabLine = "(ntBool Bool (b))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    val node = maker(Nil,Map("b" -> false) :: Map("b" -> true) :: Nil)
    assertTrue(node.isInstanceOf[BoolVariable])
    assertEquals(List(false,true),node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def intVarMaker: Unit = {
    val vocabLine = "(ntInt Int (_arg0_))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    val node = maker(Nil,Map("_arg0_" -> 0) :: Map("_arg0_" -> -88) :: Nil)
    assertTrue(node.isInstanceOf[IntVariable])
    assertEquals(List(0,-88),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def stringVarMaker: Unit = {
    val vocabLine = "(ntString String (str))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(0,maker.arity)
    assertEquals(Types.String,maker.returnType)
    val node = maker(Nil,Map("str" -> "") :: Map("str" -> "abc") :: Nil)
    assertTrue(node.isInstanceOf[StringVariable])
    assertEquals(List("","abc"),node.values)
    assertEquals(Types.String,node.nodeType)
  }

  @Test def intToStringMaker: Unit = {
    val vocabLine = "(ntString String ((int.to.str ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(1,maker.arity)
    assertEquals(Types.String,maker.returnType)
    assertEquals(Types.Int,maker.childTypes(0))
    val node = maker(List(new IntLiteral(100,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntToString])
    assertEquals(List("100"),node.values)
    assertEquals(Types.String,node.nodeType)
  }
  @Test def stringToIntMaker: Unit = {
    val vocabLine = "(ntInt Int ((str.to.int ntString)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(1,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    assertEquals(List(Types.String),maker.childTypes)
    val node = maker(List(new StringLiteral("-12",1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringToInt])
    assertEquals(List(-12),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def stringLenMaker: Unit = {
    val vocabLine = "(ntInt Int ((str.len ntString)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(1,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    assertEquals(List(Types.String),maker.childTypes)
    val node = maker(List(new StringLiteral("-12",1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringLength])
    assertEquals(List(3),node.values)
    assertEquals(Types.Int,node.nodeType)
  }

  @Test def strConcatMaker: Unit = {
    val vocabLine = "(ntString String ((str.++ ntString ntString)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.String,maker.returnType)
    assertEquals(List(Types.String,Types.String),maker.childTypes)
    val node = maker(List(new StringLiteral("a",1),new StringLiteral("b",1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringConcat])
    assertEquals(List("ab"),node.values)
    assertEquals(Types.String,node.nodeType)
  }
  @Test def strAtMaker: Unit = {
    val vocabLine = "(ntString String ((str.at ntString ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.String,maker.returnType)
    assertEquals(List(Types.String,Types.Int),maker.childTypes)
    val node = maker(List(new StringLiteral("abc",1),new IntLiteral(0,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringAt])
    assertEquals(List("a"),node.values)
    assertEquals(Types.String,node.nodeType)
  }
  @Test def intAddMaker:Unit = {
    val vocabLine = "(ntInt Int ((+ ntInt ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    assertEquals(List(Types.Int,Types.Int),maker.childTypes)
    val node = maker(List(new IntLiteral(-12,1), new IntLiteral(3,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntAddition])
    assertEquals(List(-9),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def intSubMaker: Unit = {
    val vocabLine = "(ntInt Int ((- ntInt ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    assertEquals(List(Types.Int,Types.Int),maker.childTypes)
    val node = maker(List(new IntLiteral(10,1), new IntLiteral(3,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntSubtraction])
    assertEquals(List(7),node.values)
    assertEquals(Types.Int,node.nodeType)
  }

  @Test def strReplaceMaker: Unit = {
    val vocabLine = "(ntString String ((str.replace ntString ntString ntString)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(3,maker.arity)
    assertEquals(Types.String,maker.returnType)
    assertEquals(List(Types.String,Types.String,Types.String),maker.childTypes)
    val node = maker(List(new StringLiteral("a",1),new StringLiteral("b",1), new StringLiteral("c",1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringReplace])
    assertEquals(List("a"),node.values)
    assertEquals(Types.String,node.nodeType)
  }
  @Test def stringITEMaker: Unit = {
    val vocabLine = "(ntString String ((ite ntBool ntString ntString)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(3,maker.arity)
    assertEquals(Types.String,maker.returnType)
    assertEquals(List(Types.Bool,Types.String,Types.String),maker.childTypes)
    val node = maker(List(new BoolLiteral(false,1),new StringLiteral("b",1), new StringLiteral("c",1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[StringITE])
    assertEquals(List("c"),node.values)
    assertEquals(Types.String,node.nodeType)
  }
  @Test def intITEMaker: Unit = {
    val vocabLine = "(ntInt Int ((ite ntBool ntInt ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(3,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    assertEquals(List(Types.Bool,Types.Int,Types.Int),maker.childTypes)
    val node = maker(List(new BoolLiteral(false,1),new IntLiteral(1,1), new IntLiteral(2,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntITE])
    assertEquals(List(2),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def substringMaker: Unit = {
    val vocabLine = "(ntString String ((str.substr ntString ntInt ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(3,maker.arity)
    assertEquals(Types.String,maker.returnType)
    assertEquals(List(Types.String,Types.Int,Types.Int),maker.childTypes)
    val node = maker(List(new StringLiteral("abc",1),new IntLiteral(0,1),new IntLiteral(2,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[Substring])
    assertEquals(List("ab"),node.values)
    assertEquals(Types.String,node.nodeType)
  }
  @Test def indexOfMaker: Unit = {
    val vocabLine = "(ntInt Int ((str.indexof ntString ntString ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(3,maker.arity)
    assertEquals(Types.Int,maker.returnType)
    assertEquals(List(Types.String,Types.String,Types.Int),maker.childTypes)
    val node = maker(List(new StringLiteral("abc",1),new StringLiteral("ab",1), new IntLiteral(0,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IndexOf])
    assertEquals(List(0),node.values)
    assertEquals(Types.Int,node.nodeType)
  }
  @Test def lteMaker: Unit = {
    val vocabLine = "(ntBool Bool ((<= ntInt ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    assertEquals(List(Types.Int,Types.Int),maker.childTypes)
    val node = maker(List(new IntLiteral(-12,1), new IntLiteral(3,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntLessThanEq])
    assertEquals(List(true),node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def eqMaker: Unit = {
    val vocabLine = "(ntBool Bool ((= ntInt ntInt)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    assertEquals(List(Types.Int,Types.Int),maker.childTypes)
    val node = maker(List(new IntLiteral(-12,1), new IntLiteral(3,1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[IntEquals])
    assertEquals(List(false),node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def prefixOfMaker: Unit = {
    val vocabLine = "(ntBool Bool ((str.prefixof ntString ntString)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    assertEquals(List(Types.String,Types.String),maker.childTypes)
    val node = maker(List(new StringLiteral("a",1),new StringLiteral("b",1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[PrefixOf])
    assertEquals(List(false),node.values)
    assertEquals(Types.Bool,node.nodeType)
  }

  @Test def suffixOfMaker: Unit = {
    val vocabLine = "(ntBool Bool ((str.suffixof ntString ntString)))"
    val parsed = readVocabElem(vocabLine)
    val maker: VocabMaker = SygusFileTask.makeVocabMaker(parsed._1,Types.withName(parsed._2), nonTerminals)
    assertEquals(2,maker.arity)
    assertEquals(Types.Bool,maker.returnType)
    assertEquals(List(Types.String,Types.String),maker.childTypes)
    val node = maker(List(new StringLiteral("a",1),new StringLiteral("b",1)),Map.empty[String,AnyRef] :: Nil)
    assertTrue(node.isInstanceOf[SuffixOf])
    assertEquals(List(false),node.values)
    assertEquals(Types.Bool,node.nodeType)
  }
}
