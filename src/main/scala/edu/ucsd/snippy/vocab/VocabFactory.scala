package edu.ucsd.snippy.vocab

import edu.ucsd.snippy.ast.Types.{IntList, IntSet, Types}
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
		additionalLiterals: Iterable[String]): VocabFactory =
	{
		val defaultStringLiterals = List(" ")
		val defaultIntLiterals = List(-1, 0, 1, 2)
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
				case (name, Types.Double) => new BasicVocabMaker {
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Double
					override val nodeType: Class[_ <: ASTNode] = classOf[DoubleVariable]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoubleVariable(name, contexts)
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
				case (name, Types.Set(childType)) => new BasicVocabMaker {
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.setOf(childType)
					override val nodeType: Class[_ <: ASTNode] = classOf[SetVariable[Any]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						SetVariable(name, contexts, childType)
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
			} ++
			defaultIntLiterals.map{num =>
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(num, contexts.length)
				}
			} ++ List(
				new BasicVocabMaker {
					override val returnType: Types = IntSet
					override val arity: Int = 1
					override val childTypes: List[Types] = List(IntList)
					override val nodeType: Class[_ <: ASTNode] = classOf[ToSet[Int]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ToSet(children.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker {
					override val arity: Int = 1
					override val childTypes: List[Types] = List(IntList)
					override val nodeType: Class[_ <: ASTNode] = classOf[Sum]
					override val returnType: Types = Types.Int
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Sum(children.head.asInstanceOf[ListNode[Int]])
				},
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
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[StringEquals]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringEquals(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
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
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[IsLower]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IsLower(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker {
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
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[UnarySplit]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						UnarySplit(children.head.asInstanceOf[StringNode])
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
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.StringStringMap)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[MapKeys]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						MapKeys(children.head.asInstanceOf[MapNode[String,String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.IntList, Types.Int)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListAppend[Int, IntNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListAppend[Int, IntNode](children.head.asInstanceOf[ListNode[Int]], children.tail.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringList, Types.String)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListAppend[String, StringNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListAppend[String, StringNode](children.head.asInstanceOf[ListNode[String]], children.tail.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.IntList)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListPrepend[Int, IntNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListPrepend[Int, IntNode](children.head.asInstanceOf[IntNode], children.tail.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.StringList)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListPrepend[String, StringNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListPrepend[String, StringNode](children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringList, Types.Int)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListStep[String]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListStep[String](children.head.asInstanceOf[ListNode[String]], children.tail.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.IntList, Types.Int)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListStep[Int]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListStep[Int](children.head.asInstanceOf[ListNode[Int]], children.tail.head.asInstanceOf[IntNode])
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
					override val nodeType: Class[_ <: ASTNode] = classOf[ListContains[String, StringNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListContains[String, StringNode](children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.IntList)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[ListContains[Int, IntNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListContains[Int, IntNode](children.head.asInstanceOf[IntNode], children.tail.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringList, Types.StringList)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListConcat[String]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListConcat[String](children.head.asInstanceOf[ListNode[String]], children.tail.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.IntList, Types.IntList)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListConcat[Int]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListConcat[Int](children.head.asInstanceOf[ListNode[Int]], children.tail.head.asInstanceOf[ListNode[Int]])
				},
				new ListCompVocabMaker(Types.String, Types.String) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToStringListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						StringToStringListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[StringNode],
							this.varName)

					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.String)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.String, Types.Int) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToIntListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						StringToIntListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[IntNode],
							this.varName)

					override val returnType: Types = Types.IntList
					override val childTypes: List[Types] = List(Types.String)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.Int, Types.String) {
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToStringListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						IntToStringListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[StringNode],
							this.varName)

					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.Int)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.Int, Types.Int) {
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToIntListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						IntToIntListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[IntNode],
							this.varName)

					override val returnType: Types = Types.IntList
					override val childTypes: List[Types] = List(Types.Int)
					override val head: String = ""
				},
				new MapCompVocabMaker(Types.String, Types.String) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStringMapCompNode]
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						StringStringMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[StringNode], this.varName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new MapCompVocabMaker(Types.String, Types.Int) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringIntMapCompNode]
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						StringIntMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[IntNode], this.varName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new FilteredMapVocabMaker(Types.String, Types.String) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStringFilteredMapNode]
					override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
						StringStringFilteredMapNode(map.asInstanceOf[MapNode[String, String]], filter, this.keyName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new FilteredMapVocabMaker(Types.String, Types.Int) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringIntFilteredMapNode]
					override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
						StringIntFilteredMapNode(map.asInstanceOf[MapNode[String, Int]], filter, this.keyName)
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
					override val childTypes: List[Types] = List(Types.StringStringMap, Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringMapGet]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringMapGet(children.head.asInstanceOf[MapNode[String,String]], children(1).asInstanceOf[StringNode])
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
				},
				new BasicVocabMaker {
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.IntList, Types.Int, Types.Int)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[TernarySubList[Int]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						TernarySubList[Int](children.head.asInstanceOf[ListNode[Int]], children(1).asInstanceOf[IntNode], children(2).asInstanceOf[IntNode])
				},
				new BasicVocabMaker {
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.IntList, Types.Int, Types.Int)
					override val returnType: Types = Types.IntList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListInsert[Int, IntNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListInsert[Int, IntNode](children.head.asInstanceOf[ListNode[Int]], children(1).asInstanceOf[IntNode], children(2).asInstanceOf[IntNode])
				},
				new BasicVocabMaker {
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.StringList, Types.Int, Types.Int)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[TernarySubList[String]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						TernarySubList[String](children.head.asInstanceOf[ListNode[String]], children(1).asInstanceOf[IntNode], children(2).asInstanceOf[IntNode])
				},
				new BasicVocabMaker {
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[ToList]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ToList(children.head.asInstanceOf[StringNode])
				}
				//Doubles
				,new BasicVocabMaker {
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.DoubleList)
					override val returnType: Types = Types.Double
					override val nodeType: Class[_ <: ASTNode] = classOf[DoublesMax]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoublesMax(children.head.asInstanceOf[ListNode[Double]])
				}
				,new BasicVocabMaker {
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Double)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[LessThanEqDoubles]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						LessThanEqDoubles(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[DoubleNode])
				}
				,new BasicVocabMaker {
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Double)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[GreaterThanDoubles]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						GreaterThanDoubles(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[DoubleNode])
				}
				,new BasicVocabMaker {
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Int)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[LessThanEqDoubleInt]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						LessThanEqDoubleInt(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[IntNode])
				}
				,new BasicVocabMaker {
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Int)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[GreaterThanDoubleInt]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						GreaterThanDoubleInt(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[IntNode])
				}
				,new BasicVocabMaker {
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Double)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[LessThanEqIntDouble]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						LessThanEqIntDouble(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[DoubleNode])
				}
				,new BasicVocabMaker {
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Double)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[GreaterThanIntDouble]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						GreaterThanIntDouble(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[DoubleNode])
				},
				new BasicVocabMaker {
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Bool, Types.Bool)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[LAnd]
					override val head: String = ""
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						LAnd(children.head.asInstanceOf[BoolNode], children(1).asInstanceOf[BoolNode])
				}
				,new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.DoubleList)
					override val returnType: Types = Types.DoubleList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListPrepend[Double, DoubleNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListPrepend[Double, DoubleNode](children.head.asInstanceOf[DoubleNode], children.tail.head.asInstanceOf[ListNode[Double]])
				}
				,new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.DoubleList, Types.Double)
					override val returnType: Types = Types.DoubleList
					override val nodeType: Class[_ <: ASTNode] = classOf[ListAppend[Double, DoubleNode]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						ListAppend[Double, DoubleNode](children.head.asInstanceOf[ListNode[Double]], children.tail.head.asInstanceOf[DoubleNode])
				}
				,new BasicVocabMaker {
					override val returnType: Types = Types.Double
					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoubleListLookup(children.head.asInstanceOf[ListNode[Double]], children.last.asInstanceOf[IntNode])

					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.DoubleList,Types.Int)
					override val nodeType: Class[_ <: ASTNode] = classOf[DoubleListLookup]
					override val head: String = ""
				}
				,new BasicVocabMaker {
					override val returnType: Types = Types.DoubleSet

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						SetAppend[Double, DoubleNode](children.head.asInstanceOf[SetNode[Double]],children.last.asInstanceOf[DoubleNode])

					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.DoubleSet,Types.Double)
					override val nodeType: Class[_ <: ASTNode] = classOf[SetAppend[Double, DoubleNode]]
					override val head: String = ""
				}
				, new BasicVocabMaker {
					override val returnType: Types = Types.Double

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoublesSum(children.head.asInstanceOf[ListNode[Double]])

					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.DoubleList)
					override val nodeType: Class[_ <: ASTNode] = classOf[DoublesSum]
					override val head: String = ""
				}
				, new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Double)
					override val returnType: Types = Types.Double
					override val nodeType: Class[_ <: ASTNode] = classOf[DoublesAddition]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoublesAddition(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[DoubleNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Double)
					override val returnType: Types = Types.Double
					override val nodeType: Class[_ <: ASTNode] = classOf[DoublesMultiply]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoublesMultiply(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[DoubleNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Double)
					override val returnType: Types = Types.Double
					override val nodeType: Class[_ <: ASTNode] = classOf[DoublesSubtraction]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoublesSubtraction(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[DoubleNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Double, Types.Double)
					override val returnType: Types = Types.Double
					override val nodeType: Class[_ <: ASTNode] = classOf[DoublesDivision]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						DoublesDivision(children.head.asInstanceOf[DoubleNode], children(1).asInstanceOf[DoubleNode])
				}
				,new BasicVocabMaker {
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.DoubleList, Types.Int, Types.Int)
					override val returnType: Types = Types.DoubleList
					override val nodeType: Class[_ <: ASTNode] = classOf[TernarySubList[Double]]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						TernarySubList[Double](children.head.asInstanceOf[ListNode[Double]], children(1).asInstanceOf[IntNode], children(2).asInstanceOf[IntNode])
				}
			)
		}

		VocabFactory(vocab)
	}
}
