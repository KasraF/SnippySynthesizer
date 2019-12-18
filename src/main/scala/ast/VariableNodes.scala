package ast

abstract class VariableNode[T](contexts: List[Map[String,Any]]) extends ASTNode {
  override val height: Int = 0
  val terms = 1
  val name: String
  val values: List[T] = contexts.map{ context =>
    context(name).asInstanceOf[T]
  }
  override lazy val code: String = name
  def includes(varName: String): Boolean = name == varName
}

class StringVariable(val name: String, contexts: List[Map[String,Any]]) extends VariableNode[String](contexts) with StringNode

class IntVariable(val name: String, contexts: List[Map[String,Any]]) extends VariableNode[Int](contexts) with IntNode

class BoolVariable(val name: String, contexts: List[Map[String,Any]]) extends VariableNode[Boolean](contexts) with BoolNode
