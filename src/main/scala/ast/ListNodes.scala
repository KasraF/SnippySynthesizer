package ast

class StringSplit(val lhs: ASTNode, val rhs: ASTNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = lhs.code + ".split(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Iterable[String] =
	{
		val strLhs = l.asInstanceOf[String]
		val strRhs = r.asInstanceOf[String]
		strLhs.split(strRhs)
	}
}

class StringJoin(val lhs: StringNode, val rhs: StringListNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + ".join(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): String =
	{
		val str = l.asInstanceOf[String]
		val lst = r.asInstanceOf[Iterable[String]]
		lst.mkString(str)
	}
}

class StringReverseList(val arg: StringListNode) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	// TODO This needs to also work with just Strings, since Strings are StringLists of their characters
	override lazy val code: String = "[w.reverse() for c in " + arg.code + "]"

	override def doOp(arg: Any): Iterable[String] =
	{
		val lst = arg.asInstanceOf[Iterable[String]]
		lst.map(_.reverse)
	}
}

class SubstringList(val lhs: StringListNode, val rhs: IntNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = "[w[" + rhs.code + "] for c in " + lhs.code + "]"

	override def doOp(lhs: Any, rhs: Any): Iterable[String] =
	{
		val lst = lhs.asInstanceOf[Iterable[String]]
		val idx = rhs.asInstanceOf[Int]
		lst.map(_(idx).toString)
	}
}