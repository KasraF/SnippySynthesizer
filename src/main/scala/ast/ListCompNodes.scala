package ast

import ast.Types.Types

trait ListCompNode[T] extends ASTNode
{
	val list: ASTNode
	val map: ASTNode
	val varName: String

	override val nodeType: Types = Types.listOf(map.nodeType)
	override val values: List[Iterable[T]] = List(map.values.asInstanceOf[List[T]])
	override val height: Int = 1 + Math.max(list.height, map.height)
	override val terms: Int = 1 + list.terms + map.terms
	override val children: Iterable[ASTNode] = List(list, map)
	override val code: String = s"[${map.code} for $varName in ${list.code}]"
	override def includes(varName: String): Boolean =
		varName.equals(this.varName) || list.includes(varName) || map.includes(varName)
}

class StringToStringListCompNode(val list: StringListNode, val map: StringNode, val varName: String) extends ListCompNode[String] with StringListNode
class StringToIntListCompNode(val list: StringListNode, val map: IntNode, val varName: String) extends ListCompNode[Int] with IntListNode
class IntToStringListCompNode(val list: IntListNode, val map: StringNode, val varName: String) extends ListCompNode[String] with StringListNode
class IntToIntListCompNode(val list: IntListNode, val map: IntNode, val varName: String) extends ListCompNode[Int] with IntListNode