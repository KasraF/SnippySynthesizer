package edu.ucsd.snippy.ast

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.Contexts

trait ListCompNode[T] extends ListNode[T]
{
	val list: ListNode[_]
	val map: ASTNode
	val varName: String

	override val childType: Types = map.nodeType
	override val values: List[List[T]] = {
		var rs: List[List[_]] = Nil;
		var start = 0;
		for (delta <- list.values.map(_.asInstanceOf[List[_]].length)) {
			rs = rs :+ map.values.slice(start, start + delta)
			start += delta
		}
		rs.asInstanceOf[List[List[T]]]
	}
	override val height: Int = 1 + Math.max(list.height, map.height)
	override val terms: Int = 1 + list.terms + map.terms
	override val children: Iterable[ASTNode] = List(list, map)
	override val code: String = s"[${map.code} for $varName in ${list.code}]"
	override protected val parenless: Boolean = true
	override def includes(varName: String): Boolean =
		varName.equals(this.varName) || list.includes(varName) || map.includes(varName)
	override lazy val usesVariables: Boolean = list.usesVariables || map.usesVariables
	override def updateValues(contexts: Contexts): ASTNode = null
}

class StringToStringListCompNode(val list: ListNode[String], val map: StringNode, val varName: String) extends ListCompNode[String]
class StringToIntListCompNode(val list: ListNode[String], val map: IntNode, val varName: String) extends ListCompNode[Int]
class IntToStringListCompNode(val list: ListNode[Int], val map: StringNode, val varName: String) extends ListCompNode[String]
class IntToIntListCompNode(val list: ListNode[Int], val map: IntNode, val varName: String) extends ListCompNode[Int]