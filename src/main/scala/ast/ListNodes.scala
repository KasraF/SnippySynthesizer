package ast
import ast.Types.Types

class ListCompNode(list: ASTNode, map: ASTNode, varName: String) extends ASTNode
{
	override val nodeType: Types = Types.listOf(map.nodeType)
	override val values: List[Any] = List(map.values)
	override val height: Int = 1 + Math.max(list.height, map.height)
	override val terms: Int = 1 + list.terms + map.terms
	override val children: Iterable[ASTNode] = List(list, map)
	override val code: String = s"[${map.code} for $varName in ${list.code}]"
	override def includes(varName: String): Boolean =
		varName.equals(this.varName) || list.includes(varName) || map.includes(varName)
}

class StringSplit(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = lhs.terms match {
		case 1 => lhs.code + ".split(" + rhs.code + ")"
		case _ => "(" + lhs.code + ").split(" + rhs.code + ")"
	}

	override def doOp(l: Any, r: Any): Option[Iterable[String]] = (l, r) match {
		case (l: String, r: String ) => Some(l.split(r))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Iterable[String]] =
		new StringSplit(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])
}

class StringJoin(val lhs: StringNode, val rhs: StringListNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.terms match {
		case 1 => lhs.code + ".join(" + rhs.code + ")"
		case _ => "(" + lhs.code + ").join(" + rhs.code + ")"
	}

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, lst: Iterable[String]) => Some(lst.mkString(str))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		new StringJoin(l.asInstanceOf[StringNode], r.asInstanceOf[StringListNode])
}

//class StringStepList(val lhs: StringListNode, val rhs: IntNode) extends BinaryOpNode[Iterable[String]] with StringListNode
//{
//	override lazy val code: String = "[w[::" + rhs.code + "] for c in " + lhs.code + "]"
//
//	override def doOp(lst: Any, step: Any): Option[Iterable[String]] = (lst, step) match
//	{
//		case (_, _: 0) => None
//		case (lst: Iterable[String], step: Int) => Some(lst.map(str => {
//			// TODO Is there a better way to do this?
//			var rs: String = ""
//			var idx = if (step > 0) 0 else str.length + step
//			while (idx >= 0 && idx < str.length) {
//				rs += str(idx)
//				idx += step
//			}
//			rs
//		}))
//		case _ => wrongType(lst, step)
//	}
//
//	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Iterable[String]] =
//		new StringStepList(l.asInstanceOf[StringListNode], r.asInstanceOf[IntNode])
//}
//
//class SubstringList(val lhs: StringListNode, val rhs: IntNode) extends BinaryOpNode[Iterable[String]] with StringListNode
//{
//	override lazy val code: String = "[w[" + rhs.code + "] for c in " + lhs.code + "]"
//
//	override def doOp(lhs: Any, rhs: Any): Option[Iterable[String]] = (lhs, rhs) match {
//		case (lst: Iterable[String], idx: Int) =>
//			if (idx > -1 && lst.forall(_.length > idx)) Some(lst.map(_(idx).toString))
//			else None
//		case _ => wrongType(lhs, rhs)
//	}
//
//	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Iterable[String]] =
//		new SubstringList(l.asInstanceOf[StringListNode], r.asInstanceOf[IntNode])
//}
//
//class StringToIntList(val arg: StringListNode) extends UnaryOpNode[Iterable[Int]] with IntListNode
//{
//	override lazy val code: String = "[int(i) for i in " + arg.code + "]"
//
//	override def doOp(arg: Any): Option[Iterable[Int]] = arg match
//	{
//		case lst: Iterable[String] if lst.forall(s => s.nonEmpty && s.forall(_.isDigit)) => Some(lst.map(_.toInt))
//		case _: Iterable[String] => None
//		case _ => wrongType(arg)
//	}
//
//	override def make(x: ASTNode): UnaryOpNode[Iterable[Int]] =
//		new StringToIntList(x.asInstanceOf[StringListNode])
//}

class SortedStringList(val arg: StringListNode) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = "sorted(" + arg.code + ")"

	override def doOp(arg: Any): Option[Iterable[String]] = arg match
	{
		case lst: Iterable[String] => Some(lst.toList.sorted)
		case _ => wrongType(arg)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[String]] =
		new SortedStringList(x.asInstanceOf[StringListNode])
}