package ast

trait UnaryOpNode[T] extends ASTNode
{
	override lazy val values  : List[T]           = arg.values.map(doOp)
	override      val height                      = 1 + arg.height
	override      val terms   : Int               = 1 + arg.terms
	override      val children: Iterable[ASTNode] = Iterable(arg)
	val arg: ASTNode

	def doOp(x: Any): T

	def includes(varName: String): Boolean = arg.includes(varName)
}

class IntToString(val arg: IntNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = "str(" + arg.code + ")"

	override def doOp(x: Any): String = if (x.asInstanceOf[Int] >= 0) x.asInstanceOf[Int].toString else ""
}

class StringToInt(val arg: StringNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "int(" + arg.code + ")"

	override def doOp(x: Any): Int =
	{
		val str = x.asInstanceOf[String]
      if (!str.isEmpty && str.forall(c => c.isDigit)) {
        str.toInt
      } else {
        -1
      }
    }
}

class StringLength(val arg: StringNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "len(" + arg.code + ")"

	override def doOp(x: Any): Int = x.asInstanceOf[String].length
}

class StringLower(val arg: StringNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = arg.code + ".lower()"

	override def doOp(x: Any): String = x.asInstanceOf[String].toLowerCase
}