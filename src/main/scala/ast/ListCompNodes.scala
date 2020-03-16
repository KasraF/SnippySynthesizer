package ast

import ast.Types.Types

trait ListCompNode[T] extends ListNode[T]
{
	val list: ASTNode
	val map: ASTNode
	val varName: String

	override val childType: Types = map.nodeType
	override val values: List[Iterable[T]] = list.values.indices.map(
		i => map.values.slice(
			i * map.values.length / list.values.length,
			(i+1) * map.values.length / list.values.length)).
	  toList.asInstanceOf[List[Iterable[T]]]// List(map.values.asInstanceOf[List[T]])
	override val height: Int = 1 + Math.max(list.height, map.height)
	override val terms: Int = 1 + list.terms + map.terms
	override val children: Iterable[ASTNode] = List(list, map)
	override val code: String = s"[${map.code} for $varName in ${list.code}]"
	override protected val parenless: Boolean = true
	override def includes(varName: String): Boolean =
		varName.equals(this.varName) || list.includes(varName) || map.includes(varName)
}

class StringToStringListCompNode(val list: ListNode[String], val map: StringNode, val varName: String) extends ListCompNode[String]
class StringToIntListCompNode(val list: ListNode[String], val map: IntNode, val varName: String) extends ListCompNode[Int]
class IntToStringListCompNode(val list: ListNode[Int], val map: StringNode, val varName: String) extends ListCompNode[String]
class IntToIntListCompNode(val list: ListNode[Int], val map: IntNode, val varName: String) extends ListCompNode[Int]