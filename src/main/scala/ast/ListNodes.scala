package ast

class StringSplit(val lhs: ASTNode, val rhs: ASTNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = lhs.code + ".split(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Iterable[String]] = (l, r) match {
		case (l: String, r: String ) => Some(l.split(r))
		case _ => wrongType(l, r)
	}
}

class StringJoin(val lhs: StringNode, val rhs: StringListNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + ".join(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, lst: Iterable[String]) => Some(lst.mkString(str))
		case _ => wrongType(l, r)
	}
}

class StringReverseList(val arg: StringListNode) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	// TODO This needs to also work with just Strings, since Strings are StringLists of their characters
	override lazy val code: String = "[w.reverse() for c in " + arg.code + "]"

	override def doOp(arg: Any): Option[Iterable[String]] = arg match
	{
		case lst: Iterable[String] => Some(lst.map(_.reverse))
		case _ => wrongType(arg)
	}
}

class SubstringList(val lhs: StringListNode, val rhs: IntNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = "[w[" + rhs.code + "] for c in " + lhs.code + "]"

	override def doOp(lhs: Any, rhs: Any): Option[Iterable[String]] = (lhs, rhs) match {
		case (lst: Iterable[String], idx: Int) => {
			if (idx > -1 && lst.forall(s => s.length > idx)) Some(lst.map(_(idx).toString))
			else None
		}
		case _ => wrongType(lhs, rhs)
	}
}