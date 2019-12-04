package ast
import execution.Types.{Types, Any}

class RandomAccess(val toAccess: ASTNode, val at: ASTNode, val nodeType: Types = Any) extends ASTNode {
  override val name: String = "[]"
  override val arity: Int = 2
  override val height: Int = 1 + Math.max(toAccess.height,at.height)

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    toAccess.wrapIfNeeded(stringBuilder)
    stringBuilder ++= "["
    at.toCodeInner(stringBuilder)
    stringBuilder ++= "]"
  }
}
