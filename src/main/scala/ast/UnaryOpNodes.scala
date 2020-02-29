package ast

import trace.DebugPrints.eprintln

trait UnaryOpNode[T] extends ASTNode
{
	override lazy val values: List[T] = arg.values.map(doOp).filter(_.isDefined).map(_.get)
	override val height = 1 + arg.height
	override val terms: Int = 1 + arg.terms
	override val children: Iterable[ASTNode] = Iterable(arg)
	val arg: ASTNode

	def doOp(x: Any): Option[T]

	def includes(varName: String): Boolean = arg.includes(varName)
	protected def wrongType(x: Any) : Option[T] =
	{
		eprintln(s"Wrong type: $x")
		None
	}
}

class IntToString(val arg: IntNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = "str(" + arg.code + ")"

	override def doOp(x: Any): Option[String] = x match {
		case x: Int => Some(x.toString)
		case _ => wrongType(x)
	}
}

class StringToInt(val arg: StringNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "int(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match {
		case str: String =>
			if (!str.isEmpty && str.forall(c => c.isDigit)) {
				str.toIntOption
			} else {
				None
			}
		case _ => wrongType(x)
	}
}

class StringLength(val arg: StringNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "len(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match
	{
		case x: String => Some(x.length)
		case _ => wrongType(x)
	}
}

class StringLower(val arg: StringNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = arg.code + ".lower()"

	override def doOp(x: Any): Option[String] = x match {
		// TODO What's python's semantics here?
		case x: String => Some(x.toLowerCase)
		case _ => wrongType(x)
	}
}

class Max(val arg: IntListNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "max(" + arg.code + ")"
	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.max)
		case _ => wrongType(x)
	}
}

class Min(val arg: IntListNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "min(" + arg.code + ")"
	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.min)
		case _ => wrongType(x)
	}
}