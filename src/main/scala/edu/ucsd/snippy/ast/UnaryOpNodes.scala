package edu.ucsd.snippy.ast

import edu.ucsd.snippy.DebugPrints
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.Contexts

trait UnaryOpNode[T] extends ASTNode
{
	val arg: ASTNode

	override lazy val values: List[Option[T]] = arg.values.map(_.flatMap(doOp))

	override val height: Int = 1 + arg.height
	override val terms: Int = 1 + arg.terms
	override val children: Iterable[ASTNode] = Iterable(arg)
	override lazy val usesVariables: Boolean = arg.usesVariables
	override protected val parenless: Boolean = true

	def doOp(x: Any): Option[T]

	def make(x: ASTNode): UnaryOpNode[T]

	def includes(varName: String): Boolean = arg.includes(varName)

	protected def wrongType(x: Any): Option[T] =
	{
		DebugPrints.eprintln(s"[${this.getClass.getSimpleName}] Wrong value type: $x")
		None
	}
}

case class IntToString(arg: IntNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = "str(" + arg.code + ")"

	override def doOp(x: Any): Option[String] = x match {
		case x: Int => Some(x.toString)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[String] =
		IntToString(x.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): IntToString = copy(arg.updateValues(contexts).asInstanceOf[IntNode])
}

case class StringToInt(arg: StringNode) extends UnaryOpNode[Int] with IntNode
{
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
		StringToInt(x.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): StringToInt = copy(arg.updateValues(contexts).asInstanceOf[StringNode])
}

case class Length(arg: IterableNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "len(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match {
		case x: String => Some(x.length)
		case l: List[_] => Some(l.length)
		case m: Map[_, _] => Some(m.size)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		Length(x.asInstanceOf[IterableNode])

	override def updateValues(contexts: Contexts): Length = copy(arg.updateValues(contexts).asInstanceOf[IterableNode])
}

case class StringLower(arg: StringNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = arg.parensIfNeeded + ".lower()"

	override def doOp(x: Any): Option[String] = x match {
		case x: String => Some(x.toLowerCase)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[String] =
		StringLower(x.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): StringLower = copy(arg.updateValues(contexts).asInstanceOf[StringNode])
}

case class StringUpper(arg: StringNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = arg.parensIfNeeded + ".upper()"

	override def doOp(x: Any): Option[String] = x match {
		case x: String => Some(x.toUpperCase)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[String] =
		StringUpper(x.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): StringUpper = copy(arg.updateValues(contexts).asInstanceOf[StringNode])
}

case class Max(arg: ListNode[Int]) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "max(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.max)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		Max(x.asInstanceOf[ListNode[Int]])

	override def updateValues(contexts: Contexts): Max = copy(arg.updateValues(contexts).asInstanceOf[ListNode[Int]])
}

case class Min(arg: ListNode[Int]) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "min(" + arg.code + ")"

	override def doOp(x: Any): Option[Int] = x match {
		case lst: Iterable[Int] => if (lst.isEmpty) None else Some(lst.min)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		Min(x.asInstanceOf[ListNode[Int]])

	override def updateValues(contexts: Contexts): Min = copy(arg.updateValues(contexts).asInstanceOf[ListNode[Int]])
}

case class IsAlpha(arg: StringNode) extends UnaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = arg.parensIfNeeded + ".isalpha()"

	override def doOp(x: Any): Option[Boolean] = x match {
		case arg: String => Some(arg.matches("[a-zA-Z]+"))
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Boolean] = IsAlpha(x.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): IsAlpha = copy(arg.updateValues(contexts).asInstanceOf[StringNode])
}

case class IntSet(arg: ListNode[Int]) extends UnaryOpNode[Iterable[Int]] with ListNode[Int]
{
	override lazy val code: String = "set(" + arg.parensIfNeeded + ")"

	override def doOp(x: Any): Option[Iterable[Int]] = x match {
		case arg: List[Int] => Some(arg.distinct)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[Int]] = IntSet(x.asInstanceOf[ListNode[Int]])

	override def updateValues(contexts: Contexts): IntSet = copy(arg.updateValues(contexts).asInstanceOf[ListNode[Int]])

	override val childType: Types = arg.childType
}

case class StringSet(arg: ListNode[String]) extends UnaryOpNode[Iterable[String]] with ListNode[String]
{
	override lazy val code: String = "set(" + arg.parensIfNeeded + ")"

	override def doOp(x: Any): Option[Iterable[String]] = x match {
		case arg: List[String] => Some(arg.distinct)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[String]] = StringSet(x.asInstanceOf[ListNode[String]])

	override def updateValues(contexts: Contexts): StringSet = copy(arg.updateValues(contexts).asInstanceOf[ListNode[String]])

	override val childType: Types = arg.childType
}

case class Capitalize(arg: StringNode) extends UnaryOpNode[String] with StringNode
{
	override lazy val code: String = arg.parensIfNeeded + ".capitalize()"

	override def doOp(x: Any): Option[String] = x match {
		case arg: String => Some(arg.toLowerCase().capitalize)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[String] = Capitalize(x.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): Capitalize = copy(arg.updateValues(contexts).asInstanceOf[StringNode])
}

case class SortedStringList(arg: ListNode[String]) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = "sorted(" + arg.code + ")"

	override def doOp(arg: Any): Option[Iterable[String]] = arg match {
		case lst: Iterable[String] => Some(lst.toList.sorted)
		case _ => wrongType(arg)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[String]] = SortedStringList(x.asInstanceOf[ListNode[String]])

	override def updateValues(contexts: Contexts): SortedStringList = copy(arg.updateValues(contexts).asInstanceOf[ListNode[String]])
}

case class SortedIntList(arg: ListNode[Int]) extends UnaryOpNode[Iterable[Int]] with IntListNode
{
	override lazy val code: String = "sorted(" + arg.code + ")"

	override def doOp(arg: Any): Option[Iterable[Int]] = arg match {
		case lst: Iterable[Int] => Some(lst.toList.sorted)
		case _ => wrongType(arg)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[Int]] = SortedIntList(x.asInstanceOf[ListNode[Int]])

	override def updateValues(contexts: Contexts): SortedIntList = copy(arg.updateValues(contexts).asInstanceOf[ListNode[Int]])
}

case class UnarySplit(arg: StringNode) extends UnaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = arg.code + ".split()"

	override def doOp(arg: Any): Option[Iterable[String]] = arg match {
		case str: String => Some(str.split("\\s").toList)
		case _ => wrongType(arg)
	}

	override def make(x: ASTNode): UnaryOpNode[Iterable[String]] =
		UnarySplit(x.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): UnarySplit = copy(arg.updateValues(contexts).asInstanceOf[StringNode])
}

case class NegateInt(arg: IntNode) extends UnaryOpNode[Int] with IntNode
{
	override lazy val code: String = "-" + arg.parensIfNeeded

	override def doOp(x: Any): Option[Int] = x match {
		case x: Int => Some(-x)
		case _ => wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Int] =
		NegateInt(x.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): NegateInt = copy(arg.updateValues(contexts).asInstanceOf[IntNode])
}

case class NegateBool(arg: BoolNode) extends UnaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = "not " + arg.parensIfNeeded
	override val parenless: Boolean = false

	override def doOp(x: Any): Option[Boolean] = x match {
		case x: Boolean => Some(!x)
		case _ =>wrongType(x)
	}

	override def make(x: ASTNode): UnaryOpNode[Boolean] = NegateBool(x.asInstanceOf[BoolNode])

	override def updateValues(contexts: Contexts): NegateBool = copy(arg.updateValues(contexts).asInstanceOf[BoolNode])
}