package ast

abstract class LiteralNode[T](numContexts: Int) extends ASTNode
{
	assert(numContexts > 0)

	override val children: Iterable[ASTNode] = Iterable.empty
	val height = 0
	val terms  = 1
	val value: T
	val values: List[T] = List.fill(numContexts)(value)

	def includes(varName: String): Boolean = false
}

class StringLiteral(val value: String, numContexts: Int) extends LiteralNode[String](numContexts) with StringNode
{
	override val code: String = '"' + value + '"'
}

class IntLiteral(val value: Int, numContexts: Int) extends LiteralNode[Int](numContexts) with IntNode
{
	override val code: String = value.toString
}

class BoolLiteral(val value: Boolean, numContexts: Int) extends LiteralNode[Boolean](numContexts) with BoolNode
{
	override val code: String = value.toString.capitalize
}