package ast

import trace.DebugPrints.eprintln

trait TernaryOpNode[T] extends ASTNode
{
	lazy val values: List[T] = arg0.values.zip(arg1.values).zip(arg2.values).map(tup => doOp(tup._1._1, tup._1._2, tup._2)).filter(_.isDefined).map(_.get)
	override val height: Int = 1 + Math.max(arg0.height, Math.max(arg1.height, arg2.height))
	override val terms : Int = 1 + arg0.terms + arg1.terms + arg2.terms
	override val children: Iterable[ASTNode] = Iterable(arg0, arg1, arg2)
	val arg0: ASTNode

	assert(arg0.values.length == arg1.values.length && arg1.values.length == arg2.values.length)
	val arg1: ASTNode
	val arg2: ASTNode

	def doOp(a0: Any, a1: Any, a2: Any): Option[T]

	def includes(varName: String): Boolean = arg0.includes(varName) || arg1.includes(varName) || arg2.includes(varName)
	protected def wrongType(l: Any, m: Any, r: Any) : Option[T] = {
		eprintln(s"Wrong types: $l $m $r")
		None
	}
}

class TernarySubstring(val arg0: StringNode, val arg1: IntNode, val arg2: IntNode) extends TernaryOpNode[String] with StringNode
{
	override lazy val code: String =
		(if (arg0.terms > 1) "(" + arg0.code + ")" else arg0.code) + "[" + arg1.code + ":" + arg2.code + "]"

	override def doOp(a0: Any, a1: Any, a2: Any): Option[String] = (a0, a1, a2) match {
		case (s: String, start: Int, end: Int) =>
			// TODO What's Python's semantics here?
			if (start < 0 || end < 0 || start >= s.length || end > s.length) {
				Some("")
			} else {
				Some(s.slice(start, end))
			}
		case _ => wrongType(a0, a1, a2)
	}
}