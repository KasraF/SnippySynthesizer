package ast

import ast.Types.Types

trait MapCompNode[K,V] extends MapNode[K,V]
{
	val list: IterableNode
	val key: ASTNode
	val value: ASTNode
	val varName: String

	assert(key.values.length == value.values.length, "Key and value did not match")

	override val keyType: Types = Types.childOf(list.nodeType)
	override val valType: Types = value.nodeType

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

trait FilteredMapNode[K,V] extends MapNode[K,V]
{
	val map: MapNode[K,V]
	val filter: BoolNode
	val keyName: String

	override val keyType: Types = map.keyType
	override val valType: Types = map.valType

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

	def make(map: MapNode[K,V], filter: BoolNode, keyName: String) : FilteredMapNode[K,V]
}

class StringStringMapCompNode    (val list: StringNode,       val key: StringNode, val value: StringNode, val varName: String) extends MapCompNode[String,String]
class StringIntMapCompNode       (val list: StringNode,       val key: StringNode, val value: IntNode,    val varName: String) extends MapCompNode[String,Int]
class StringListStringMapCompNode(val list: ListNode[String], val key: StringNode, val value: StringNode, val varName: String) extends MapCompNode[String,String]
class StringListIntMapCompNode   (val list: ListNode[String], val key: StringNode, val value: IntNode,    val varName: String) extends MapCompNode[String,Int]
class IntStringMapCompNode       (val list: ListNode[Int],    val key: IntNode,    val value: StringNode, val varName: String) extends MapCompNode[Int,String]
class IntIntMapCompNode          (val list: ListNode[String], val key: IntNode,    val value: IntNode,    val varName: String) extends MapCompNode[Int,Int]

class StringStringFilteredMapNode(val map: MapNode[String,String], val filter: BoolNode, val keyName: String) extends FilteredMapNode[String,String]
{
	override def make(map: MapNode[String, String], filter: BoolNode, keyName: String): FilteredMapNode[String, String] =
		new StringStringFilteredMapNode(map, filter, keyName)
}
class StringIntFilteredMapNode(val map: MapNode[String,Int], val filter: BoolNode, val keyName: String) extends FilteredMapNode[String,Int]
{
	override def make(map: MapNode[String, Int], filter: BoolNode, keyName: String): FilteredMapNode[String, Int] =
		new StringIntFilteredMapNode(map, filter, keyName)
}
class IntStringFilteredMapNode(val map: MapNode[Int,String], val filter: BoolNode, val keyName: String) extends FilteredMapNode[Int,String]
{
	override def make(map: MapNode[Int, String], filter: BoolNode, keyName: String): FilteredMapNode[Int, String] =
		new IntStringFilteredMapNode(map, filter, keyName)
}
class IntIntFilteredMapNode(val map: MapNode[Int,Int], val filter: BoolNode, val keyName: String) extends FilteredMapNode[Int,Int]
{
	override def make(map: MapNode[Int, Int], filter: BoolNode, keyName: String): FilteredMapNode[Int, Int] =
		new IntIntFilteredMapNode(map, filter, keyName)
}
