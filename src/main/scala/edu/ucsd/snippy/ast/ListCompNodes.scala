package edu.ucsd.snippy.ast

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.Contexts

trait ListCompNode[T] extends ListNode[T]
{
	val list: ListNode[_]
	val map: ASTNode
	val varName: String

	override val childType: Types = map.nodeType
	override val values: List[Option[List[T]]] = {
		var rs: List[Option[List[T]]] = Nil
		var start = 0

		val deltas = list.values.map {
			case Some(lst: List[_]) => Some(lst.length)
			case _ => None
		}

		for (delta <- deltas) {
			delta match {
				case Some(delta) =>
					// TODO Test this carefully
					val values = map.values.asInstanceOf[List[Option[T]]].slice(start, start + delta)
					if (values.forall(_.isDefined)) {
						rs = rs :+ Some(values.map(_.get))
					} else {
						rs = rs :+ None
					}

					start += delta
				case None =>
					// We didn't add None lists to the context, so just add None and don't change start
					rs +:= None
			}
		}

		rs
	}
	override val height: Int = 1 + Math.max(list.height, map.height)
	override val terms: Int = 1 + list.terms + map.terms
	override val children: Iterable[ASTNode] = List(list, map)
	override val code: String = s"[${map.code} for $varName in ${list.code}]"
	override protected val parenless: Boolean = true
	override lazy val usesVariables: Boolean = list.usesVariables || map.usesVariables

	override def includes(varName: String): Boolean =
		varName.equals(this.varName) || list.includes(varName) || map.includes(varName)
	override def updateValues(contexts: Contexts): ListCompNode[T] = ???
}

class StringToStringListCompNode(val list: ListNode[String], val map: StringNode, val varName: String) extends ListCompNode[String]
class StringToIntListCompNode(val list: ListNode[String], val map: IntNode, val varName: String) extends ListCompNode[Int]
class IntToStringListCompNode(val list: ListNode[Int], val map: StringNode, val varName: String) extends ListCompNode[String]
class IntToIntListCompNode(val list: ListNode[Int], val map: IntNode, val varName: String) extends ListCompNode[Int]