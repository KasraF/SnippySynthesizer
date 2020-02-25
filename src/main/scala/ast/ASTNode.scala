package ast

import ast.Types.Types

trait ASTNode
{
	val nodeType: Types.Types
	val values  : List[Any]
	val code    : String
	val height  : Int
	val terms   : Int
	val children: Iterable[ASTNode]

	def includes(varName: String): Boolean
}

trait StringNode extends ASTNode
{
	override val values: List[String]
	override val nodeType: Types = Types.String
}

trait IntNode extends ASTNode
{
	override val values: List[Int]
	override val nodeType: Types = Types.Int
}

trait BoolNode extends ASTNode
{
	override val values: List[Boolean]
	override val nodeType: Types = Types.Bool
}

trait StringListNode extends ASTNode
{
	override val values: List[Iterable[String]]
	override val nodeType: Types = Types.StringList
}