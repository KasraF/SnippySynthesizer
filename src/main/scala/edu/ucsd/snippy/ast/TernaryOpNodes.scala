package edu.ucsd.snippy.ast

import edu.ucsd.snippy.DebugPrints
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.Contexts

trait TernaryOpNode[T] extends ASTNode
{
	val arg0: ASTNode
	val arg1: ASTNode
	val arg2: ASTNode

	assert(arg0.values.length == arg1.values.length && arg1.values.length == arg2.values.length)

	lazy val values: List[Option[T]] = arg0.values.lazyZip(arg1.values).lazyZip(arg2.values).map {
		case (Some(arg0), Some(arg1), Some(arg2)) => this.doOp(arg0, arg1, arg2)
		case _ => None
	}

	override val height: Int = 1 + Math.max(arg0.height, Math.max(arg1.height, arg2.height))
	override val terms: Int = 1 + arg0.terms + arg1.terms + arg2.terms
	override val children: Iterable[ASTNode] = Iterable(arg0, arg1, arg2)

	def doOp(a0: Any, a1: Any, a2: Any): Option[T]

	def make(a0: ASTNode, a1: ASTNode, a2: ASTNode): TernaryOpNode[T]

	def includes(varName: String): Boolean = arg0.includes(varName) || arg1.includes(varName) || arg2.includes(varName)

	override lazy val usesVariables: Boolean = arg0.usesVariables || arg1.usesVariables || arg2.usesVariables

	protected def wrongType(l: Any, m: Any, r: Any): Option[T] =
	{
		DebugPrints.eprintln(s"Wrong types: $l $m $r")
		None
	}
}

case class StringReplace(arg0: StringNode, arg1: StringNode, arg2: StringNode) extends TernaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = false
	override lazy val code: String =
		arg0.parensIfNeeded + ".replace(" + arg1.code + ", " + arg2.code + ")"

	override def doOp(a0: Any, a1: Any, a2: Any): Option[String] = (a0, a1, a2) match {
		case (s: String, it: String, that: String) =>
			Some(s.replace(it, that))
		case _ => wrongType(a0, a1, a2)
	}

	override def make(a0: ASTNode, a1: ASTNode, a2: ASTNode): TernaryOpNode[String] =
		StringReplace(a0.asInstanceOf[StringNode], a1.asInstanceOf[StringNode], a2.asInstanceOf[StringNode])

	override def updateValues(contexts: Contexts): StringReplace = copy(
		arg0.updateValues(contexts).asInstanceOf[StringNode],
		arg1.updateValues(contexts).asInstanceOf[StringNode],
		arg2.updateValues(contexts).asInstanceOf[StringNode])
}

case class TernarySubstring(arg0: StringNode, arg1: IntNode, arg2: IntNode) extends TernaryOpNode[String] with StringNode
{
	override protected val parenless: Boolean = true
	override lazy val code: String =
		arg0.parensIfNeeded + "[" + arg1.code + ":" + arg2.code + "]"

	override def doOp(a0: Any, a1: Any, a2: Any): Option[String] = (a0, a1, a2) match {
		case (s: String, start_orig: Int, end_orig: Int) =>
			// The max() and min() remove unnecessary looping
			val start = (if (start_orig >= 0) start_orig else (s.length + start_orig)).max(0).min(s.length)
			val end = (if (end_orig >= 0) end_orig else (s.length + end_orig)).max(0).min(s.length)
			var rs = ""

			if (start < end) {
				for (idx <- start until end) rs += s(idx)
			}

			Some(rs)
		case _ => wrongType(a0, a1, a2)
	}

	override def make(a0: ASTNode, a1: ASTNode, a2: ASTNode): TernaryOpNode[String] =
		TernarySubstring(a0.asInstanceOf[StringNode], a1.asInstanceOf[IntNode], a2.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): TernarySubstring = copy(
		arg0.updateValues(contexts).asInstanceOf[StringNode],
		arg1.updateValues(contexts).asInstanceOf[IntNode],
		arg2.updateValues(contexts).asInstanceOf[IntNode])
}

case class IntTernarySubList(arg0: ListNode[Int], arg1: IntNode, arg2: IntNode) extends TernaryOpNode[Iterable[Int]] with ListNode[Int]
{
	override protected val parenless: Boolean = true
	override lazy val code: String =
		arg0.parensIfNeeded + "[" + arg1.code + ":" + arg2.code + "]"

	override def doOp(a0: Any, a1: Any, a2: Any): Option[Iterable[Int]] = (a0, a1, a2) match {
		case (s: List[Int], start_orig: Int, end_orig: Int) =>
			// The max() and min() remove unnecessary looping
			val start = (if (start_orig >= 0) start_orig else (s.length + start_orig)).max(0).min(s.length)
			val end = (if (end_orig >= 0) end_orig else (s.length + end_orig)).max(0).min(s.length)
			var rs = List[Int]()

			if (start < end) {
				for (idx <- start until end) (
					rs = rs :+ s(idx))
			}

			Some(rs)
		case _ => wrongType(a0, a1, a2)
	}

	override def make(a0: ASTNode, a1: ASTNode, a2: ASTNode): IntTernarySubList =
		IntTernarySubList(a0.asInstanceOf[ListNode[Int]], a1.asInstanceOf[IntNode], a2.asInstanceOf[IntNode])

	override def updateValues(contexts: Contexts): IntTernarySubList = copy(
		arg0.updateValues(contexts).asInstanceOf[ListNode[Int]],
		arg1.updateValues(contexts).asInstanceOf[IntNode],
		arg2.updateValues(contexts).asInstanceOf[IntNode])

	override val childType: Types = arg0.childType
}