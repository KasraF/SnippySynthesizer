package ast

import ast.Types.Types

trait MapCompNode[K,V] extends ASTNode
{
	val list: ASTNode
	val key: ASTNode
	val value: ASTNode
	val varName: String

	assert(key.values.length == value.values.length, "Key and value did not match")

	override val nodeType: Types = Types.Map(Types.childOf(list.nodeType), value.nodeType)

	override val values: List[Map[K,V]] = {
		val entries = key.values.zip(value.values)
		val rs = list.values
		  .indices
		  .map(i => entries.slice(
			  i * entries.length / list.values.length,
			  (i+1) * entries.length / list.values.length))
		  .map(l => l.map { case (k: Any, v: Any) => k.asInstanceOf[K] -> v.asInstanceOf[V] }.toMap[K,V])
		  .toList

		rs
	}

	override val height: Int = 1 + Math.max(list.height, value.height)
	override val terms: Int = 1 + list.terms + value.terms
	override val children: Iterable[ASTNode] = List(list, value)
	override val code: String = s"{${key.code}: ${value.code} for $varName in ${list.code}}"
	override def includes(varName: String): Boolean =
		varName.equals(this.varName) || list.includes(varName) || key.includes(varName) || value.includes(varName)
}

class StringIntMapCompNode(val list: StringNode, val key: ASTNode, val value: ASTNode, val varName: String) extends MapCompNode[String,Int] with StringIntMapNode