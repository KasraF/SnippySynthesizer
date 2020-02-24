package ast

trait BinaryOpNode[T] extends ASTNode
{
	lazy val values: List[T] = lhs.values.zip(rhs.values).map(pair => doOp(pair._1, pair._2))
	override val height  : Int               = 1 + Math.max(lhs.height, rhs.height)
	override val terms   : Int               = 1 + lhs.terms + rhs.terms
	override val children: Iterable[ASTNode] = Iterable(lhs, rhs)
	val lhs: ASTNode

	assert(lhs.values.length == rhs.values.length)
	val rhs: ASTNode

	def doOp(l: Any, r: Any): T

	def includes(varName: String): Boolean = lhs.includes(varName) || rhs.includes(varName)
}

class StringConcat(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): String =
	{
		val strLhs = l.asInstanceOf[String]
		val strRhs = r.asInstanceOf[String]
		strLhs + strRhs
	}
}

class StringAt(val lhs: StringNode, val rhs: IntNode) extends BinaryOpNode[String] with StringNode
{
	override lazy val code: String = lhs.code + "[" + rhs.code + "]"

	override def doOp(l: Any, r: Any): String =
	{
		val str = l.asInstanceOf[String]
		val idx = r.asInstanceOf[Int]
		if (idx < 0 || idx >= str.length) {
			""
		} else {
			str(idx).toString
		}
	}
}

class IntAddition(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + " + " + rhs.code

	override def doOp(l: Any, r: Any): Int = l.asInstanceOf[Int] + r.asInstanceOf[Int]
}

class IntSubtraction(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String = lhs.code + " - " + rhs.code

	override def doOp(l: Any, r: Any): Int = l.asInstanceOf[Int] - r.asInstanceOf[Int]
}

class IntDivision(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Int] with IntNode
{
	override lazy val code: String =
		(if (lhs.terms > 1) "(" + lhs.code + ")" else lhs.code) + " // " + rhs.code

	override def doOp(l: Any, r: Any): Int = l.asInstanceOf[Int] / r.asInstanceOf[Int]
}


class IntLessThanEq(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " <= " + rhs.code

	override def doOp(l: Any, r: Any): Boolean = l.asInstanceOf[Int] <= r.asInstanceOf[Int]
}

class IntEquals(val lhs: IntNode, val rhs: IntNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " == " + rhs.code

	override def doOp(l: Any, r: Any): Boolean = l.asInstanceOf[Int] == r.asInstanceOf[Int]
}

//class PrefixOf(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
//{
//	override lazy val code: String = "(str.prefixof " + lhs.code + " " + rhs.code + ")"
//
//	override def doOp(l: Any, r: Any): Boolean = r.asInstanceOf[String].startsWith(l.asInstanceOf[String])
//}
//
//class SuffixOf(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
//{
//	override lazy val code: String = "(str.suffixof " + lhs.code + " " + rhs.code + ")"
//
//	override def doOp(l: Any, r: Any): Boolean = r.asInstanceOf[String].endsWith(l.asInstanceOf[String])
//}

class Contains(val lhs: StringNode, val rhs: StringNode) extends BinaryOpNode[Boolean] with BoolNode
{
	override lazy val code: String = lhs.code + " in " + rhs.code

	override def doOp(l: Any, r: Any): Boolean = l.asInstanceOf[String].contains(r.asInstanceOf[String])
}