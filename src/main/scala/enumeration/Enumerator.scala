package enumeration

import ast.{ASTNode, VocabFactory, VocabMaker}

class Enumerator(val vocab: VocabFactory) extends Iterator[ASTNode]{
  override def toString(): String = "enumeration.Enumerator"

  var nextProgram: Option[ASTNode] = None
  override def hasNext: Boolean = {
    nextProgram = getNextProgram()
    !nextProgram.isEmpty
  }

  override def next(): ASTNode = {
    if (nextProgram.isEmpty) {
      nextProgram = getNextProgram()
    }
    nextProgram.get
  }

  var currIter = vocab.leaves
  var childrenIterator: Iterator[List[ASTNode]] = Iterator.single(Nil)
  var rootMaker: VocabMaker = currIter.next()
  def advanceRoot(): Boolean = {
    if (!currIter.hasNext) return false
    rootMaker = currIter.next()
    childrenIterator = if (rootMaker.arity == 0)
      Iterator.single(Nil)
    else ???
    true
  }
  def changeLevel():Unit = {
    currIter = vocab.nonLeaves
    advanceRoot()
  }
  def getNextProgram(): Option[ASTNode] = {
    if(childrenIterator.hasNext) {
      Some(rootMaker(childrenIterator.next()))
    }
    else if (currIter.hasNext) {
      if (advanceRoot())
        getNextProgram()
      else None
    }
    else {
      changeLevel()
      getNextProgram()
    }
  }
}
