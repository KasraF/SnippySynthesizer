package ast
import ast.Types.Types

abstract class VariableNode[T](contexts: List[Map[String, Any]]) extends ASTNode
{
	override lazy val code: String = name
	override val height: Int = 0
	override val children: Iterable[ASTNode] = Iterable.empty
	val terms = 1
	val name: String
	val values: List[T] = contexts.map { context => context(name).asInstanceOf[T]}

	def includes(varName: String): Boolean = name == varName
}

class StringVariable(val name: String, contexts: List[Map[String, Any]]) extends VariableNode[String](contexts) with StringNode
class IntVariable(val name: String, contexts: List[Map[String, Any]]) extends VariableNode[Int](contexts) with IntNode
class BoolVariable(val name: String, contexts: List[Map[String, Any]]) extends VariableNode[Boolean](contexts) with BoolNode
class ListVariable[T](val name: String, val contexts: List[Map[String,Any]], val childType: Types) extends VariableNode[List[T]](contexts) with ListNode[T]
class MapVariable[K,V](val name: String, val contexts: List[Map[String, Any]], val keyType: Types, val valType: Types) extends VariableNode[List[(K,V)]](contexts) with MapNode[K,V]