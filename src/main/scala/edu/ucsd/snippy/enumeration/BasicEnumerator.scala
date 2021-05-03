package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.vocab.{VocabFactory, VocabMaker}

import scala.collection.mutable

class BasicEnumerator(
	override val vocab: VocabFactory,
	override val oeManager: OEValuesManager,
	override val contexts: List[Map[String, Any]]) extends Enumerator
{
	override def toString(): String = "enumeration.Enumerator"

	var nextProgram: Option[ASTNode] = None
	var currIter: Iterator[VocabMaker] = vocab.leaves()
	val prevLevelProgs: mutable.ListBuffer[ASTNode] = mutable.ListBuffer()
	val currLevelProgs: mutable.ListBuffer[ASTNode] = mutable.ListBuffer()
	var height = 0
	var rootMaker: Iterator[ASTNode] =
		currIter.next().init(currLevelProgs.toList, contexts, vocab, height)
	var progs = 0

	// TODO We don't use this rn
	// ProbUpdate.probMap = ProbUpdate.createProbMap(vocab)
	// ProbUpdate.priors = ProbUpdate.createPrior(vocab)

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

	/**
	 * This method moves the rootMaker to the next possible non-leaf. Note that this does not
	 * change the level/height of generated programs.
	 *
	 * @return False if we have exhausted all non-leaf AST nodes.
	 */
	def advanceRoot(): Boolean =
	{
		// We may not have any children for a root
		rootMaker = null
		while (rootMaker == null || !rootMaker.hasNext) {
			// We are out of programs!
			if (!currIter.hasNext) return false
			val next = currIter.next()
			rootMaker = next.init(prevLevelProgs.toList, contexts, this.vocab, height)
		}

		true
	}

	/**
	 * This method resets the variables to begin enumerating the next level (taller) trees.
	 *
	 * @return False if the current level failed to generate any new programs.
	 */
	def changeLevel(): Boolean =
	{
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

		// Iterate while no non-equivalent program is found
		while (res.isEmpty) {
			if (rootMaker.hasNext) {
				val prog = rootMaker.next

				if (prog.values.exists(_.isDefined) && oeManager.isRepresentative(prog)) {
					res = Some(prog)
					progs += 1
				}
			}
			else if (currIter.hasNext) {
				if (!advanceRoot()) {
					if (!changeLevel()) return None
				}
			}
			else if (!changeLevel()) {
				return None
			}
		}
		currLevelProgs += res.get
		res
	}

	override def programsSeen: Int = progs
}