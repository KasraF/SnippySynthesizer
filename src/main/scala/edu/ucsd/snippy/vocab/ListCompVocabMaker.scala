package edu.ucsd.snippy.vocab

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration._
import edu.ucsd.snippy.utils.Utils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class ListCompVocabMaker(inputListType: Types, outputListType: Types) extends VocabMaker with Iterator[ASTNode]
{
	// Causes "Too many open files" error :/
	// var size_log = new FileOutputStream("output.txt", true)

	override val arity: Int = 2
	def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode = null

	var listIter: Iterator[ASTNode] = _
	var mapVocab: VocabFactory = _
	var contexts: List[Map[String, Any]] = _

	var costLevel: Int = _
	var enumerator: Enumerator = _
	var currList: ASTNode = _
	var childHeight: Int = _
	var varName: String = _
	var nextProg: Option[ASTNode] = None
	var miniBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]] = _
	var mainBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]] = _

	assert(inputListType.equals(Types.Int) || inputListType.equals(Types.String),
	       s"List comprehension input type not supported: $inputListType")

	assert(outputListType.equals(Types.Int) || outputListType.equals(Types.String),
	       s"List comprehension output type not supported: $inputListType")

	def makeNode(lst: ASTNode, map: ASTNode) : ASTNode

	override def init(programs: List[ASTNode], contexts : List[Map[String, Any]], vocabFactory: VocabFactory, height: Int) : Iterator[ASTNode] = {
		this.listIter = programs.filter(n => n.nodeType.equals(Types.listOf(this.inputListType))).iterator

		this.childHeight = height - 1
		this.varName = "var"
		this.contexts = contexts

		// Make sure the name is unique
		this.varName = Utils.createUniqueName(this.varName, contexts)

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.inputListType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(varName, contexts)
			}
			case Types.Int => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				override val nodeType: Class[_ <: ASTNode] = classOf[IntVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					IntVariable(varName, contexts)
			}
		}

		// We don't support nested list comprehensions
		val vocabs = newVarVocab ::
			vocabFactory.leavesMakers :::
			vocabFactory.nodeMakers.filter(c => c.isInstanceOf[BasicVocabMaker])

		this.mapVocab = VocabFactory.apply(vocabs)
		this.nextList()
		this
	}

	override def probe_init(vocabFactory: VocabFactory,
	                        costLevel: Int, contexts: List[Map[String,Any]],
	                        bank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
	                        nested: Boolean,
	                        miniBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]],
	                        mini: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]) : Iterator[ASTNode] = {

		this.costLevel = costLevel - 1
		this.mainBank = bank.map(n => (n._1, n._2.filter(c => !c.includes(this.varName))))
		this.listIter = this.mainBank.dropRight(1).values.flatten.toList
			.filter(n => n.nodeType.equals(Types.listOf(this.inputListType))).iterator
		this.varName = "var"
		this.contexts = contexts
		this.miniBank = miniBank
		// Make sure the name is unique
		// TODO We need a nicer way to generate this
		while (contexts.head.contains(this.varName)) this.varName = "_" + this.varName

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.inputListType match {
			case Types.
				String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(varName, contexts)
			}
			case Types.Int => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				override val nodeType: Class[_ <: ASTNode] = classOf[IntVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					IntVariable(varName, contexts)
			}
		}

		val vocabs = newVarVocab ::
			vocabFactory.leavesMakers :::
			vocabFactory.nodeMakers.filter(c => c.isInstanceOf[BasicVocabMaker]
				&& c.returnType.equals(this.outputListType)) // We don't support nested list comprehensions

		this.mapVocab = VocabFactory.apply(vocabs)
		this.nextList()
		this
	}

	override def hasNext: Boolean = {
		if (this.nextProg.isEmpty) nextProgram()
		this.nextProg.isDefined
	}

	override def next: ASTNode =
	{
		if (this.nextProg.isEmpty) nextProgram()
		val rs = this.nextProg.get
		this.nextProg = None
		rs
	}

	private def nextProgram() : Unit =
	{
		if (this.enumerator == null) return

		while (this.nextProg.isEmpty) {

			while (!this.enumerator.hasNext) { if (!this.nextList()) return }

			val next = this.enumerator.next()

			if (next.cost <= this.costLevel - this.currList.cost) {
				updateMiniBank((this.nodeType, this.currList), next) // TODO: update miniBank with only variable program
			}

			if (next.cost > this.costLevel - this.currList.cost) {
				// We are out of map functions to synthesize for this list.
				if (!this.nextList()) {
					// We are also out of lists!
					return
				}
			} else if (next.nodeType.eq(this.outputListType) && next.includes(this.varName)) {
				// next is a valid program
				val node = this.makeNode(this.currList, next)
				this.nextProg = Some(node)
			}
		}
	}

	private def updateMiniBank(key: (Class[_], ASTNode), value: ASTNode): Unit = {
		if (!this.miniBank.contains(key))
			this.miniBank(key) = mutable.Map(value.cost -> ArrayBuffer(value))
		else if (!this.miniBank(key).contains(value.cost))
			this.miniBank(key)(value.cost) = ArrayBuffer(value)
		else
			this.miniBank(key)(value.cost) += value
	}

	private def nextList() : Boolean =
	{
		var done = false

		while (!done && listIter.hasNext) {
			val lst = listIter.next()

			if (lst.values.exists(_.isDefined) && lst.values.filter(_.isDefined).forall(_.get.asInstanceOf[List[Any]].nonEmpty)) {
				this.currList = lst
				val newContexts = new Contexts(this.contexts.zipWithIndex.flatMap(
					context =>
						this.currList.values(context._2) match {
							case Some(lst: List[Any]) => lst.map(value => context._1 + (this.varName -> value))
							case None => Nil // We skip these, and handle them when unrolling later
						}
				))
				val oeValuesManager = new InputsValuesManager
				this.enumerator = {
					val bankCost = this.costLevel - this.currList.cost
					val mainBank = this.mainBank.take(bankCost - 1)

					val miniBank = if (this.miniBank.contains((this.nodeType, this.currList))) {
						this.miniBank((this.nodeType, this.currList)).take(bankCost)
							.mapValuesInPlace((_, nodes) => nodes.map(_.updateValues(newContexts)))
					} else {
						null
					}

					val nestedCost = if (this.miniBank.contains((this.nodeType, this.currList))) {
						this.miniBank((this.nodeType, this.currList)).keys.last
					} else {
						0
					}

					val endCost = this.costLevel - this.currList.cost
					new ProbEnumerator(
						this.mapVocab,
						oeValuesManager,
						newContexts.contexts,
						true,
						nestedCost,
						mainBank,
						miniBank,
						endCost)
				}

				done = true
			}
		}

		done
	}
}