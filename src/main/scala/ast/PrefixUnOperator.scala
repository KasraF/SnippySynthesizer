package ast
import execution.Types.{Types, Any}

class PrefixUnOperator(val name: String, val rhs: ASTNode, val nodeType: Types = Any) extends ASTNode{
  override val arity: Int = 1
  override val height: Int = 1 + rhs.height

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    stringBuilder ++= name ++= " "
    rhs.wrapIfNeeded(stringBuilder)
  }
}
