package ast

trait TernaryOpNode[T] extends ASTNode
{
	lazy val values: List[T] = arg0.values.zip(arg1.values).zip(arg2.values).map(tup => doOp(tup._1._1, tup._1._2, tup._2)).toList
	override val height: Int = 1 + Math.max(arg0.height, Math.max(arg1.height, arg2.height))
	override val terms : Int = 1 + arg0.terms + arg1.terms + arg2.terms
	override val children: Iterable[ASTNode] = Iterable(arg0, arg1, arg2)
	val arg0: ASTNode

	assert(arg0.values.length == arg1.values.length && arg1.values.length == arg2.values.length)
	val arg1: ASTNode
	val arg2: ASTNode

	def doOp(a0: Any, a1: Any, a2: Any): T

	def includes(varName: String): Boolean = arg0.includes(varName) || arg1.includes(varName) || arg2.includes(varName)
}

class StringITE(val arg0: BoolNode, val arg1: StringNode, val arg2: StringNode) extends TernaryOpNode[String] with StringNode
{
	override lazy val code: String = List(arg0.code, arg1.code, arg2.code).mkString("(ite ", " ", ")")

	override def doOp(a0: Any, a1: Any, a2: Any): String = if (a0.asInstanceOf[Boolean]) a1.asInstanceOf[String] else a2.asInstanceOf[String]
}

class IntITE(val arg0: BoolNode, val arg1: IntNode, val arg2: IntNode) extends TernaryOpNode[Int] with IntNode
{
	override lazy val code: String = List(arg0.code, arg1.code, arg2.code).mkString("(ite ", " ", ")")

	override def doOp(a0: Any, a1: Any, a2: Any): Int = if (a0.asInstanceOf[Boolean]) a1.asInstanceOf[Int] else a2.asInstanceOf[Int]
}

class TernarySubstring(val arg0: StringNode, val arg1: IntNode, val arg2: IntNode) extends TernaryOpNode[String] with StringNode
{
	override lazy val code: String =
		(if (arg0.terms > 1) "(" + arg0.code + ")" else arg0.code) + "[" + arg1.code + ":" + arg2.code + "]"

	override def doOp(a0: Any, a1: Any, a2: Any): String =
	{
		val s = a0.asInstanceOf[String]
		val start = a1.asInstanceOf[Int]
		val end = a2.asInstanceOf[Int]

		// TODO What's Python's semantics here?
		if (start < 0 || end < 0 || start >= s.length || end > s.length) {
			""
		} else {
			s.slice(start, end)
		}
	}
}

class IndexOf(val arg0: StringNode, val arg1: StringNode, val arg2: IntNode) extends TernaryOpNode[Int] with IntNode
{
	override lazy val code: String = List(arg0.code, arg1.code, arg2.code).mkString("(str.indexof ", " ", ")")

	override def doOp(a0: Any, a1: Any, a2: Any): Int = a0.asInstanceOf[String].indexOf(a1.asInstanceOf[String], a2.asInstanceOf[Int])
}