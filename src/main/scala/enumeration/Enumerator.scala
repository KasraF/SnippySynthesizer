package enumeration

import ast.{ASTNode, VocabFactory, VocabMaker}
import trace.DebugPrints.dprintln

import scala.collection.mutable

class Enumerator(
	val vocab: VocabFactory,
	val oeManager: OEValuesManager,
	val contexts: List[Map[String, Any]])
	extends Iterator[ASTNode]
{
	override def toString(): String = "enumeration.Enumerator"

	var currIter: Iterator[VocabMaker] = vocab.leaves()
	var childrenIterator: Iterator[List[ASTNode]] = Iterator.single(Nil)
	var rootMaker: VocabMaker = currIter.next()
	var prevLevelProgs: mutable.ListBuffer[ASTNode] = mutable.ListBuffer()
	var currLevelProgs: mutable.ListBuffer[ASTNode] = mutable.ListBuffer()
	var nextProgram: Option[ASTNode] = None
	var height = 0

	override def hasNext: Boolean =
		if (nextProgram.isDefined) {
			true
		} else {
			nextProgram = getNextProgram
			nextProgram.isDefined
		}

	override def next(): ASTNode =
	{
		if (nextProgram.isEmpty) nextProgram = getNextProgram
		val res = nextProgram.get
		nextProgram = None
		res
	}

	def advanceRoot(): Boolean =
	{
		if (!currIter.hasNext) return false

		rootMaker = currIter.next()
		childrenIterator = if (rootMaker.arity == 0) {
			Iterator.single(Nil)
		} else {
			new ChildrenIterator(prevLevelProgs.toList, rootMaker.childTypes, height)
		}

		true
	}

	def changeLevel(): Boolean =
	{
		dprintln(currLevelProgs.length)
		if (currLevelProgs.isEmpty) return false

		currIter = vocab.nonLeaves()
		height += 1
		prevLevelProgs ++= currLevelProgs
		currLevelProgs.clear()
		advanceRoot()
	}

	def getNextProgram: Option[ASTNode] =
	{
		var res: Option[ASTNode] = None

      while (res.isEmpty) {
			if (childrenIterator.hasNext) {
				val children = childrenIterator.next()
				if (rootMaker.canMake(children)) {
					val prog = rootMaker(children, contexts)
					if (oeManager.isRepresentative(prog)) {
						res = Some(prog)
					}
				}
			}
			else if (currIter.hasNext) {
				if (!advanceRoot()) {
					return None
				}
			}
			else {
				if (!changeLevel()) {
					return None
				}
			}
		}
		currLevelProgs += res.get
		res
	}
}
