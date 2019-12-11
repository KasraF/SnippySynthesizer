package ast

trait UnaryOpNode[T] extends ASTNode{
  val arg: ASTNode
  def doOp(x: Any): T
  override lazy val values : List[T] = arg.values.map(doOp)
  override val height = 1 + arg.height
}

class IntToString(val arg: IntNode) extends UnaryOpNode[String] with StringNode {
  override def doOp(x: Any): String = x.asInstanceOf[Int].toString

  override lazy val code: String = "(int.to.str " + arg.code + ")"
}

class StringToInt(val arg: StringNode) extends UnaryOpNode[Int] with IntNode {
  override def doOp(x: Any): Int = x.asInstanceOf[String].toInt

  override lazy val code: String = "(str.to.int " + arg.code + ")"
}

class StringLength(val arg: StringNode) extends UnaryOpNode[Int] with IntNode {
  override def doOp(x: Any): Int = x.asInstanceOf[String].length

  override lazy val code: String = "(str.len " + arg.code + ")"
}