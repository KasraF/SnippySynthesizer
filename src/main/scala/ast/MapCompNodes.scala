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

	override val values: List[List[(K,V)]] = {
		val entries = key.values.zip(value.values)
		val rs = list.values
		  .indices
		  .map(i => entries.slice(
			  i * entries.length / list.values.length,
			  (i+1) * entries.length / list.values.length))
		  .map(l => l.map { case (k: Any, v: Any) => k.asInstanceOf[K] -> v.asInstanceOf[V] }.distinct)
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

trait FilteredMapNode[K,V] extends ASTNode
{
	val map: ASTNode
	val filter: ASTNode
	val keyName: String

	override val nodeType: Types = map.nodeType

	override val values: List[List[(K,V)]] = {
		map.values
		  .indices
		  .map(i => filter.values.asInstanceOf[List[Boolean]].slice(
			  i * filter.values.length / map.values.length,
			  (i+1) * filter.values.length / map.values.length))
		  .zip(map.values.asInstanceOf[List[List[(K,V)]]])
		  .map {
			  case (preds: List[Boolean], entries: List[(K,V)]) =>
				  entries.zip(preds).filter(_._2).map(_._1).distinct
		  }
		  .toList
	}

	override val height: Int = 1 + Math.max(map.height, filter.height)
	override val terms: Int = 1 + map.terms + filter.terms
	override val children: Iterable[ASTNode] = List(map, filter)
	override val code: String = s"{$keyName: ${map.code}[$keyName] for $keyName in ${map.code} if ${filter.code}}"
	override def includes(varName: String): Boolean =
		varName.equals(this.keyName) || map.includes(varName) || filter.includes(varName)
}

class StringIntMapCompNode(val list: StringNode, val key: StringNode, val value: IntNode, val varName: String) extends MapCompNode[String,Int] with StringIntMapNode
class StringIntFilteredMapNode(val map: StringIntMapNode, val filter: BoolNode, val keyName: String) extends FilteredMapNode[String,Int] with StringIntMapNode