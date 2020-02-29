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

class StringStepList(val lhs: StringListNode, val rhs: IntNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = "[w[::" + rhs.code + "] for c in " + lhs.code + "]"

	override def doOp(lst: Any, step: Any): Option[Iterable[String]] = (lst, step) match
	{
		case (_, _: 0) => None
		case (lst: Iterable[String], step: Int) => Some(lst.map(str => {
			// TODO Is there a better way to do this?
			var rs: String = ""
			var idx = if (step > 0) 0 else str.length + step
			while (idx >= 0 && idx < str.length) {
				rs += str(idx)
				idx += step
			}
			rs
		}))
		case _ => wrongType(lst, step)
	}
}

class SubstringList(val lhs: StringListNode, val rhs: IntNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = "[w[" + rhs.code + "] for c in " + lhs.code + "]"

	override def doOp(lhs: Any, rhs: Any): Option[Iterable[String]] = (lhs, rhs) match {
		case (lst: Iterable[String], idx: Int) =>
			if (idx > -1 && lst.forall(_.length > idx)) Some(lst.map(_(idx).toString))
			else None
		case _ => wrongType(lhs, rhs)
	}
}

class StringToIntList(val arg: StringListNode) extends UnaryOpNode[Iterable[Int]] with IntListNode
{
	override lazy val code: String = "[int(i) for i in " + arg.code + "]"

	override def doOp(arg: Any): Option[Iterable[Int]] = arg match
	{
		case lst: Iterable[String] if lst.forall(s => s.nonEmpty && s.forall(_.isDigit)) => Some(lst.map(_.toInt))
		case _: Iterable[String] => None
		case _ => wrongType(arg)
	}
}

class SortedStringList(val arg: StringListNode) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = "sorted(" + arg.code + ")"

	override def doOp(arg: Any): Option[Iterable[String]] = arg match
	{
		case lst: Iterable[String] => Some(lst.toList.sorted)
		case _ => wrongType(arg)
	}
}