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

	override val values: List[Map[K,V]] = {
		val entries = key.values.zip(value.values)
		val rs = list.values
		  .indices
		  .map(i => entries.slice(
			  i * entries.length / list.values.length,
			  (i+1) * entries.length / list.values.length))
		  .map(l => l.map { case (k: Any, v: Any) => k.asInstanceOf[K] -> v.asInstanceOf[V] }.distinct.toMap)
		  .toList
		rs
	}

	override val height: Int = 1 + Math.max(list.height, value.height)
	override val terms: Int = 1 + list.terms + value.terms
	override val children: Iterable[ASTNode] = List(list, value)
	override protected val parenless: Boolean = true
	override val code: String = s"{${key.code}: ${value.code} for $varName in ${list.code}}"
	override def includes(varName: String): Boolean =
		varName.equals(this.varName) || list.includes(varName) || key.includes(varName) || value.includes(varName)
	override lazy val usesVariables: Boolean = list.usesVariables || key.usesVariables || value.usesVariables
}

trait FilteredMapNode[K,V] extends MapNode[K,V]
{
	val map: MapNode[K,V]
	val filter: BoolNode
	val keyName: String

	override val keyType: Types = map.keyType
	override val valType: Types = map.valType

	override val values: List[Map[K,V]] = filterOp(map, filter)
	override val height: Int = 1 + Math.max(map.height, filter.height)
	override val terms: Int = 1 + map.terms + filter.terms
	override val children: Iterable[ASTNode] = List(map, filter)
	override protected val parenless: Boolean = true
	override val code: String = s"{$keyName: ${map.code}[$keyName] for $keyName in ${map.code} if ${filter.code}}"
	override def includes(varName: String): Boolean =
		varName.equals(this.keyName) || map.includes(varName) || filter.includes(varName)
	override lazy val usesVariables: Boolean = map.usesVariables || filter.usesVariables
	def make(map: MapNode[K,V], filter: BoolNode, keyName: String) : FilteredMapNode[K,V]

	def findKeyVarInNode(node: ASTNode) : Option[VariableNode[_]] =
	{
		node match {
			case n: VariableNode[_] if n.name == keyName => Some(n)
			case n: ASTNode if n.terms > 1 => findKeyVar(n.children)
			case _ => None
		}
	}
	def findKeyVar(nodes: Iterable[ASTNode]) : Option[VariableNode[_]] =
	{
		val keyVar = nodes.map(findKeyVarInNode).filter(_.isDefined)
		if (keyVar.isEmpty) None
		else {
			keyVar.head
		}
	}
	def filterOp(map: MapNode[K,V], filter: BoolNode) : List[Map[K,V]] =
	{
	    val keyNode: VariableNode[_] = findKeyVar(filter.children).get
		val filterValues = map.values
		  .indices
		  .map(i => filter.values.slice(
			  i * filter.values.length / map.values.length,
			  (i+1) * filter.values.length / map.values.length))
		val keyValues = map.values
		  .indices
		  .map(i => keyNode.values.slice(
			  i * keyNode.values.length / map.values.length,
			  (i+1) * keyNode.values.length / map.values.length))
		map.values
		  .zip(keyValues.zip(filterValues).map(tup => tup._1.zip(tup._2)))
		  .map( {
			  case (map: Map[K,V], keyMap: List[(K,Boolean)]) =>
				  map.filter({
					  case (k: K, _) => keyMap.find(_._1.equals(k)).get._2
				  })
		  })
	}
}

class StringStringMapCompNode    (val list: StringNode,       val key: StringNode, val value: StringNode, val varName: String) extends MapCompNode[String,String]
class StringIntMapCompNode       (val list: StringNode,       val key: StringNode, val value: IntNode,    val varName: String) extends MapCompNode[String,Int]
class StringListStringMapCompNode(val list: ListNode[String], val key: StringNode, val value: StringNode, val varName: String) extends MapCompNode[String,String]
class StringListIntMapCompNode   (val list: ListNode[String], val key: StringNode, val value: IntNode,    val varName: String) extends MapCompNode[String,Int]
class IntStringMapCompNode       (val list: ListNode[Int],    val key: IntNode,    val value: StringNode, val varName: String) extends MapCompNode[Int,String]
class IntIntMapCompNode          (val list: ListNode[Int],    val key: IntNode,    val value: IntNode,    val varName: String) extends MapCompNode[Int,Int]

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
