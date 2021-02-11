package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types

import scala.collection.mutable

class NestedChildrenIterator(
	val childTypes: List[Types],
	val childrenCost: Int,
	var mainBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
	var miniBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]])
	extends Iterator[List[ASTNode]]
{
	val childrenCosts: Array[Int] = mainBank.keys.toArray
	val arity: Int = childTypes.length
	val costs: Array[Array[Int]] = ProbCosts.getCosts(childrenCost, childrenCosts, childTypes.size)
	var childrenLists: List[List[ASTNode]] = Nil
	var combinationCounter = 0
	var candidates: Array[Iterator[ASTNode]] = Array[Iterator[ASTNode]]()
	var allExceptLast: Array[ASTNode] = Array.empty
	var newCost: Array[Int] = Array[Int]()
	mainBank = mainBank.map(n => (n._1, n._2.filter(c => (!c.includes("key") && !c.includes("var")))))
	//TODO: alternate to filtering the mainBank

	def resetCounter(arity: Int): Unit =
	{
		if (arity == 1) combinationCounter = 1
		else if (arity == 2) combinationCounter = 3
		else if (arity == 3) combinationCounter = 6
	}

	def newChildrenIterator(cost: Array[Int]): Unit =
	{
		if (miniBank == null) {
			childrenLists = Nil
		} else if (childTypes.length == 1 || (childTypes.length == 2 && combinationCounter == 3)) {
			childrenLists = childTypes.zip(cost).map { case (t, c) => miniBank.getOrElse(c, Nil).view.filter(c => t.equals(c.nodeType)).toList }
		} else if (childTypes.length == 2 && combinationCounter == 2) {
			childrenLists = List(
				miniBank.getOrElse(cost(0), Nil).filter(c => childTypes(0).equals(c.nodeType)).toList,
				mainBank(cost(1))
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) { c.updateValues } else { c }))
		} else if (childTypes.length == 2 && combinationCounter == 1) {
			childrenLists = List(
				mainBank(cost.head)
					.filter(c => childTypes(0).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c),
				miniBank.getOrElse(cost(1), Nil)
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList)
		} else if (childTypes.length == 3 && combinationCounter == 6) {
			childrenLists = List(
				miniBank.getOrElse(cost(0), Nil)
					.filter(c => childTypes(0).equals(c.nodeType))
					.toList,
				mainBank(cost(1))
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c),
				mainBank(cost(2))
					.filter(c => childTypes(2).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c)
				)
		} else if (childTypes.length == 3 && combinationCounter == 5) {
			childrenLists = List(
				mainBank(cost(0))
					.filter(c => childTypes(0).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c),
				miniBank.getOrElse(cost(1), Nil)
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList,
				mainBank(cost(2))
					.filter(c => childTypes(2).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c)
				)
		} else if (childTypes.length == 3 && combinationCounter == 4) {
			childrenLists = List(
				mainBank(cost(0))
					.filter(c => childTypes(0).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c),
				mainBank(cost(1))
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c),
				miniBank.getOrElse(cost(2), Nil)
					.filter(c => childTypes(2).equals(c.nodeType))
					.toList)
		} else if (childTypes.length == 3 && combinationCounter == 3) {
			childrenLists = List(
				miniBank.getOrElse(cost(0), Nil)
					.filter(c => childTypes(0).equals(c.nodeType))
					.toList,
				miniBank.getOrElse(cost(1), Nil)
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList,
				mainBank(cost(2))
					.filter(c => childTypes(2).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c)
				)
		} else if (childTypes.length == 3 && combinationCounter == 2) {
			childrenLists = List(
				mainBank(cost(0))
					.filter(c => childTypes(0).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c),
				miniBank.getOrElse(cost(1), Nil)
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList,
				miniBank.getOrElse(cost(2), Nil)
					.filter(c => childTypes(2).equals(c.nodeType))
					.toList)
		} else if (childTypes.length == 3 && combinationCounter == 1) {
			childrenLists = List(
				miniBank.getOrElse(cost(0), Nil)
					.filter(c => childTypes(0).equals(c.nodeType))
					.toList,
				mainBank(cost(1))
					.filter(c => childTypes(1).equals(c.nodeType))
					.toList
					.map(c => if (c.values.length != Contexts.contextLen) c.updateValues else c),
				miniBank.getOrElse(cost(2), Nil)
					.filter(c => childTypes(2).equals(c.nodeType))
					.toList)
		}
		combinationCounter = combinationCounter - 1
	}

	def resetIterators(cost: Array[Int]): Unit =
	{
		newChildrenIterator(cost)
		candidates = if (childrenLists.exists(l => l.isEmpty)) {
			childrenLists.map(_ => Iterator.empty).toArray
		} else {
			childrenLists.map(l => l.iterator).toArray
		}
		if (!candidates.isEmpty && candidates(0).hasNext) {
			allExceptLast = candidates.dropRight(1).map(_.next())
		}
	}

	var next_child: Option[List[ASTNode]] = None
	val costsIterator = costs.iterator

	def getNextChild(): Option[List[ASTNode]] =
	{
		if (!candidates.isEmpty) {
			while (true) {
				if (candidates.last.hasNext) {
					val children = allExceptLast.toList :+ candidates.last.next()
					//TODO: maybe recompute the updated values over here?
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
				if (!costsIterator.hasNext && combinationCounter == 0) {
					return
				} // No more costs combinations available

				else if (combinationCounter != 0) {
					resetIterators(newCost) // Same cost, different children combinations
				}

				else if (combinationCounter == 0 && costsIterator.hasNext) {
					// Different cost combination, explored all children combinations for that cost
					resetCounter(childTypes.length)
					newCost = costsIterator.next()
					resetIterators(newCost)
				}
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