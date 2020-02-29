package ast

import trace.DebugPrints.eprintln

trait BinaryOpNode[T] extends ASTNode
{
	lazy val values: List[T] = lhs.values.zip(rhs.values).map(pair => doOp(pair._1, pair._2)).filter(_.isDefined).map(_.get)
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
		eprintln(false, s"Wrong value types: $l $r")
		None
	}
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

class StringAt(val lhs: StringNode, val rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + "[" + rhs.code + "]"

	override def doOp(l: Any, r: Any): Option[String] = (l, r) match {
		case (str: String, idx: Int) =>
			if (idx < 0 || idx >= str.length) None
			else Some(str(idx).toString)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[String] =
		new StringAt(l.asInstanceOf[StringNode], r.asInstanceOf[IntNode])
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


class IntLessThanEq(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " <= " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l <= r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		new IntLessThanEq(lhs.asInstanceOf[IntNode], rhs.asInstanceOf[IntNode])
}

class IntEquals(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " == " + rhs.code

	override def doOp(l: Any, r: Any): Option[Boolean] = (l, r) match {
		case (l: Int, r: Int) => Some(l == r)
		case _ => wrongType(l, r)
	}

	override def make(l: ASTNode, r: ASTNode): BinaryOpNode[Boolean] =
		new IntEquals(lhs.asInstanceOf[IntNode], rhs.asInstanceOf[IntNode])
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