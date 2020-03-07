package ast

import java.io.ObjectInputValidation

import ast.Types.Types
import enumeration.{ChildrenIterator, Enumerator, InputsValuesManager}

import scala.collection.immutable

trait VocabMaker extends Iterator[ASTNode]
{
	val arity: Int
	def init(progs: List[ASTNode], contexts : List[Map[String, Any]], vocabFactory: VocabFactory, height: Int) : Unit
}

trait BasicVocabMaker extends VocabMaker
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

	override def init(progs: List[ASTNode], contexts : List[Map[String, Any]], vocabFactory: VocabFactory, height: Int) : Unit =
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
}

trait ListCompVocabMaker extends VocabMaker
{
	override val arity: Int = 2
	val inputListType: Types
	val outputListType: Types

	var listIter: Iterator[ASTNode] = _
	var mapVocab: VocabFactory = _
	var contexts: List[Map[String, Any]] = _

	var enumerator: Enumerator = _
	var currList: ASTNode = _
	var childHeight: Int = _
	var varName: String = _

	var nextProg: Option[ASTNode] = None

	override def init(
	  progs: List[ASTNode],
	  contexts : List[Map[String, Any]],
	  vocabFactory: VocabFactory,
	  height: Int) : Unit =
	{
		this.listIter = progs.filter(n => n.nodeType.equals(Types.listOf(this.inputListType))).iterator
		this.childHeight = height - 1
		this.varName = "var"
		this.contexts = contexts

		// Make sure the name is unique
		// TODO We need a nicer way to generate this
		while (contexts.head.contains(this.varName)) this.varName = "_" + this.varName

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.inputListType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringVariable(varName, contexts)
			}
			case Types.Int => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntVariable(varName, contexts)
			}
			case t =>
				assert(false, s"type '$t' not supported for list comp")
				null
		}

		val vocabs = (newVarVocab :: vocabFactory.leavesMakers).reverse :::
		  vocabFactory.nodeMakers
		    .filter(_.isInstanceOf[BasicVocabMaker])
		    .map(_.asInstanceOf[BasicVocabMaker])
		    .filter(v => !Types.isListType(v.returnType))

		this.mapVocab = VocabFactory.apply(vocabs)
		if (this.listIter.hasNext) this.nextList()
	}

	override def hasNext: Boolean =
	{
		if (this.nextProg.isEmpty) getNextProgram()
		this.nextProg.isDefined
	}

	override def next: ASTNode =
	{
		if (this.nextProg.isEmpty) getNextProgram()
		val rs = this.nextProg.get
		this.nextProg = None
		rs
	}

	private def getNextProgram() : Unit =
	{
		if (this.enumerator == null) return

		while (this.nextProg.isEmpty) {
			val next = this.enumerator.next()
			if (next.height > this.childHeight) {
				// We are out of map functions to synthesize for this list.
				if (this.listIter.hasNext) this.nextList()
				else return
			} else if (next.nodeType.eq(this.outputListType) && next.includes(this.varName)) {
				// next is a valid program
				val node = new ListCompNode(this.currList, next, this.varName)
				this.nextProg = Some(node)
			}
		}
	}

	private def nextList() : Unit =
	{
		this.currList = listIter.next()
		val newContexts = this.contexts.flatMap(m => this.currList.values.map(value => m + (this.varName -> value)));
		val oeValuesManager = new InputsValuesManager();
		this.enumerator = new Enumerator(this.mapVocab, oeValuesManager, newContexts)
	}
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