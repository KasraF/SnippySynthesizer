package edu.ucsd.snippy.vocab

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration._
import edu.ucsd.snippy.utils.Utils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class MapCompVocabMaker(iterableType: Types, valueType: Types) extends VocabMaker with Iterator[ASTNode]
{
	override val arity: Int = 3
	def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode = null

	var listIter: Iterator[ASTNode] = _
	var mapVocab: VocabFactory = _
	var contexts: List[Map[String, Any]] = _

	var enumerator: Enumerator = _
	var currList: ASTNode = _
	var costLevel: Int = _
	var childHeight: Int = _
	var varName: String = _
	var nestedCost: Int = _
	var varBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]] = _

	var mainBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]] = _
	var nextProg: Option[ASTNode] = None

	assert(iterableType.equals(Types.String) ||
		iterableType.equals(Types.IntList) ||
		iterableType.equals(Types.StringList),
		s"List comprehension iterable type not supported: $iterableType")

	assert(valueType.equals(Types.Int) || valueType.equals(Types.String),
		s"List comprehension output type not supported: $valueType")

	def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode) : ASTNode

	override def init(
		progs: List[ASTNode],
		contexts : List[Map[String, Any]],
		vocabFactory: VocabFactory,
		height: Int) : Iterator[ASTNode] =
	{
		this.listIter = progs.filter(n => n.nodeType.equals(this.iterableType)).iterator
		this.childHeight = height - 1
		this.varName = "var"
		this.contexts = contexts

		// Make sure the name is unique
		this.varName = Utils.createUniqueName(this.varName, contexts)

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.iterableType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(varName, contexts)
			}
			case Types.StringList => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""
				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(varName, contexts)
			}
			case Types.IntList => new BasicVocabMaker {
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

	override def probe_init(
		vocabFactory: VocabFactory,
		costLevel: Int,
		contexts: List[Map[String,Any]],
		bank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
		nested: Boolean,
		varBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]],
		mini: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]) : Iterator[ASTNode] = {

		this.mainBank = bank.map(n => (n._1, n._2.filter(c => !c.includes(this.varName))))
		this.listIter = this.mainBank.dropRight(1).values.flatten.toList.filter(n => n.nodeType.equals(this.iterableType)).iterator

		/**
		 * The outer enumerator bank contains list and dictionary comprehension programs
		 * which are not needed here since there is no nested enumeration.
		 **/
		this.costLevel = costLevel - 1
		this.varName = "var"
		this.contexts = contexts
		this.varBank = varBank
		// Make sure the name is unique
		// TODO We need a nicer way to generate this
		while (contexts.head.contains(this.varName)) this.varName = "_" + this.varName

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.iterableType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(varName, contexts)
			}
			case Types.StringList => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(varName, contexts)
			}
			case Types.IntList => new BasicVocabMaker {
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

	override def hasNext: Boolean =
	{
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

	private def updateVarBank(key: (Class[_], ASTNode), value: ASTNode): Unit = {
		if (!this.varBank.contains(key))
			this.varBank(key) = mutable.Map(value.cost -> ArrayBuffer(value))
		else if (!this.varBank(key).contains(value.cost))
			this.varBank(key)(value.cost) = ArrayBuffer(value)
		else
			this.varBank(key)(value.cost) += value
	}

	private def nextProgram() : Unit = {
		if (this.enumerator == null) return

		while (this.nextProg.isEmpty) {
			while (!this.enumerator.hasNext) { if (!this.nextList()) return }

			val value = this.enumerator.next()

			if (value.cost <= this.costLevel - this.currList.cost)
				updateVarBank((this.nodeType, this.currList), value) // TODO: update varBank with only variable programs
			//TODO: optimize - right now you need to keep enumerating programs to check whether it's above the required level.
			// What if there are many empty levels?

			if (value.cost > this.costLevel - this.currList.cost) {
				// We are out of map functions to synthesize for this list.
				if (!this.nextList()) {
					// We are also out of lists!
					return
				}
			} else if (value.nodeType.eq(this.valueType) && value.includes(this.varName)) {
				// next is a valid program
				val node = this.makeNode(
					this.currList,
					StringVariable(varName, this.enumerator.contexts),
					value)
				this.nextProg = Some(node)
			}
		}
	}

	private def nextList() : Boolean =
	{
		var done = false

		while (!done && listIter.hasNext) {
			val lst = listIter.next()

			if (lst.values.exists(_.isDefined) && lst.values.filter(_.isDefined).forall(_.get.asInstanceOf[String].nonEmpty)) {
				this.currList = lst
				val newContexts = this.contexts.zipWithIndex
					.flatMap(context =>
					this.currList.values(context._2) match {
						case Some(s: String) => s.map(c => c.toString).map(value => context._1 + (this.varName -> value))
						case None => Nil
					})
				val oeValuesManager = new InputsValuesManager()
				this.enumerator = {
					val contexts = new Contexts(newContexts)
					val bankCost = this.costLevel - this.currList.cost
					val mainBank = this.mainBank
						.take(bankCost - 1)

					val varBank = if (this.varBank.contains((this.nodeType, this.currList))) {
						this.varBank((this.nodeType, this.currList))
							.take(bankCost)
							.mapValuesInPlace((_, nodes) => nodes.map(_.updateValues(contexts)))
					} else {
						null
					}

					val nestedCost = if (this.varBank.contains((this.nodeType, this.currList)))
						this.varBank((this.nodeType, this.currList)).keys.last else 0
					// TODO: add the programs from the varBank to the main bank;
					//  pass the updated bank as parameter to the new enumerator object
					val endCost = this.costLevel - this.currList.cost

					new ProbEnumerator(
						this.mapVocab,
						oeValuesManager,
						newContexts,
						true,
						nestedCost,
						mainBank,
						varBank,
						endCost)
				}
				done = true
			}
		}
		done
	}
}

abstract class FilteredMapVocabMaker(keyType: Types, valueType: Types) extends VocabMaker with Iterator[ASTNode]
{
	override val arity: Int = 2
	def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode = null

	var mapIter: Iterator[ASTNode] = _
	var filterVocab: VocabFactory = _
	var contexts: List[Map[String, Any]] = _

	var enumerator: Enumerator = _
	var currMap: ASTNode = _
	var childHeight: Int = _
	var keyName: String = _
	var costLevel: Int = _
	var varBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]] = _
	var mainBank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]] = _
	var nextProg: Option[ASTNode] = None

	assert(keyType.equals(Types.Int) || keyType.equals(Types.String),
		s"List comprehension input type not supported: $keyType")

	assert(valueType.equals(Types.Int) || valueType.equals(Types.String),
		s"List comprehension output type not supported: $valueType")

	def makeNode(map: ASTNode, filter: BoolNode) : ASTNode

	override def init(
		progs: List[ASTNode],
		contexts : List[Map[String, Any]],
		vocabFactory: VocabFactory,
		height: Int) : Iterator[ASTNode] =
	{
		this.mapIter = progs.filter(n => n.isInstanceOf[VariableNode[_]] && n.nodeType.equals(Types.mapOf(keyType, valueType))).iterator
		this.childHeight = height - 1
		this.keyName = "key"
		this.contexts = contexts

		this.keyName = Utils.createUniqueName(this.keyName, contexts)

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.keyType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(keyName, contexts)
			}
			case Types.Int => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				override val nodeType: Class[_ <: ASTNode] = classOf[IntVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					IntVariable(keyName, contexts)
			}
		}

		// We don't support nested list comprehensions
		val vocabs = newVarVocab ::
			vocabFactory.leavesMakers :::
			vocabFactory.nodeMakers.filter(c => c.isInstanceOf[BasicVocabMaker])

		this.filterVocab = VocabFactory.apply(vocabs)
		this.nextMap()
		this
	}

	override def probe_init(
		vocabFactory: VocabFactory,
		costLevel: Int,
		contexts: List[Map[String,Any]],
		bank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
		nested: Boolean,
		varBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]],
		mini: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]) : Iterator[ASTNode] = {


		this.mainBank = bank.map(n => (n._1, n._2.filter(c => !c.includes(this.keyName))))
		this.mapIter = this.mainBank.dropRight(1).values.flatten.toList.
			filter(n => n.isInstanceOf[VariableNode[_]] && n.nodeType.equals(Types.mapOf(keyType, valueType))).iterator

		this.keyName = "key"
		this.contexts = contexts
		this.costLevel = costLevel - 1
		this.varBank = varBank

		// TODO We need a nicer way to generate this
		this.keyName = Utils.createUniqueName(this.keyName, contexts)

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.keyType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(keyName, contexts)
			}
			case Types.Int => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				override val nodeType: Class[_ <: ASTNode] = classOf[IntVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					IntVariable(keyName, contexts)
			}
		}

		// We don't support nested list comprehensions
		val vocabs = newVarVocab ::
			vocabFactory.leavesMakers :::
			vocabFactory.nodeMakers.filter(_.isInstanceOf[BasicVocabMaker])

		this.filterVocab = VocabFactory.apply(vocabs)
		this.nextMap()
		this
	}

	override def hasNext: Boolean =
	{
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

	private def updateVarBank(key: (Class[_], ASTNode), value: ASTNode): Unit = {
		if (!this.varBank.contains(key))
			this.varBank(key) = mutable.Map(value.cost -> ArrayBuffer(value))
		else if (!this.varBank(key).contains(value.cost))
			this.varBank(key)(value.cost) = ArrayBuffer(value)
		else
			this.varBank(key)(value.cost) += value
	}

	private def nextProgram() : Unit =
	{
		if (this.enumerator == null) return

		while (this.nextProg.isEmpty) {

			while (!this.enumerator.hasNext) { if (!this.nextMap()) return }

			val filter = this.enumerator.next()

			if (filter.cost <= this.costLevel - this.currMap.cost)
				updateVarBank((this.nodeType, this.currMap), filter) // TODO: update varBank with only variable programs

			if (filter.cost > this.costLevel - this.currMap.cost) {
				// We are out of map functions to synthesize for this list.
				if (!this.nextMap()) {
					// We are also out of lists!
					return
				}
			} else filter match {
				case node1: BoolNode if filter.includes(this.keyName) =>
					// next is a valid program
					val node = this.makeNode(this.currMap, node1)
					this.nextProg = Some(node)
				case _ => ()
			}
		}
	}

	private def nextMap() : Boolean =
	{
		var done = false

		while (!done && mapIter.hasNext) {
			val map = mapIter.next()

			if (map.values.exists(_.isDefined) && map.values.filter(_.isDefined).forall(_.get.asInstanceOf[Map[_,_]].nonEmpty)) {
				this.currMap = map
				val newContexts = this.contexts.zipWithIndex
					.flatMap(
						context =>
							this.currMap.values(context._2) match {
								case Some(map: Map[String, Int]) => map.keys.map(key => context._1 + (this.keyName -> key))
								case None => Nil
							})
				val oeValuesManager = new InputsValuesManager()
				this.enumerator = {
					val contexts = new Contexts(newContexts)
					val bankCost = this.costLevel - this.currMap.cost
					val mainBank = this.mainBank.take(bankCost - 1)

					val varBank = if (this.varBank.contains((this.nodeType, this.currMap))) {
						this.varBank((this.nodeType, this.currMap)).take(bankCost)
							.mapValuesInPlace((_, nodes) => nodes.map(_.updateValues(contexts)))

					} else {
						null
					}

					val nestedCost = if (this.varBank.contains((this.nodeType, this.currMap)))
						this.varBank((this.nodeType, this.currMap)).keys.last else 0

					val endCost = this.costLevel - this.currMap.cost
					new ProbEnumerator(
						this.filterVocab,
						oeValuesManager,
						newContexts,
						true,
						nestedCost,
						mainBank,
						varBank,
						endCost)
				}
				done = true
			}
		}

		done
	}
}