package ast

import org.apache.commons.lang3.StringUtils

trait TernaryOpNode[T] extends ASTNode{
  val arg0: ASTNode
  val arg1: ASTNode
  val arg2: ASTNode
  override val height: Int = 1 + Math.max(arg0.height,Math.max(arg1.height,arg2.height))
  assert(arg0.values.length == arg1.values.length && arg1.values.length == arg2.values.length)
  def doOp(a0: Any, a1: Any, a2: Any): T
  lazy val values : List[T] =
    arg0.values.zip(arg1.values).zip(arg2.values).map(tup => doOp(tup._1._1, tup._1._2, tup._2)).toList
}

class StringReplace(val arg0: StringNode, val arg1: StringNode, val arg2: StringNode) extends TernaryOpNode[String] with StringNode {
  override def doOp(a0: Any, a1: Any, a2: Any): String =
    StringUtils.replaceOnce(a0.asInstanceOf[String],a1.asInstanceOf[String],a2.asInstanceOf[String])

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(str.replace "," ",")")
}

class StringITE(val arg0: BoolNode, val arg1: StringNode, val arg2: StringNode) extends TernaryOpNode[String] with StringNode {
  override def doOp(a0: Any, a1: Any, a2: Any): String = if (a0.asInstanceOf[Boolean]) a1.asInstanceOf[String] else a2.asInstanceOf[String]

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(ite "," ",")")
}

class IntITE(val arg0: BoolNode, val arg1: IntNode, val arg2: IntNode) extends TernaryOpNode[Int] with IntNode {
  override def doOp(a0: Any, a1: Any, a2: Any): Int = if (a0.asInstanceOf[Boolean]) a1.asInstanceOf[Int] else a2.asInstanceOf[Int]

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(ite "," ",")")
}

class Substring(val arg0: StringNode, val arg1: IntNode, val arg2: IntNode) extends TernaryOpNode[String] with StringNode {
  override def doOp(a0: Any, a1: Any, a2: Any): String = {
    //a[b:(c+b)] if 0 <= b and len(a) >= (c+b) >= b else ''
    val a = a0.asInstanceOf[String]
    val b = a1.asInstanceOf[Int]
    val c = a2.asInstanceOf[Int]
    if (0 <= b && a.length >= (c+b) && (c+b) >= b)
      a.drop(b).take(c)
    else ""
  }

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(str.substr "," ",")")
}

class IndexOf(val arg0: StringNode, val arg1: StringNode, val arg2: IntNode) extends TernaryOpNode[Int] with IntNode {
  override def doOp(a0: Any, a1: Any, a2: Any): Int = a0.asInstanceOf[String].indexOf(a1.asInstanceOf[String],a2.asInstanceOf[Int])

  override lazy val code: String = List(arg0.code,arg1.code,arg2.code).mkString("(str.indexof "," ",")")
}