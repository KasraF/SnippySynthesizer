package ast

import trace.DebugPrints.eprintln

trait TernaryOpNode[T] extends ASTNode
{
	lazy val values: List[T] = arg0.values.zip(arg1.values).zip(arg2.values).map(tup => doOp(tup._1._1, tup._1._2, tup._2)).filter(_.isDefined).map(_.get)
	override val height: Int = 1 + Math.max(arg0.height, Math.max(arg1.height, arg2.height))
	override val terms : Int = 1 + arg0.terms + arg1.terms + arg2.terms
	override val children: Iterable[ASTNode] = Iterable(arg0, arg1, arg2)

	val arg0: ASTNode
	val arg1: ASTNode
	val arg2: ASTNode

	assert(arg0.values.length == arg1.values.length && arg1.values.length == arg2.values.length)

	def doOp(a0: Any, a1: Any, a2: Any): Option[T]

	def make(a0: ASTNode, a1: ASTNode, a2: ASTNode): TernaryOpNode[T]

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
		case (s: String, start_orig: Int, end_orig: Int) =>
			// The max() and min() remove unnecessary looping
			val start = (if (start_orig >= 0) start_orig else (s.length + start_orig)).max(0).min(s.length)
			val end = (if (end_orig >= 0) end_orig else (s.length + end_orig)).max(0).min(s.length)
			var rs = ""

			if (start < end) {
				var idx = start;

				while (idx < end) {
					if (idx < s.length) rs += s(idx)
					idx += 1
				}
			}

			Some(rs)
		case _ => wrongType(a0, a1, a2)
	}

	override def make(a0: ASTNode, a1: ASTNode, a2: ASTNode): TernaryOpNode[String] =
		new TernarySubstring(a0.asInstanceOf[StringNode], a1.asInstanceOf[IntNode], a2.asInstanceOf[IntNode])
}

class StringReplace(val arg0: StringNode, val arg1: StringNode, val arg2: StringNode) extends TernaryOpNode[String] with StringNode
{
	override lazy val code: String =
		(if (arg0.terms > 1) "(" + arg0.code + ")" else arg0.code) + ".replace(" + arg1.code + ", " + arg2.code + ")"

	override def doOp(a0: Any, a1: Any, a2: Any): Option[String] = (a0, a1, a2) match {
		case (s: String, it: String, that: String) =>
			Some(s.replace(it, that))
		case _ => wrongType(a0, a1, a2)
	}

	override def make(a0: ASTNode, a1: ASTNode, a2: ASTNode): TernaryOpNode[String] =
		new StringReplace(a0.asInstanceOf[StringNode], a1.asInstanceOf[StringNode], a2.asInstanceOf[StringNode])
}