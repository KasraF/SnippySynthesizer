import ast._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite
import org.junit.Assert._

class ASTNodeTests extends JUnitSuite{
  @Test def stringLiteralNode(): Unit = {
    val stringLiteral: StringNode = new StringLiteral("abc",1)
    assertEquals(1,stringLiteral.values.length)
    assertEquals("abc",stringLiteral.values(0))
    assertEquals(Types.String,stringLiteral.nodeType)
    assertEquals("\"abc\"",stringLiteral.code)
    assertEquals(0,stringLiteral.height)
  }

  @Test def intLiteralNode(): Unit = {
    val intLiteral: IntNode = new IntLiteral(2,2)
    assertEquals(List(2,2),intLiteral.values)
    assertEquals(Types.Int,intLiteral.nodeType)
    assertEquals("2",intLiteral.code)
    assertEquals(0,intLiteral.height)
  }

  @Test def boolLiteralNode(): Unit = {
    val boolLiteral: BoolNode = new BoolLiteral(true,1)
    assertEquals(true,boolLiteral.values(0))
    assertEquals(Types.Bool,boolLiteral.nodeType)
    assertEquals("true",boolLiteral.code)
    assertEquals(0,boolLiteral.height)
  }

  @Test def variableNode(): Unit = {
    val contexts: List[Map[String,Any]] = List(Map("x" -> "abc"),Map("x" -> "","y" -> "abcd"))
    val stringVariableNode : StringNode = new StringVariable("x", contexts)
    assertEquals(Types.String,stringVariableNode.nodeType)
    assertEquals("x", stringVariableNode.code)
    assertEquals(0,stringVariableNode.height)
    assertEquals("abc",stringVariableNode.values(0))
    assertEquals("",stringVariableNode.values(1))

    val intVariableNode: IntNode = new IntVariable("y",Map("y" -> 2) :: Nil)
    assertEquals(Types.Int,intVariableNode.nodeType)
    assertEquals("y", intVariableNode.code)
    assertEquals(0,intVariableNode.height)
    assertEquals(1,intVariableNode.values.length)
    assertEquals(List(2),intVariableNode.values)

    val contexts2: List[Map[String,Any]] = List(Map("x" -> "abc","z"-> true),Map("x" -> "","y" -> "abcd","z" -> false), Map("z" -> true))
    val boolVariableNode: BoolNode = new BoolVariable("z",contexts2)
    assertEquals(Types.Bool,boolVariableNode.nodeType)
    assertEquals(0,boolVariableNode.height)
    assertEquals(3,boolVariableNode.values.length)
    assertEquals(List(true,false,true),boolVariableNode.values)
  }

  @Test def stringConcatNode: Unit = {
    val lhs = new StringLiteral("abc",2)
    val rhs = new StringVariable("x",Map("x" -> "123") :: Map("x" -> "456") :: Nil)
    val strConcat: StringNode = new StringConcat(lhs,rhs)
    assertEquals(Types.String, strConcat.nodeType)
    assertEquals(1, strConcat.height)
    assertEquals("(str.++ \"abc\" x)", strConcat.code)
    assertEquals(List("abc123","abc456"),strConcat.values)
  }

  @Test def stringReplaceNode: Unit = {
    val arg0 = new StringVariable("x",Map("x" -> "12312") :: Map("x" -> "456") :: Nil)
    val arg1 = new StringLiteral("12",2)
    val arg2 = new StringLiteral("2", 2)
    val strReplace: StringNode = new StringReplace(arg0, arg1, arg2)
    assertEquals(Types.String,strReplace.nodeType)
    assertEquals(1,strReplace.height)
    assertEquals("(str.replace x \"12\" \"2\")",strReplace.code)
    assertEquals(List("2312","456"),strReplace.values)
  }

