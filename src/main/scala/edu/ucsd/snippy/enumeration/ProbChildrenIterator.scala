package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types

import scala.collection.mutable

class ProbChildrenIterator(val childTypes: List[Types], val childrenCost: Int, val bank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]) extends Iterator[List[ASTNode]]
{
	var childrenLists: List[List[ASTNode]] = Nil
	val childrenCosts: Array[Int] = bank.keys.toArray
	val costs: Array[Array[Int]] = ProbCosts.getCosts(childrenCost, childrenCosts, childTypes.size)
	var candidates: Array[Iterator[ASTNode]] = Array[Iterator[ASTNode]]()
	var allExceptLast: Array[ASTNode] = Array.empty
	var next_child: Option[List[ASTNode]] = None
	val costsIterator: Iterator[Array[Int]] = costs.iterator

	def resetIterators(cost: Array[Int]): Unit =
	{
		childrenLists = childTypes.zip(cost).map { case (t, c) => bank(c).view.filter(c => t.equals(c.nodeType)).toList }
		candidates = if (childrenLists.exists(l => l.isEmpty)) {
			childrenLists.map(_ => Iterator.empty).toArray
		} else {
			childrenLists.map(l => l.iterator).toArray
		}
		if (!candidates.isEmpty && candidates(0).hasNext) {
			allExceptLast = candidates.dropRight(1).map(_.next())
		}
	}

	def getNextChild(): Option[List[ASTNode]] =
	{
		if (!candidates.isEmpty) {
			while (true) {
				if (candidates.last.hasNext) {
					val children = allExceptLast.toList :+ candidates.last.next()
					return Some(children)
				}
				else { //roll
					val next = candidates.zipWithIndex.findLast { case (iter, _) => iter.hasNext }
					if (next.isEmpty) {
						return None
					} else {
						val (iter, idx) = next.get
						allExceptLast.update(idx, iter.next)
						for (i <- idx + 1 until candidates.length - 1) {
							candidates.update(i, childrenLists(i).iterator)
							allExceptLast.update(i, candidates(i).next())
						}
					}
					candidates.update(candidates.length - 1, childrenLists.last.iterator)
				}
			}
		}
		None
	}

	def getChild(): Unit =
	{
		next_child = None
		while (next_child.isEmpty) {
			next_child = getNextChild()
			if (next_child.isEmpty) {
				if (!costsIterator.hasNext) return
				val newCost = costsIterator.next()
				resetIterators(newCost)
			}
		}
	}

	override def hasNext: Boolean =
	{
		if (next_child.isEmpty) getChild()
		next_child.isDefined
	}

	override def next(): List[ASTNode] =
	{
		if (next_child.isEmpty) getChild()
		val res = next_child.get
		next_child = None
		res
	}
}