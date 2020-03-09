package ast

import trace.DebugPrints.eprintln

trait UnaryOpNode[T] extends ASTNode
{
	override lazy val values: List[T] = arg.values.map(doOp) match {
		case l if l.forall(_.isDefined) => l.map(_.get)
		case _ => Nil
	}

	override val height: Int = 1 + arg.height
	override val terms: Int = 1 + arg.terms
	override val children: Iterable[ASTNode] = Iterable(arg)
	val arg: ASTNode

	def doOp(x: Any): Option[T]
	def make(x: ASTNode): UnaryOpNode[T]
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

	override def make(x: ASTNode): UnaryOpNode[String] =
		new IntToString(x.asInstanceOf[IntNode])
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

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new StringToInt(x.asInstanceOf[StringNode])
}

class StringLength(val arg: StringNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "len(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match
	{
		case x: String => Some(x.length)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new StringLength(x.asInstanceOf[StringNode])
}

class StringLower(val arg: StringNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = arg.terms match {
		case 1 => arg.code + ".lower()"
		case _ => "(" + arg.code + ").lower()"
	}

	override def doOp(x: Any): Option[String] = x match {
		// TODO What's python's semantics here?
		case x: String => Some(x.toLowerCase)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[String] =
		new StringLower(x.asInstanceOf[StringNode])
}

class Max(val arg: IntListNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "max(" + arg.code + ")"
	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.max)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new Max(x.asInstanceOf[IntListNode])
}

class Min(val arg: IntListNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "min(" + arg.code + ")"
	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.min)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new Min(x.asInstanceOf[IntListNode])
}

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