  @Test def stringAtNode: Unit = {
    val lhs = new StringVariable("str",List(Map("str" -> "abc"),Map("str" -> "abcd")))
    val rhs = new IntLiteral(1,2)
    val strAt: StringNode = new StringAt(lhs,rhs)
    assertEquals(Types.String,strAt.nodeType)
    assertEquals(1,strAt.height)
    assertEquals("(str.at str 1)",strAt.code)
    assertEquals(List("b","b"),strAt.values)

    val strAt2 = new StringAt(strAt,rhs)
    assertEquals("(str.at (str.at str 1) 1)",strAt2.code)
    assertEquals(2,strAt2.height)
    assertEquals(List("",""), strAt2.values)
  }

  @Test def intToStringNode: Unit = {
    val arg = new IntVariable("i", Map("i" -> 1) :: Map("i" -> -1) :: Map("i" -> 0) :: Nil)
    val intToString: StringNode = new IntToString(arg)
    assertEquals(Types.String, intToString.nodeType)
    assertEquals(1, intToString.height)
    assertEquals("(int.to.str i)", intToString.code)
    assertEquals(List("1", "", "0"), intToString.values)
  }

  @Test def stringITENode: Unit = {
    val cond = new BoolVariable("b",Map("b" -> true) :: Map("b" -> false) :: Nil)
    val exp1 = new StringLiteral("true",2)
    val exp2 = new StringLiteral("false",2)
    val ite: StringNode = new StringITE(cond,exp1,exp2)
    assertEquals(Types.String,ite.nodeType)
    assertEquals(1,ite.height)
    assertEquals("(ite b \"true\" \"false\")", ite.code)
    assertEquals(List("true","false"),ite.values)
  }

  @Test def substringNode: Unit = {
    val str = new StringVariable("str", Map("str" -> "a") :: Map("str" -> "ab") :: Map("str" -> "abc"):: Map("str" -> "abcde") :: Nil)
    val from = new IntLiteral(1,4)
    val to = new IntLiteral(3,4)
    val substring : StringNode = new Substring(str,from,to)

    assertEquals(Types.String, substring.nodeType)
    assertEquals(1, substring.height)
    assertEquals("(str.substr str 1 3)",substring.code)
    //Desired behavior from euphony/eusolver: a[b:(c+b)] if 0 <= b and len(a) >= (c+b) >= b else ''
    assertEquals(List("","","","bcd"),substring.values)

  }

  @Test def intAddNode: Unit = {
    val lhs = new IntLiteral(1,1)
    val rhs = new IntLiteral(2,1)
    val add :IntNode = new IntAddition(lhs,rhs)
    assertEquals(Types.Int, add.nodeType)
    assertEquals(1, add.height)
    assertEquals("(+ 1 2)", add.code)
    assertEquals(List(3), add.values)
  }

  @Test def intSubNode: Unit = {
    val lhs = new IntLiteral(1,1)
    val rhs = new IntLiteral(2,1)
    val sub :IntNode = new IntSubtraction(lhs,rhs)
    assertEquals(Types.Int, sub.nodeType)
    assertEquals(1, sub.height)
    assertEquals("(- 1 2)", sub.code)
    assertEquals(List(-1), sub.values)
  }

  @Test def strelnNode: Unit = {
    val str = new StringVariable("s", Map("s" -> "") :: Map("s" -> " ") :: Nil)
    val strlen: IntNode = new StringLength(str)
    assertEquals(Types.Int, strlen.nodeType)
    assertEquals(1,strlen.height)
    assertEquals("(str.len s)", strlen.code)
    assertEquals(List(0,1), strlen.values)
  }

  @Test def stringToIntNode: Unit = {
    val str = new StringLiteral("88",1)
    val strToInt: IntNode = new StringToInt(str)

    assertEquals(Types.Int,strToInt.nodeType)
    assertEquals(1, strToInt.height)
    assertEquals("(str.to.int \"88\")",strToInt.code)
    assertEquals(List(88), strToInt.values)

    val str2 = new StringLiteral("a",1)
    val strToInt2: IntNode = new StringToInt(str2)

    assertEquals(Types.Int,strToInt2.nodeType)
    assertEquals(1, strToInt2.height)
    assertEquals("(str.to.int \"a\")",strToInt2.code)
    assertEquals(List(-1), strToInt2.values)
  }

