package edu.ucsd.snippy.vocab

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast.{ASTNode, BoolVariable, IntVariable, StringVariable, Types, _}

class VocabFactory(
	val leavesMakers: List[VocabMaker],
	val nodeMakers: List[VocabMaker])
{
	def leaves(): Iterator[VocabMaker] = leavesMakers.iterator
	def nonLeaves(): Iterator[VocabMaker] = nodeMakers.iterator
	def all(): Iterator[VocabMaker] = leaves() ++ nonLeaves()
}

object VocabFactory
{
	def apply(vocab: List[VocabMaker]): VocabFactory =
	{
		val (leavesMakers, nodeMakers) = vocab.partition(m => m.arity == 0)
		new VocabFactory(leavesMakers, nodeMakers)
	}

	def apply(variables: List[(String, Types.Value)],
		additionalLiterals: Iterable[String],
		size: Boolean): VocabFactory =
	{
		val defaultStringLiterals = List(" ")
		val stringLiterals = (defaultStringLiterals ++ additionalLiterals).distinct

		val vocab: List[VocabMaker] = {
			// First, add the variables
			variables.map {
				case (name, Types.String) => new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringVariable(name, contexts)
				}
				case (name, Types.Int) => new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntVariable]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntVariable(name, contexts)
				}
				case (name, Types.Bool) => new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[BoolVariable]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						BoolVariable(name, contexts)
				}
				case (name, Types.List(childType)) => new BasicVocabMaker {
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.listOf(childType)
					override val nodeType: Class[_ <: ASTNode] = classOf[ListVariable[Any]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListVariable(name, contexts, childType)
				}
				case (name, Types.Map(keyType, valType)) => new BasicVocabMaker {
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.mapOf(keyType, valType)
					override val nodeType: Class[_ <: ASTNode] = classOf[MapVariable[Any,Any]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						MapVariable(name, contexts, keyType, valType)
				}
				case (name, typ) =>
					assert(assertion = false, s"Input type $typ not supported for input $name")
					null
			} ++
			// Then literals
			stringLiterals.map{str =>
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringLiteral]
					override val head: String = ""

					override def apply(children : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringLiteral(str, contexts.length)

				}
			} ++ List(
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(0, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(2, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(1, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(-1, contexts.length)
				}, // Binary Ops
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[GreaterThan]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						GreaterThan(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[Equals]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Equals(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[LessThanEq]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						LessThanEq(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringConcat]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringConcat(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[BinarySubstring]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						BinarySubstring(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStep]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringStep(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Find]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Find(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[Contains]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Contains(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.IntList)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[IntContains]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntContains(children.head.asInstanceOf[IntNode], children.tail.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Count]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Count(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[StartsWith]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StartsWith(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[EndsWith]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						EndsWith(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.Iterable(Types.Any))
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Length]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Length(children.head.asInstanceOf[IterableNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.IntList)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Min]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Min(children.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.IntList)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Max]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Max(children.head.asInstanceOf[ListNode[Int]])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[IsAlpha]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IsAlpha(children.head.asInstanceOf[StringNode])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[Capitalize]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Capitalize(children.head.asInstanceOf[StringNode])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[IsNumeric]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IsNumeric(children.head.asInstanceOf[StringNode])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringLower]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringLower(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringUpper]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringUpper(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToInt]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringToInt(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToString]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntToString(children.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.String, Types.Int, Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[TernarySubstring]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						TernarySubstring(
							children.head.asInstanceOf[StringNode],
							children(1).asInstanceOf[IntNode],
							children(2).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.IntList, Types.Int, Types.Int)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[IntTernarySubList]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntTernarySubList(
							children.head.asInstanceOf[ListNode[Int]],
							children(1).asInstanceOf[IntNode],
							children(2).asInstanceOf[IntNode])
				},


				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[StringSplit]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringSplit(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.StringList)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[SortedStringList]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						SortedStringList(children.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.IntList)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[SortedIntList]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						SortedIntList(children.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.IntList, Types.Int)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[IntListAppend]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntListAppend(children.head.asInstanceOf[ListNode[Int]], children.tail.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringList, Types.String)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[StringListAppend]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringListAppend(children.head.asInstanceOf[ListNode[String]], children.tail.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringList, Types.Int)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[StringListStep]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringListStep(children.head.asInstanceOf[ListNode[String]], children.tail.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.IntList, Types.Int)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[IntListStep]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntListStep(children.head.asInstanceOf[ListNode[Int]], children.tail.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.StringList)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringJoin]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringJoin(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.IntList, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntListLookup]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntListLookup(children.head.asInstanceOf[ListNode[Int]], children.tail.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringList, Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringListLookup]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringListLookup(children.head.asInstanceOf[ListNode[String]], children.tail.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.StringList)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[StringListContains]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringListContains(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.IntList)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[IntListContains]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntListContains(children.head.asInstanceOf[IntNode], children.tail.head.asInstanceOf[ListNode[Int]])
				},
				new ListCompVocabMaker(Types.String, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToStringListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new StringToStringListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[StringNode],
							this.varName)

					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.String)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.String, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToIntListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new StringToIntListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[IntNode],
							this.varName)

					override val returnType: Types = Types.IntList
					override val childTypes: List[Types] = List(Types.String)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.Int, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToStringListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new IntToStringListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[StringNode],
							this.varName)

					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.Int)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.Int, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToIntListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new IntToIntListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[IntNode],
							this.varName)

					override val returnType: Types = Types.IntList
					override val childTypes: List[Types] = List(Types.Int)
					override val head: String = ""
				},
				new MapCompVocabMaker(Types.String, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStringMapCompNode]
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						new StringStringMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[StringNode], this.varName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new MapCompVocabMaker(Types.String, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringIntMapCompNode]
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						new StringIntMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[IntNode], this.varName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new FilteredMapVocabMaker(Types.String, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStringFilteredMapNode]
					override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
						new StringStringFilteredMapNode(map.asInstanceOf[StringStringMapNode], filter, this.keyName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new FilteredMapVocabMaker(Types.String, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringIntFilteredMapNode]
					override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
						new StringIntFilteredMapNode(map.asInstanceOf[MapNode[String,Int]], filter, this.keyName)
					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringIntMap, Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[MapGet]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						MapGet(children.head.asInstanceOf[MapNode[String,Int]], children(1).asInstanceOf[StringNode])

				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntAddition]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntAddition(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntMultiply]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntMultiply(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntSubtraction]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntSubtraction(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntDivision]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntDivision(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Modulo]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Modulo(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				}
			)
		}

		VocabFactory(vocab)
	}
}
