package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class NestedChildrenIterator(
	val childTypes: List[Types],
	val childrenCost: Int,
	val contexts: Contexts,
	var mainBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
	var miniBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]])
	extends Iterator[List[ASTNode]]
{
	val childrenCosts: Array[Int] = mainBank.keys.toArray
	val arity: Int = childTypes.length
	val costs: Array[Array[Int]] = ProbCosts.getCosts(childrenCost, childrenCosts, childTypes.size)
	var indices: Array[ArrayBuffer[Int]] = ProbCosts.getIndices(arity)
	var childrenLists: List[List[ASTNode]] = List[List[ASTNode]]()
	var combinationCounter = 0
	var candidates: Array[Iterator[ASTNode]] = Array[Iterator[ASTNode]]()
	var allExceptLast: Array[ASTNode] = Array.empty
	var newCost: Array[Int] = Array[Int]()
	mainBank = mainBank.map(n => (n._1, n._2.filter(c => !c.includes("key") && !c.includes("var"))))
	//TODO: alternate to filtering the mainBank

	def resetIndices(arity: Int): Unit =
	{
		indices = ProbCosts.getIndices(arity)
		indicesIterator = indices.iterator
	}

	def newChildrenIterator(cost: Array[Int]): Unit = {
		val varBankIndex = indicesIterator.next()
		val mainBankIndex = ArrayBuffer.range(0, arity) --= varBankIndex
		val elements: mutable.Map[Int, List[ASTNode]] = mutable.Map[Int, List[ASTNode]]()

		varBankIndex.foreach(c => elements += (c -> miniBank.getOrElse(cost(c), Nil)
			.filter(d => childTypes(c).equals(d.nodeType)).toList))

		mainBankIndex.foreach(c => elements += (c -> mainBank.getOrElse(cost(c), Nil)
			.filter(d => childTypes(c).equals(d.nodeType)).toList
			.map(c => if (c.values.length != contexts.contextLen) c.updateValues(contexts) else c)))

		val sortedElem = mutable.Map(elements.toList.sortBy(_._1):_*)
		childrenLists = sortedElem.toList.map(_._2)
	}

	def resetIterators(cost: Array[Int]): Unit = {

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
	val costsIterator: Iterator[Array[Int]] = costs.iterator
	var indicesIterator: Iterator[ArrayBuffer[Int]] = Iterator()

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
				if (!costsIterator.hasNext && !indicesIterator.hasNext) {
					return
				} // No more costs combinations available

				else if (indicesIterator.hasNext) {
					resetIterators(newCost) // Same cost, different children combinations
				}

				else if (!indicesIterator.hasNext && costsIterator.hasNext) {
					// Different cost combination, explored all children combinations for that cost
					resetIndices(childTypes.length)
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