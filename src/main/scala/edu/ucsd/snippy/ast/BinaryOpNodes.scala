package edu.ucsd.snippy.ast

import edu.ucsd.snippy.DebugPrints
import edu.ucsd.snippy.enumeration.Contexts

trait BinaryOpNode[T] extends ASTNode
{
	val lhs: ASTNode
	val rhs: ASTNode

	override val height: Int = 1 + Math.max(lhs.height, rhs.height)
	override val terms: Int = 1 + lhs.terms + rhs.terms
	override val children: Iterable[ASTNode] = Iterable(lhs, rhs)
	override lazy val usesVariables: Boolean = lhs.usesVariables || rhs.usesVariables
	override protected val parenless: Boolean = false

	if (lhs.values.length != rhs.values.length) println(lhs.code, lhs.values, rhs.code, rhs.values)
	assert(lhs.values.length == rhs.values.length)

	def doOp(l: Any, r: Any): Option[T]
	def make(l: ASTNode, r: ASTNode): BinaryOpNode[T]

	override val values: List[Option[T]] = lhs.values.zip(rhs.values).map {
		case (Some(left), Some(right)) => this.doOp(left, right)
		case _ => None
	}

	def includes(varName: String): Boolean = lhs.includes(varName) || rhs.includes(varName)

	protected def wrongType(l: Any, r: Any): Option[T] =
	{
		DebugPrints.eprintln(s"[${this.getClass.getSimpleName}] Wrong value types: $l $r")
		None
	}
}

case class LessThanEq(lhs: IntNode, rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " <= " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l <= r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		LessThanEq(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): LessThanEq = copy(
		lhs.updateValues(contexts),
		rhs.updateValues(contexts))
}

case class GreaterThan(lhs: IntNode, rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " > " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l > r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		GreaterThan(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): GreaterThan = copy(
		lhs.updateValues(contexts),
		rhs.updateValues(contexts))
}

case class StringConcat(lhs: StringNode, rhs: StringNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (l: String, r: String) => Some(l + r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		StringConcat(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): StringConcat = copy(
		lhs.updateValues(contexts),
		rhs.updateValues(contexts))
}

case class MapGet(lhs: MapNode[String, Int], rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + "[" + rhs.code + "]"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (map: Map[String, Int], key: String) => map.get(key)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		MapGet(l.asInstanceOf[MapNode[String, Int]], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): MapGet = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class IntAddition(lhs: IntNode, rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l.asInstanceOf[Int] + r.asInstanceOf[Int])
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntAddition(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): IntAddition = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class IntMultiply(lhs: IntNode, rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + " * " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l.asInstanceOf[Int] * r.asInstanceOf[Int])
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntMultiply(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): IntMultiply = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class StringMultiply(lhs: StringNode, rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + " * " + rhs.code

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (l: String, r: Int) => Some(l.asInstanceOf[String] * r.asInstanceOf[Int])
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		StringMultiply(l.asInstanceOf[StringNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): StringMultiply = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class IntSubtraction(lhs: IntNode, rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
		override lazy val code: String = lhs.code + " - " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l - r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntSubtraction(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): IntSubtraction = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class IntDivision(lhs: IntNode, rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.parensIfNeeded + " // " + rhs.parensIfNeeded

	override def doOp(l: Any, r: Any): Option[Int] =
		(l, r) match {
			case (_: Int, 0) => None
			case (l: Int, r: Int) => Some(Math.floorDiv(l, r))
			case _ => wrongType(l, r)
		}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntDivision(lhs.asInstanceOf[IntNode], rhs.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): IntDivision = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class Find(lhs: StringNode, rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + ".find(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: String, r: String) => Some(l.indexOf(r))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		Find(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): Find = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class Contains(lhs: StringNode, rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.parensIfNeeded + " in " + rhs.parensIfNeeded

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (substr: String, str: String) => Some(str.contains(substr))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		Contains(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): Contains = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class StringSplit(lhs: StringNode, rhs: StringNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + ".split(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Iterable[String]] = (l, r) match {
		case (_, "") => None
		case (l: String, r: String) => Some(l.split(r).toList)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Iterable[String]] =
		StringSplit(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): StringSplit = copy(
		lhs.updateValues(contexts),
		rhs.updateValues(contexts))
}

case class StringJoin(lhs: StringNode, rhs: ListNode[String]) extends BinaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + ".join(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, lst: Iterable[_]) => Some(lst.mkString(str))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		StringJoin(l.asInstanceOf[StringNode], r.asInstanceOf[ListNode[String]])

	override def updateValues(contexts: Contexts): StringJoin = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class Count(lhs: StringNode, rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + ".count(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case ("", _) => Some(0)
		case (l: String, "") => Some(l.length + 1)
		case (l: String, r: String) =>
			var count = 0
			var i = 0
			while (i != -1) {
				val nextInstance = l.indexOf(r, i)
				if (nextInstance > -1) {
					count += 1
					i = nextInstance + r.length
				}
				else {
					i = -1
				}
			}
			Some(count)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		Count(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): Count = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class BinarySubstring(lhs: StringNode, rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + "[" + rhs.code + "]"

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, idx: Int) =>
			if (idx < 0 || idx >= str.length) {
				None
			} else {
				Some(str(idx).toString)
			}
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		BinarySubstring(l.asInstanceOf[StringNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): BinarySubstring = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class StartsWith(lhs: StringNode, rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.code + ".startswith(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: String, r: String) => Some(l.asInstanceOf[String].startsWith(r.asInstanceOf[String]))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		StartsWith(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): StartsWith = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}

case class EndsWith(lhs: StringNode, rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.code + ".endswith(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: String, r: String) => Some(l.asInstanceOf[String].endsWith(r.asInstanceOf[String]))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		EndsWith(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): EndsWith = copy(lhs.updateValues(contexts), rhs.updateValues(contexts))
}


case class StringStep(lhs: StringNode, rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + "[::" + rhs.code + "]"

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (_, _: 0) => None
		case (str: String, step: Int) =>
			var rs: StringBuilder = new StringBuilder(Math.abs(str.length / step) + 1)
			var idx = if (step > 0) 0 else str.length - 1
			while (idx >= 0 && idx < str.length) {
				rs += str(idx)
				idx += step
			}
			Some(rs.toString)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		StringStep(l.asInstanceOf[StringNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): StringStep = copy( lhs.updateValues(contexts), rhs.updateValues(contexts))
}
