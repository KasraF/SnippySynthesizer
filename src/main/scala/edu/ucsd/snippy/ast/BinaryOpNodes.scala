package edu.ucsd.snippy.ast

import edu.ucsd.snippy.DebugPrints
import edu.ucsd.snippy.enumeration.Contexts

trait BinaryOpNode[T] extends ASTNode
{
	val lhs: ASTNode
	val rhs: ASTNode

	override val height: Int = 1 + Math.max(lhs.height, rhs.height)
	override val terms: Int = 1 + lhs.terms + rhs.terms
	if (lhs.values.length != rhs.values.length) println(lhs.code, lhs.values, rhs.code, rhs.values)

	assert(lhs.values.length == rhs.values.length)

	def doOp(l: Any, r: Any): Option[T]
	def make(l: ASTNode, r: ASTNode): BinaryOpNode[T]

	override val values: List[T] = lhs.values.zip(rhs.values).map(pair => doOp(pair._1, pair._2)) match {
		case l if l.forall(_.isDefined) => l.map(_.get)
		case _ => Nil
	}

	override val children: Iterable[ASTNode] = Iterable(lhs, rhs)
	override lazy val usesVariables: Boolean = lhs.usesVariables || rhs.usesVariables

	def includes(varName: String): Boolean = lhs.includes(varName) || rhs.includes(varName)

	protected def wrongType(l: Any, r: Any): Option[T] =
	{
		DebugPrints.eprintln(s"[${this.getClass.getSimpleName}] Wrong value types: $l $r")
		None
	}
}

case class LessThanEq(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.code + " <= " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l <= r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		LessThanEq(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[IntNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class GreaterThan(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.code + " > " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l > r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		GreaterThan(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[IntNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class StringConcat(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (l: String, r: String) => Some(l + r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		StringConcat(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[StringNode])
}

case class MapGet(val lhs: MapNode[String, Int], val rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + "[" + rhs.code + "]"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (map: Map[String, Int], key: String) => map.get(key)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		MapGet(l.asInstanceOf[MapNode[String, Int]], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts) = copy(lhs, rhs)
}

case class IntAddition(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l.asInstanceOf[Int] + r.asInstanceOf[Int])
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntAddition(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[IntNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class IntMultiply(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.code + " * " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l.asInstanceOf[Int] * r.asInstanceOf[Int])
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntMultiply(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[IntNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class StringMultiply(val lhs: StringNode, val rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.code + " * " + rhs.code

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (l: String, r: Int) => Some(l.asInstanceOf[String] * r.asInstanceOf[Int])
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		StringMultiply(l.asInstanceOf[StringNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class IntSubtraction(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.code + " - " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l - r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntSubtraction(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[IntNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class IntDivision(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String =
		lhs.parensIfNeeded + " // " + rhs.parensIfNeeded

	override def doOp(l: Any, r: Any): Option[Int] =
		(l, r) match {
			case (_: Int, 0) => None
			case (l: Int, r: Int) => Some(Math.floorDiv(l, r))
			case _ => wrongType(l, r)
		}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		IntDivision(lhs.asInstanceOf[IntNode], rhs.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[IntNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class Find(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + ".find(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: String, r: String) => Some(l.indexOf(r))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		Find(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[StringNode])
}

case class Contains(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.parensIfNeeded + " in " + rhs.parensIfNeeded

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (substr: String, str: String) => Some(str.contains(substr))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		Contains(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[StringNode])
}

case class StringSplit(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Iterable[String]] with StringListNode
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

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[StringNode])
}

case class StringJoin(val lhs: StringNode, val rhs: ListNode[String]) extends BinaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String = lhs.parensIfNeeded + ".join(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, lst: Iterable[_]) => Some(lst.mkString(str))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		StringJoin(l.asInstanceOf[StringNode], r.asInstanceOf[ListNode[String]])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[ListNode[String]])
}

case class Count(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.parensIfNeeded + ".count(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case ("", _) => Some(0)
		case (l: String, "") => Some(l.length + 1)
		case (l: String, r: String) => {
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
		}
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		Count(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[StringNode])
}

case class BinarySubstring(val lhs: StringNode, val rhs: IntNode) extends BinaryOpNode[String] with StringNode
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

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}

case class StartsWith(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.code + ".startswith(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: String, r: String) => Some(l.asInstanceOf[String].startsWith(r.asInstanceOf[String]))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		StartsWith(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[StringNode])

}

case class EndsWith(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String = lhs.code + ".endswith(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: String, r: String) => Some(l.asInstanceOf[String].endsWith(r.asInstanceOf[String]))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		EndsWith(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[StringNode])
}


case class StringStep(val lhs: StringNode, val rhs: IntNode) extends BinaryOpNode[String] with StringNode
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

	override def updateValues(contexts: Contexts) = copy(lhs.updateValues(contexts).asInstanceOf[StringNode], rhs.updateValues(contexts).asInstanceOf[IntNode])
}