  @Test def intITENode: Unit = {
    val cond = new BoolVariable("b",Map("b" -> true) :: Map("b" -> false) :: Nil)
    val exp1 = new IntLiteral(5,2)
    val exp2 = new IntLiteral(-10,2)
    val ite: IntNode = new IntITE(cond,exp1,exp2)
    assertEquals(Types.Int,ite.nodeType)
    assertEquals(1,ite.height)
    assertEquals("(ite b 5 -10)", ite.code)
    assertEquals(List(5,-10),ite.values)
  }

  @Test def indexOfNode: Unit = {
    val arg0 = new StringLiteral("abcd",3)
    val arg1 = new StringVariable("s", Map("s" -> "a") :: Map("s" -> "cd") :: Map("s" -> "def") :: Nil)
    val arg2 = new IntLiteral(1,3)
    val indexOf : IntNode = new IndexOf(arg0,arg1,arg2)

    assertEquals(Types.Int,indexOf.nodeType)
    assertEquals(1,indexOf.height)
    assertEquals("(str.indexof \"abcd\" s 1)",indexOf.code)
    assertEquals(List(-1,2,-1),indexOf.values)
  }

  @Test def lteNode: Unit = {
    val lhs = new IntLiteral(1,1)
    val rhs = new IntLiteral(1,1)
    val lte :BoolNode = new IntLessThanEq(lhs,rhs)
    assertEquals(Types.Bool, lte.nodeType)
    assertEquals(1, lte.height)
    assertEquals("(<= 1 1)", lte.code)
    assertEquals(List(true), lte.values)
  }

  @Test def eqNode: Unit = {
    val ctxs = Map("i" -> 5, "j" -> 6) :: Map("i" -> 5, "j" -> 5) :: Nil
    val lhs = new IntVariable("i",ctxs)
    val rhs = new IntVariable("j",ctxs)
    val eq: BoolNode = new IntEquals(lhs,rhs)
    assertEquals(Types.Bool,eq.nodeType)
    assertEquals(1,eq.height)
    assertEquals("(= i j)", eq.code)
    assertEquals(List(false,true),eq.values)
  }

  @Test def prefixOfNode: Unit = {
    val lhs = new StringLiteral("abc",2)
    val rhs = new StringVariable("x", Map("x" -> "ab"):: Map("x" -> "c") :: Nil)
    val prefixOf: BoolNode = new PrefixOf(lhs,rhs)
    assertEquals(Types.Bool,prefixOf.nodeType)
    assertEquals(1,prefixOf.height)
    assertEquals("(str.prefixof \"abc\" x)", prefixOf.code)
    assertEquals(List(true,false),prefixOf.values)
  }

  @Test def suffixOfNode: Unit = {
    val lhs = new StringLiteral("abc",2)
    val rhs = new StringVariable("x", Map("x" -> "ab"):: Map("x" -> "c") :: Nil)
    val suffixOf: BoolNode = new SuffixOf(lhs,rhs)
    assertEquals(Types.Bool,suffixOf.nodeType)
    assertEquals(1,suffixOf.height)
    assertEquals("(str.suffixof \"abc\" x)", suffixOf.code)
    assertEquals(List(false,true),suffixOf.values)
  }

  @Test def strContains: Unit = {
    val lhs = new StringLiteral("abc",2)
    val rhs = new StringVariable("x", Map("x" -> "d"):: Map("x" -> "c") :: Nil)
    val contains: BoolNode = new Contains(lhs,rhs)
    assertEquals(Types.Bool,contains.nodeType)
    assertEquals(1,contains.height)
    assertEquals("(str.contains \"abc\" x)", contains.code)
    assertEquals(List(false,true),contains.values)
  }
}
