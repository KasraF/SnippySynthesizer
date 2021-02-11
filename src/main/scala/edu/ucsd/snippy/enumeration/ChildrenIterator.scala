package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types

class ChildrenIterator(
	val childrenCandidates: List[ASTNode],
	val childTypes: List[Types],
	val currHeight: Int) extends Iterator[List[ASTNode]]
{
	val childrenLists: List[List[ASTNode]] = childTypes.map(t => childrenCandidates.filter(c => t.equals(c.nodeType)))
	val candidates: Array[Iterator[ASTNode]] = childrenLists.map(l => l.iterator).toArray
	val allExceptLast: Array[ASTNode] = candidates.dropRight(1).map(_.next())
	var next_child: Option[List[ASTNode]] = None

	def getNextChild(): Unit =
	{
		while (next_child.isEmpty) {
			if (candidates.last.hasNext) {
				val children = allExceptLast.toList :+ candidates.last.next()
				if (children.exists(_.height == currHeight - 1)) {
					next_child = Some(children)
				}
			}
			else { //roll
				val next = candidates.zipWithIndex.findLast { case (iter, _) => iter.hasNext }
				if (next.isEmpty) {
					return
				} else {
					val (iter, idx) = next.get
					allExceptLast.update(idx, iter.next)
					for (i <- idx + 1 until candidates.length - 1) {
						candidates.update(i, childrenLists(i).iterator)
						allExceptLast.update(i, candidates(i).next())
					}
					candidates.update(candidates.length - 1, childrenLists.last.iterator)
				}
			}
		}
	}

	override def hasNext: Boolean =
	{
		if (next_child.isEmpty) getNextChild()
		next_child.isDefined
	}

	override def next(): List[ASTNode] =
	{
		if (next_child.isEmpty) getNextChild()
		val res = next_child.get
		next_child = None
		res
	}
}