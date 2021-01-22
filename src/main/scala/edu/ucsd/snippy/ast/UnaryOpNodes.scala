package edu.ucsd.snippy.ast

import edu.ucsd.snippy.DebugPrints

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
	override lazy val usesVariables: Boolean = arg.usesVariables
	protected def wrongType(x: Any) : Option[T] =
	{
		DebugPrints.eprintln(s"[${this.getClass.getSimpleName}] Wrong value type: $x")
		None
	}
}

class Negate(val arg: IntNode) extends UnaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = "-" + arg.code

	override def doOp(x: Any): Option[Int] = x match {
		case x: Int => Some(-x)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new Negate(x.asInstanceOf[IntNode])
}

class IntToString(val arg: IntNode) extends UnaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = true
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
	override protected val parenless: Boolean = true
	override lazy val code: String = "int(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match {
		case str: String =>
			if (!str.isEmpty && (str(0) == '-' && str.substring(1).forall(_.isDigit)) || str.forall(_.isDigit)) {
				str.toIntOption
			} else {
				None
			}
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new StringToInt(x.asInstanceOf[StringNode])
}

class Length(val arg: IterableNode) extends UnaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = "len(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match
	{
		case x: String => Some(x.length)
		case l: List[_] => Some(l.length)
		case m: Map[_,_] => Some(m.size)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new Length(x.asInstanceOf[IterableNode])
}

class StringLower(val arg: StringNode) extends UnaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = arg.parensIfNeeded + ".lower()"

	override def doOp(x: Any): Option[String] = x match {
		case x: String => Some(x.toLowerCase)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[String] =
		new StringLower(x.asInstanceOf[StringNode])
}

class Max(val arg: ListNode[Int]) extends UnaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = "max(" + arg.code + ")"
	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.max)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new Max(x.asInstanceOf[ListNode[Int]])
}

class Min(val arg: ListNode[Int]) extends UnaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = "min(" + arg.code + ")"
	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.min)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		new Min(x.asInstanceOf[ListNode[Int]])
}

class SortedStringList(val arg: ListNode[String]) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = "sorted(" + arg.code + ")"

	override def doOp(arg: Any): Option[Iterable[String]] = arg match
	{
		case lst: Iterable[String] => Some(lst.toList.sorted)
		case _ => wrongType(arg)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[String]] =
		new SortedStringList(x.asInstanceOf[ListNode[String]])
}

class UnarySplit(val arg: StringNode) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = arg.code + ".split()"

	override def doOp(arg: Any): Option[Iterable[String]] = arg match {
		case str: String => Some(str.split("\\s").toList)
		case _ => wrongType(arg)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[String]] =
		new UnarySplit(x.asInstanceOf[StringNode])
}