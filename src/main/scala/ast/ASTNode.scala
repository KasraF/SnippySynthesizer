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

trait IntListNode extends ASTNode
{
	override val values: List[Iterable[Int]]
	override val nodeType: Types = Types.IntList
}

object EmptyStringListNode extends StringListNode
{
	override val values: List[Iterable[String]] = List(Nil)
	override val code: String = "[]"
	override val height: Int = 0
	override val terms: Int = 0
	override val children: Iterable[ASTNode] = Nil
	override def includes(varName: String): Boolean = false
}

object EmptyIntListNode extends IntListNode
{
	override val values: List[Iterable[Int]] = List(Nil)
	override val code: String = "[]"
	override val height: Int = 0
	override val terms: Int = 0
	override val children: Iterable[ASTNode] = Nil
	override def includes(varName: String): Boolean = false
}
