package edu.ucsd.snippy.ast

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.{Contexts, ProbUpdate}

trait ASTNode
{
	val nodeType: Types.Types
	val values: List[Option[Any]]
	val code: String
	val height: Int
	val terms: Int
	val children: Iterable[ASTNode]
	val usesVariables: Boolean
	protected val parenless: Boolean
	private var _cost: Option[Int] = None

	def includes(varName: String): Boolean

	def parensIfNeeded: String = if (height > 0 && !parenless) "(" + code + ")" else code

	def cost: Int =
	{
		if (_cost.isEmpty) renewCost()
		_cost.get
	}

	def renewCost(): Unit =
	{
		children.foreach(_.renewCost())
		_cost = Some(ProbUpdate.getRootPrior(this) + children.map(c => c.cost).sum)
	}

	def updateValues(contexts: Contexts): ASTNode
}

trait IterableNode extends ASTNode
{
	def splitByIterable[A](values: Iterable[Option[Iterable[_]]], listToSplit: Iterable[Option[A]]): List[Option[List[A]]] =
	{
		var rs: List[Option[List[A]]] = Nil
		var start = 0
		for (lst <- values) {
			lst match {
				case None => rs = rs :+ None
				case Some(lst) =>
					val delta = lst.size
					val splt: List[Option[A]] = listToSplit.slice(start, start + delta).toList
					if (splt.contains(None)) {
						rs = rs :+ None
					} else {
						rs = rs :+ Some(splt.map(_.get))
					}
					start += delta
			}
		}
		rs
	}
}

trait StringNode extends IterableNode
{
	override val values: List[Option[String]]
	override val nodeType: Types = Types.String
	override def updateValues(contexts: Contexts): StringNode
}

trait IntNode extends ASTNode
{
	override val values: List[Option[Int]]
	override val nodeType: Types = Types.Int
	override def updateValues(contexts: Contexts): IntNode
}

trait BoolNode extends ASTNode
{
	override val values: List[Option[Boolean]]
	override val nodeType: Types = Types.Bool
	override def updateValues(contexts: Contexts): BoolNode
}

trait DoubleNode extends ASTNode
{
	override val values: List[Option[Double]]
	override val nodeType: Types = Types.Double
	override def updateValues(contexts: Contexts): DoubleNode
}

trait ListNode[T] extends IterableNode
{
	val childType: Types
	override val values: List[Option[Iterable[T]]]
	override lazy val nodeType: Types = Types.listOf(childType)
	override def updateValues(contexts: Contexts): ListNode[T]
}

trait SetNode[T] extends IterableNode
{
	val childType: Types
	override val values: List[Option[Set[T]]]
	override lazy val nodeType: Types = Types.setOf(childType)

	override def updateValues(contexts: Contexts): SetNode[T]
}

trait StringListNode extends ListNode[String]
{
	override val childType: Types = Types.String
	override def updateValues(contexts: Contexts): StringListNode
}

trait IntListNode extends ListNode[Int]
{
	override val childType: Types = Types.Int
	override def updateValues(contexts: Contexts): IntListNode
}

trait DoubleListNode extends ListNode[Double]
{
	override val childType: Types = Types.Double
	override def updateValues(contexts: Contexts): DoubleListNode
}

trait BoolListNode extends ListNode[Boolean]
{
	override val childType: Types = Types.Bool
	override def updateValues(contexts: Contexts): BoolListNode
}

trait MapNode[K, V] extends IterableNode
{
	val keyType: Types
	val valType: Types

	override val values: List[Option[Map[K, V]]]
	override lazy val nodeType: Types = Types.mapOf(keyType, valType)
	override def updateValues(contexts: Contexts): MapNode[K, V]
}

trait StringStringMapNode extends MapNode[String, String]
{
	override val keyType: Types = Types.String
	override val valType: Types = Types.String
	override def updateValues(contexts: Contexts): StringStringMapNode
}

trait StringIntMapNode extends MapNode[String, Int]
{
	override val keyType: Types = Types.String
	override val valType: Types = Types.Int
	override def updateValues(contexts: Contexts): StringIntMapNode
}

trait IntStringMapNode extends MapNode[Int, String]
{
	override val keyType: Types = Types.Int
	override val valType: Types = Types.String
	override def updateValues(contexts: Contexts): IntStringMapNode
}

trait IntIntMapNode extends MapNode[Int, Int]
{
	override val keyType: Types = Types.Int
	override val valType: Types = Types.Int
	override def updateValues(contexts: Contexts): IntIntMapNode
}