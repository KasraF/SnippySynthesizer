package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.vocab.{VocabFactory, VocabMaker}

import java.io.FileOutputStream
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ProbEnumerator(
	override val vocab: VocabFactory,
	override val oeManager: OEValuesManager,
	override val contexts: List[Map[String, Any]],
	var nested: Boolean,
	var initCost: Int,
	var mainBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
	var vars: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
	var endCost: Int) extends Enumerator
{
	override def toString(): String = "enumeration.Enumerator"

	// TODO Terrible name :/
	val contextsObj = new Contexts(contexts)
	var nextProgram: Option[ASTNode] = None

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

	var costLevel: Int = initCost
	var currIterator: Iterator[VocabMaker] = _
	val currLevelPrograms: mutable.ArrayBuffer[ASTNode] = mutable.ArrayBuffer()
	val varBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, ArrayBuffer[ASTNode]]] = mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]]()
	val totalLeaves: List[VocabMaker] = vocab.leaves().toList.distinct ++ vocab.nonLeaves().toList.distinct

	// Causes "Too many open files" error :/
	// var size_log = new FileOutputStream("output.txt", true)

	ProbUpdate.probMap = ProbUpdate.createProbMap(vocab)
	ProbUpdate.priors = ProbUpdate.createPrior(vocab)
	resetEnumeration()

	mainBank.values
		.flatten
		.toList
		.map(p => if (p.values.length != this.contexts.length) {
			oeManager.isRepresentative(p.updateValues(this.contextsObj))
		} else {
			oeManager.isRepresentative(p)
		}) // OE

	if (vars != null) vars.values.flatten.toList.map(p => oeManager.isRepresentative(p)) // OE

	var rootMaker: Iterator[ASTNode] = currIterator.next().
		probe_init(vocab, costLevel, contexts, mainBank, nested, varBank, vars)

	def resetEnumeration(): Unit =
	{
		currIterator = totalLeaves.sortBy(_.rootCost).iterator
		rootMaker = currIterator.next().probe_init(vocab, costLevel, contexts, mainBank, nested, varBank, vars)
		currLevelPrograms.clear()
		oeManager.clear()
	}

	/**
	 * This method moves the rootMaker to the next possible non-leaf. Note that this does not
	 * change the level/height of generated programs.
	 *
	 * @return False if we have exhausted all non-leaf AST nodes.
	 */
	def advanceRoot(): Boolean =
	{
		rootMaker = null
		while (rootMaker == null || !rootMaker.hasNext) {
			if (!currIterator.hasNext) {
				return false
			}
			val next = currIterator.next()
			rootMaker = next.probe_init(vocab, costLevel, contexts, mainBank, nested, varBank, vars)
			if ((next.nodeType == classOf[StringToStringListCompNode]) ||
				(next.nodeType == classOf[StringToIntListCompNode]) ||
				(next.nodeType == classOf[IntToStringListCompNode]) ||
				(next.nodeType == classOf[IntToIntListCompNode]) ||
				(next.nodeType == classOf[StringStringMapCompNode]) ||
				(next.nodeType == classOf[StringIntMapCompNode]) ||
				(next.nodeType == classOf[StringListStringMapCompNode]) ||
				(next.nodeType == classOf[StringListIntMapCompNode]) ||
				(next.nodeType == classOf[IntStringMapCompNode]) ||
				(next.nodeType == classOf[IntIntMapCompNode]) ||
				(next.nodeType == classOf[StringStringFilteredMapNode]) ||
				(next.nodeType == classOf[StringIntFilteredMapNode]) ||
				(next.nodeType == classOf[IntStringFilteredMapNode]) ||
				(next.nodeType == classOf[IntIntFilteredMapNode])) {
				nested = false
			}
		}
		true
	}

	def updateBank(program: ASTNode): Unit =
	{ //TODO: Add check to only add non-variable programs,
		// TODO: aren't only var programs being generated except for arity 0 programs?
		if (!mainBank.contains(program.cost)) {
			mainBank(program.cost) = ArrayBuffer(program)
		} else {
			mainBank(program.cost) += program
		}
	}

	def changeLevel(): Boolean =
	{
		currIterator = totalLeaves.sortBy(_.rootCost).iterator //todo: more efficient
		if (!nested) for (p <- currLevelPrograms) updateBank(p)
		costLevel += 1
		currLevelPrograms.clear()
		advanceRoot()
	}

	var progs = 0
	def getNextProgram: Option[ASTNode] =
	{
		var res: Option[ASTNode] = None
		// Iterate while no non-equivalent program is found
		while (res.isEmpty) {
			if (costLevel > endCost) return None

			if (rootMaker.hasNext) {
				val program = rootMaker.next
				if (program.values.exists(_.isDefined) && oeManager.isRepresentative(program)) {
					res = Some(program)
					progs += 1
				}
			}
			else if (currIterator.hasNext) {
				if (!advanceRoot()) changeLevel()
			}
			else if (!changeLevel()) {
				changeLevel()
			}
		}
		currLevelPrograms += res.get
		//Console.withOut(size_log) { println(nested, currLevelPrograms.takeRight(1).map(c => (c.code, c.values, c.cost))) }
		res
	}

	override def programsSeen: Int = progs
}
