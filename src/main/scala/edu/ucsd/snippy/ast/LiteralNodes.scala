package edu.ucsd.snippy.ast

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.Contexts

abstract class LiteralNode[T](numContexts: Int) extends ASTNode
{
	assert(numContexts > 0)

	val height = 0
	val terms = 1
	val value: T
	override val values: List[Option[T]] = List.fill(numContexts)(Some(value))
	override val children: Iterable[ASTNode] = Iterable.empty
	override lazy val usesVariables: Boolean = false

	def includes(varName: String): Boolean = false
}

case class StringLiteral(value: String, numContexts: Int) extends LiteralNode[String](numContexts) with StringNode
{
	override protected val parenless: Boolean = true
	override val code: String = '"' + value.flatMap(c => if (c.toInt >= 32 && c.toInt <= 127 && c != '\\' && c != '"') {
		c.toString
	} else {
		c.toInt match {
			case 92 => "\\\\" // \
			case 34 => "\\\"" // "
			case 7 => "\\a" //bell
			case 8 => "\\b" //backspace
			case 9 => "\\t" //tab
			case 10 => "\\n" //lf
			case 11 => "\\v" //vertical tab
			case 12 => "\\f" //formfeed
			case 13 => "\\r" //cr
			case _ => "\\x" + c.toInt.toHexString
		}
	}) + '"'

	override def updateValues(contexts: Contexts): StringLiteral = copy(value, numContexts = contexts.contextLen)
}

case class IntLiteral(value: Int, numContexts: Int) extends LiteralNode[Int](numContexts) with IntNode
{
	override protected val parenless: Boolean = true
	override val code: String = value.toString

	override def updateValues(contexts: Contexts): IntLiteral = copy(value, numContexts = contexts.contextLen)
}

case class BoolLiteral(value: Boolean, numContexts: Int) extends LiteralNode[Boolean](numContexts) with BoolNode
{
	override protected val parenless: Boolean = true
	override val code: String = value.toString.capitalize

	override def updateValues(contexts: Contexts): BoolLiteral = copy(value, numContexts = contexts.contextLen)
}

case class DoubleLiteral(value: Double, numContexts: Int) extends LiteralNode[Double](numContexts) with DoubleNode {
	override val code: String = value.toString
	override protected val parenless: Boolean = true

	override def updateValues(contexts: Contexts): DoubleLiteral = copy(value, numContexts = contexts.contextLen)
}

case class ListLiteral[T](childType: Types, value: List[T], numContexts: Int) extends LiteralNode[List[T]](numContexts) with ListNode[T]
{
	val elems: List[LiteralNode[_]] = value.map {
		case b: Boolean => BoolLiteral(b, numContexts)
		case s: String => StringLiteral(s, numContexts)
		case i: Int => IntLiteral(i, numContexts)
		case d: Double => DoubleLiteral(d, numContexts)
	}

	assert(elems.forall(_.nodeType == this.childType))

	override protected val parenless: Boolean = true
	override val code: String = f"[${elems.map(_.code).mkString(", ")}]"
	override def updateValues(contexts: Contexts): ListLiteral[T] = copy(childType, value, numContexts = contexts.contextLen)
}