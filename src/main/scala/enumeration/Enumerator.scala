package enumeration

import ast.{ASTNode, VocabFactory, VocabMaker}

import scala.collection.mutable

class Enumerator(val vocab: VocabFactory) extends Iterator[ASTNode]{
  override def toString(): String = "enumeration.Enumerator"

  var nextProgram: Option[ASTNode] = None
  override def hasNext: Boolean = if (!nextProgram.isEmpty) true
  else {
    nextProgram = getNextProgram()
    !nextProgram.isEmpty
  }

  override def next(): ASTNode = {
    if (nextProgram.isEmpty) {
      nextProgram = getNextProgram()
    }
    val res = nextProgram.get
    nextProgram = None
    res
  }

  var currIter = vocab.leaves
  var childrenIterator: Iterator[List[ASTNode]] = Iterator.single(Nil)
  var rootMaker: VocabMaker = currIter.next()
  var prevLevelProgs: mutable.MutableList[ASTNode] = mutable.MutableList()
  var currLevelProgs: mutable.MutableList[ASTNode] = mutable.MutableList()
  def advanceRoot(): Boolean = {
    if (!currIter.hasNext) return false
    rootMaker = currIter.next()
    childrenIterator = if (rootMaker.arity == 0)
      Iterator.single(Nil)
    else new ChildrenIterator(prevLevelProgs.toList,rootMaker.arity,height)
    true
  }
  var height = 0
  def changeLevel():Unit = {
    currIter = vocab.nonLeaves
    height += 1
    prevLevelProgs ++= currLevelProgs
    currLevelProgs.clear()
    advanceRoot()
  }
  def getNextProgram(): Option[ASTNode] = {
    var res : Option[ASTNode] = None
    while(res.isEmpty) {
      if (childrenIterator.hasNext) {
        res = Some(rootMaker(childrenIterator.next()))
      }
      else if (currIter.hasNext) {
        if (!advanceRoot())
          return None
      }
      else {
        changeLevel()
      }
    }
    currLevelProgs += res.get
    res
  }
}