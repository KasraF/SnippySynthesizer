package edu.ucsd.snippy.ast

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.Contexts

abstract class VariableNode[T](contexts: List[Map[String, Any]]) extends ASTNode
{
	override lazy val code: String = name
	override val height: Int = 0
	override val children: Iterable[ASTNode] = Iterable.empty
	override protected val parenless: Boolean = true

	val terms = 1
	val name: String
	val values: List[Option[T]] = contexts.map(context => context.get(name).asInstanceOf[Option[T]])

	def includes(varName: String): Boolean = name == varName

	override lazy val usesVariables: Boolean = true
}

object VariableNode {
	def nodeFromType(name: String, retType: Types.Types, contexts: List[Map[String, Any]]): ASTNode = retType match {
		case Types.Bool => (BoolVariable(name, contexts))
		case Types.String => (StringVariable(name, contexts))
		case Types.Int => (IntVariable(name, contexts))
		case Types.IntList => (ListVariable[Int](name, contexts, Types.Int))
		case Types.StringList => (ListVariable[String](name, contexts, Types.String))
		case Types.IntIntMap => (MapVariable[Int,Int](name, contexts, Types.Int, Types.Int))
		case Types.IntStringMap => (MapVariable[Int,String](name, contexts, Types.Int, Types.String))
		case Types.StringIntMap => (MapVariable[String,Int](name, contexts, Types.String, Types.Int))
		case Types.StringStringMap => (MapVariable[String,String](name, contexts, Types.String, Types.String))
		case Types.IntSet => SetVariable[Int](name, contexts, Types.Int)
		case Types.StringSet => SetVariable[String](name,contexts,Types.String)
	}
}

case class StringVariable(name: String, contexts: List[Map[String, Any]]) extends VariableNode[String](contexts) with StringNode
{
	override def updateValues(contexts: Contexts): StringVariable = copy(name, contexts = contexts.contexts)
}

case class IntVariable(name: String, contexts: List[Map[String, Any]]) extends VariableNode[Int](contexts) with IntNode
{
	override def updateValues(contexts: Contexts): IntVariable = copy(name, contexts = contexts.contexts)
}

case class BoolVariable(name: String, contexts: List[Map[String, Any]]) extends VariableNode[Boolean](contexts) with BoolNode
{
	override def updateValues(contexts: Contexts): BoolVariable = copy(name, contexts = contexts.contexts)
}

case class ListVariable[T](name: String, contexts: List[Map[String, Any]], childType: Types) extends VariableNode[List[T]](contexts) with ListNode[T]
{
	override def updateValues(contexts: Contexts): ListVariable[T] = copy(name, contexts = contexts.contexts)
}

case class MapVariable[K, V](name: String, contexts: List[Map[String, Any]], keyType: Types, valType: Types) extends VariableNode[Map[K, V]](contexts) with MapNode[K, V]
{
	override def updateValues(contexts: Contexts): MapVariable[K, V] = copy(name, contexts = contexts.contexts)
}

case class SetVariable[T](name: String, contexts: List[Map[String, Any]], childType: Types) extends VariableNode[Set[T]](contexts) with SetNode[T]
{
	override def updateValues(contexts: Contexts): SetNode[T] = copy(name, contexts = contexts.contexts)
}