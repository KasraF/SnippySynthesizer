package edu.ucsd.snippy.vocab

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.ChildrenIterator

trait BasicVocabMaker extends VocabMaker with Iterator[ASTNode]
{
	val childTypes: List[Types]
	val returnType: Types

	var childIterator: Iterator[List[ASTNode]] = _
	var contexts: List[Map[String, Any]] = _

	def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode

	override def hasNext: Boolean = childIterator != null && childIterator.hasNext

	override def next: ASTNode =
	{
		this(this.childIterator.next(), this.contexts)
	}

	override def init(
	  progs: List[ASTNode],
	  contexts : List[Map[String, Any]],
	  vocabFactory: VocabFactory,
	  height: Int) : Iterator[ASTNode] =
	{
		this.contexts = contexts

		this.childIterator = if (this.arity == 0) {
			// No children needed, but we still return 1 value
			Iterator.single(Nil)
		} else if (this.childTypes.map(t => progs.filter(c => t.equals(c.nodeType))).exists(_.isEmpty)) {
			// Don't have any candidates for one or more children
			Iterator.empty
		} else {
			new ChildrenIterator(progs, childTypes, height)
		}

		this
	}
}