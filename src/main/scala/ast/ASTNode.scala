package ast

import ast.Types.Types

trait ASTNode
{
	val nodeType: Types.Types
	val values  : List[Option[Any]]
	val code    : String
	val height  : Int
	val terms   : Int
	val children: Iterable[ASTNode]

	def includes(varName: String): Boolean
}

trait StringNode extends ASTNode
{
	override val values: List[Option[String]]
	override val nodeType: Types = Types.String
}

trait IntNode extends ASTNode
{
	override val values: List[Option[Int]]
	override val nodeType: Types = Types.Int
}

trait BoolNode extends ASTNode
{
	override val values: List[Option[Boolean]]
	override val nodeType: Types = Types.Bool
}

trait StringListNode extends ASTNode
{
	override val values: List[Option[Iterable[String]]]
	override val nodeType: Types = Types.StringList
}