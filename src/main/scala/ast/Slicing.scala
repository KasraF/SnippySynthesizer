package ast
import execution.Types.{Types,Any}

class Slicing(val toSlice: ASTNode, val from: ASTNode, val to: ASTNode, val nodeType: Types = Any) extends ASTNode {
  override val name: String = "[:]"
  override val arity: Int = 3
  override val height: Int = 1 + List(toSlice.height,from.height,to.height).max

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    toSlice.wrapIfNeeded(stringBuilder)
    stringBuilder ++= "["
    from.toCodeInner(stringBuilder)
    stringBuilder ++= ":"
    to.toCodeInner(stringBuilder)
    stringBuilder ++= "]"
  }
}
