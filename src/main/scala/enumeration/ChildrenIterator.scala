package enumeration

import ast.ASTNode
import ast.Types.Types

import scala.collection.parallel.CollectionConverters._

class ChildrenIterator(val childrenCandidates: List[ASTNode], val childTypes: List[Types], val currHeight: Int) extends Iterator[List[ASTNode]]{
  val childrenLists =
    childTypes.map(t => childrenCandidates.filter(c => c.nodeType == t))
  val candidates = childrenLists.map(l => l.iterator).toArray
  val allExceptLast = candidates.dropRight(1).map(_.next()).toArray
  var next_child: Option[List[ASTNode]] = None
  def getNextChild(): Unit = {
    next_child = None
    while (next_child.isEmpty) {
      if (candidates.last.hasNext) {
        val children = allExceptLast.toList :+ candidates.last.next()
        if (children.exists(child => child.height == currHeight - 1))
          next_child = Some(children)
      }
      else { //roll
        val next = candidates.zipWithIndex.findLast{case (iter,idx) => iter.hasNext}
        if (next.isEmpty) return
        else {
          val (iter,idx) = next.get
          allExceptLast.update(idx,iter.next)
          for (i <- idx + 1 until candidates.length - 1) {
            candidates.update(i,childrenLists(i).iterator)
            allExceptLast.update(i,candidates(i).next())
          }
          candidates.update(candidates.length - 1,childrenLists.last.iterator)
        }
      }
    }
  }
  override def hasNext: Boolean = {
    if (next_child.isEmpty) getNextChild()
    !next_child.isEmpty
  }
  override def next(): List[ASTNode] = {
    if (next_child.isEmpty) getNextChild()
    val res = next_child.get
    next_child = None
    res
  }
}