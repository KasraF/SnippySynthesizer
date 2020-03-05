package ast

import ast.Types.Types
import enumeration.ChildrenIterator

trait VocabMaker extends Iterator[ASTNode]
{
	val arity: Int
	val childTypes: List[Types]
	val returnType: Types
	val head: String

	var childIterator: Iterator[List[ASTNode]] = _
	var contexts: List[Map[String, Any]] = _

	def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode

	def hasNext: Boolean = childIterator != null && childIterator.hasNext

	def next: ASTNode =
	{
		this(this.childIterator.next(), this.contexts)
	}

	def init(progs: List[ASTNode], contexts : List[Map[String, Any]], height: Int) : Unit =
	{
		this.contexts = contexts
		if (this.arity == 0) {
			// No children needed, but we still return 1 value
			this.childIterator = Iterator.single(Nil)
		} else if (this.childTypes.map(t => progs.filter(_.nodeType == t)).exists(_.isEmpty)) {
			// Don't have any candidates for one or more children
			this.childIterator = Iterator.empty
		} else {
			this.childIterator = new ChildrenIterator(progs, childTypes, height)
		}
	}

	def canMake(children: List[ASTNode]): Boolean =
		children.length == arity && children.zip(childTypes).forall(pair => pair._1.nodeType == pair._2)
}

class VocabFactory(
	val leavesMakers: List[VocabMaker],
	val nodeMakers  : List[VocabMaker])
{
	def leaves(): Iterator[VocabMaker] = leavesMakers.iterator

	def nonLeaves(): Iterator[VocabMaker] = nodeMakers.iterator
}

object VocabFactory
{
	def apply(vocabMakers: Seq[VocabMaker]): VocabFactory =
	{
		val (leavesMakers, nodeMakers) = vocabMakers.toList.partition(m => m.arity == 0)
		new VocabFactory(leavesMakers, nodeMakers)
	}
}