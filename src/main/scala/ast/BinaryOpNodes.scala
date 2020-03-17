package ast

import trace.DebugPrints.eprintln

import scala.collection.LinearSeq

trait BinaryOpNode[T] extends ASTNode
{
	lazy val values: List[T] = lhs.values.zip(rhs.values).map(pair => doOp(pair._1, pair._2)) match {
		case l if l.forall(_.isDefined) => l.map(_.get)
		case _ => Nil
	}
	override val height: Int = 1 + Math.max(lhs.height, rhs.height)
	override val terms: Int = 1 + lhs.terms + rhs.terms
	override val children: Iterable[ASTNode] = Iterable(lhs, rhs)

	val lhs: ASTNode
	val rhs: ASTNode

	assert(lhs.values.length == rhs.values.length)

	def doOp(l: Any, r: Any): Option[T]
	def make(l: ASTNode, r: ASTNode): BinaryOpNode[T]

	def includes(varName: String): Boolean = lhs.includes(varName) || rhs.includes(varName)

	protected def wrongType(l: Any, r: Any): Option[T] =
	{
		eprintln(s"[${this.getClass.getSimpleName}] Wrong value types: $l $r")
		None
	}
}

class LessThanEq(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " <= " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l <= r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		new LessThanEq(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])
}

class GreaterThan(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " > " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l > r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		new GreaterThan(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])
}

class StringConcat(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (l: String, r: String) => Some(l + r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		new StringConcat(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])
}

class MapGet(val lhs: MapNode[String,Int], val rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.terms match {
		case 1 => lhs.code + "[" + rhs.code + "]"
		case _ => "(" + lhs.code + ")[" + rhs.code + "]"
	}

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (map: Map[String,Int], key: String) => map.get(key)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		new MapGet(l.asInstanceOf[MapNode[String,Int]], r.asInstanceOf[StringNode])
}

class BinarySubstring(val lhs: StringNode, val rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.terms match {
		case 1 => lhs.code + "[" + rhs.code + "]"
		case _ => "(" + lhs.code + ")[" + rhs.code + "]"
	}

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, idx: Int) =>
			if (idx < 0 || idx >= str.length) None
			else Some(str(idx).toString)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		new BinarySubstring(l.asInstanceOf[StringNode], r.asInstanceOf[IntNode])
}

class StringStep(val lhs: StringNode, val rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + "[::" + rhs.code + "]"

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (_, _: 0) => None
		case (str: String, step: Int) =>
			// TODO Is there a better way to do this?
			var rs: String = ""
			var idx = if (step > 0) 0 else str.length - 1
			while (idx >= 0 && idx < str.length) {
				rs += str(idx)
				idx += step
			}
			Some(rs)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		new StringStep(l.asInstanceOf[StringNode], r.asInstanceOf[IntNode])
}

class IntAddition(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l.asInstanceOf[Int] + r.asInstanceOf[Int])
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		new IntAddition(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])
}

class IntSubtraction(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + " - " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: Int, r: Int) => Some(l - r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		new IntSubtraction(l.asInstanceOf[IntNode], r.asInstanceOf[IntNode])
}

class IntDivision(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String =
		(if (lhs.terms > 1) "(" + lhs.code + ")" else lhs.code) + " // " + rhs.code

	override def doOp(l: Any, r: Any): Option[Int] =
		(l, r) match {
			case (_: Int, 0) => None
			case (l: Int, r: Int) => Some(l / r)
			case _ => wrongType(l, r)
		}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		new IntDivision(lhs.asInstanceOf[IntNode], rhs.asInstanceOf[IntNode])
}

class Find(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + ".find(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case (l: String, r: String) => Some(l.indexOf(r))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		new Find(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])
}

class Contains(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " in " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (substr: String, str: String) => Some(str.contains(substr))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		new Contains(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])
}

class Count(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + ".count(" + rhs.code + ")"

	override def doOp(l: Any, r: Any): Option[Int] = (l, r) match {
		case ("", _) => Some(0)
		case (l: String, "") => Some(l.length + 1)
		case (l: String, r: String) => {
			// TODO I vaguely remember something from algorithms class about a faster way
			var count = 0
			var i = 0

			while (i < l.length - r.length + 1) {
				if (l.substring(i, i + r.length) == r) {
					count += 1
					i += r.length
				} else {
					i += 1
				}
			}

			Some(count)
		}
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Int] =
		new Count(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])
}

class StringSplit(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Iterable[String]] with StringListNode
{
	override lazy val code: String = lhs.terms match {
		case 1 => lhs.code + ".split(" + rhs.code + ")"
		case _ => "(" + lhs.code + ").split(" + rhs.code + ")"
	}

	override def doOp(l: Any, r: Any): Option[Iterable[String]] = (l, r) match {
		case (_, "") => None
		case (l: String, r: String) => Some(l.split(r).toList)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Iterable[String]] =
		new StringSplit(l.asInstanceOf[StringNode], r.asInstanceOf[StringNode])
}

class StringJoin(val lhs: StringNode, val rhs: ListNode[String]) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.terms match {
		case 1 => lhs.code + ".join(" + rhs.code + ")"
		case _ => "(" + lhs.code + ").join(" + rhs.code + ")"
	}

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, lst: Iterable[_]) => Some(lst.mkString(str))
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		new StringJoin(l.asInstanceOf[StringNode], r.asInstanceOf[ListNode[String]])
}