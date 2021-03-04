package edu.ucsd.snippy.vocab

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait BasicVocabMaker extends VocabMaker with Iterator[ASTNode]
{
	val returnType: Types
	var childIterator: Iterator[List[ASTNode]] = _
	var contexts: List[Map[String, Any]] = _

	// Causes "Too many open files" error :/
	// var size_log = new FileOutputStream("output.txt", true)

	override def hasNext: Boolean = childIterator != null && childIterator.hasNext

	def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode

	override def next: ASTNode =
		this (this.childIterator.next(), this.contexts)

	override def rootCost: Int = //if
		//(nodeType == classOf[IntLiteral] || nodeType == classOf[StringLiteral]
		//|| nodeType == classOf[BoolLiteral] || nodeType == classOf[StringVariable]
		//|| nodeType == classOf[BoolVariable] || nodeType == classOf[IntVariable]) {
		//ProbUpdate.priors(nodeType, Some(head))
	//} else {
		ProbUpdate.priors(nodeType, None)
	//}

	override def init(programs: List[ASTNode], contexts: List[Map[String, Any]], vocabFactory: VocabFactory, height: Int): Iterator[ASTNode] =
	{
		this.contexts = contexts

		this.childIterator = if (this.arity == 0) {
			// No children needed, but we still return 1 value
			Iterator.single(Nil)
		} else if (this.childTypes.map(t => programs.filter(c => t.equals(c.nodeType))).exists(_.isEmpty)) {
			Iterator.empty
		} else {
			new ChildrenIterator(programs, childTypes, height)
		}
		this
	}

	def probe_init(
		vocabFactory: VocabFactory,
		costLevel: Int,
		contexts: List[Map[String, Any]],
		bank: mutable.Map[Int, ArrayBuffer[ASTNode]],
		nested: Boolean,
		miniBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]],
		mini: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]): Iterator[ASTNode] =
	{
		this.contexts = contexts
		this.childIterator = if (this.arity == 0 && this.rootCost == costLevel) {
			// No children needed, but we still return 1 value
			Iterator.single(Nil)
		} else if (mini == null && nested) {
			Iterator.empty
		} else if (this.rootCost < costLevel && !nested) { //TODO: add condition (arity != 0)
			val childrenCost = costLevel - this.rootCost
			val children = new ProbChildrenIterator(this.childTypes, childrenCost, bank)
			children
		} else if (this.rootCost < costLevel && nested) { //TODO: add condition (arity != 0)
			val childrenCost = costLevel - this.rootCost
			val children = new NestedChildrenIterator(this.childTypes, childrenCost, new Contexts(contexts), bank, mini)
			children
		} else {
			Iterator.empty
		}
		this
	}
}