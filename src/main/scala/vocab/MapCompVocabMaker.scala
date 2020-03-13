package vocab

import ast.Types.Types
import ast._
import enumeration.{Enumerator, InputsValuesManager}

abstract class MapCompVocabMaker(iterableType: Types, valueType: Types) extends VocabMaker with Iterator[ASTNode]
{
	override val arity: Int = 3

	var listIter: Iterator[ASTNode] = _
	var mapVocab: VocabFactory = _
	var contexts: List[Map[String, Any]] = _

	var enumerator: Iterator[ASTNode] = _
	var currList: ASTNode = _
	var childHeight: Int = _
	var varName: String = _

	var nextProg: Option[ASTNode] = None

	assert(iterableType.equals(Types.String) ||
	         iterableType.equals(Types.List(Types.Int)) ||
	         iterableType.equals(Types.List(Types.String)),
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
		// TODO We need a nicer way to generate this
		while (contexts.head.contains(this.varName)) this.varName = "_" + this.varName

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.iterableType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringVariable(varName, contexts)
			}
			case Types.List(Types.String) => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringVariable(varName, contexts)
			}
			case Types.List(Types.Int) => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntVariable(varName, contexts)
			}
		}

		// We don't support nested list comprehensions
		val vocabs = newVarVocab ::
		  vocabFactory.leavesMakers :::
		  vocabFactory.nodeMakers.filter(_.isInstanceOf[BasicVocabMaker])

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

	private def nextProgram() : Unit =
	{
		if (this.enumerator == null) return

		while (this.nextProg.isEmpty) {
			if (!this.enumerator.hasNext) return

			val value = this.enumerator.next()
			if (value.height > this.childHeight) {
				// We are out of map functions to synthesize for this list.
				if (!this.nextList())
				// We are also out of lists!
				return
			} else if (value.nodeType.eq(this.valueType) && value.includes(this.varName)) {
				// next is a valid program
				val node = this.makeNode(
					this.currList,
					new StringVariable(varName, this.enumerator.asInstanceOf[Enumerator].contexts),
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

			if (lst.values.head.asInstanceOf[String].nonEmpty) {
				this.currList = lst
				val newContexts = this.contexts.zipWithIndex
				  .flatMap(context => this.currList.values(context._2).asInstanceOf[String]
				    .map(c => c.toString)
					.map(value => context._1 + (this.varName -> value)))
				val oeValuesManager = new InputsValuesManager()
				this.enumerator = new Enumerator(this.mapVocab, oeValuesManager, newContexts)
				done = true
			}
		}

		done
	}
}

abstract class FilteredMapVocabMaker(keyType: Types, valueType: Types) extends VocabMaker with Iterator[ASTNode]
{
	override val arity: Int = 2

	var mapIter: Iterator[ASTNode] = _
	var filterVocab: VocabFactory = _
	var contexts: List[Map[String, Any]] = _

	var enumerator: Iterator[ASTNode] = _
	var currMap: ASTNode = _
	var childHeight: Int = _
	var keyName: String = _

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
		this.mapIter = progs.filter(n => n.isInstanceOf[VariableNode[_]] && n.nodeType.equals(Types.Map(keyType, valueType))).iterator
		this.childHeight = height - 1
		this.keyName = "key"
		this.contexts = contexts

		// Make sure the name is unique
		// TODO We need a nicer way to generate this
		while (contexts.head.contains(this.keyName)) this.keyName = "_" + this.keyName

		// Filter the vocabs for the map function
		// TODO There has to be a more efficient way
		val newVarVocab = this.keyType match {
			case Types.String => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new StringVariable(keyName, contexts)
			}
			case Types.Int => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					new IntVariable(keyName, contexts)
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

	private def nextProgram() : Unit =
	{
		if (this.enumerator == null) return

		while (this.nextProg.isEmpty) {
			if (!this.enumerator.hasNext) return

			val filter = this.enumerator.next()

			if (filter.height > this.childHeight + 1) {
				// We are out of map functions to synthesize for this list.
				if (!this.nextMap())
				// We are also out of lists!
				return
			} else if (filter.isInstanceOf[BoolNode] && filter.includes(this.keyName)) {
				// next is a valid program
				val node = this.makeNode(this.currMap, filter.asInstanceOf[BoolNode])
				this.nextProg = Some(node)
			}
		}
	}

	private def nextMap() : Boolean =
	{
		var done = false

		while (!done && mapIter.hasNext) {
			val map = mapIter.next()

			if (map.values.head.asInstanceOf[List[_]].nonEmpty) {
				this.currMap = map
				val newContexts = this.contexts.zipWithIndex
				  .flatMap(context =>
					           this.currMap.values(context._2)
					             .asInstanceOf[List[(String, Int)]]
					             .map(_._1)
					             .map(value => context._1 + (this.keyName -> value)))
				val oeValuesManager = new InputsValuesManager()
				this.enumerator = new Enumerator(this.filterVocab, oeValuesManager, newContexts)
				done = true
			}
		}

		done
	}
}