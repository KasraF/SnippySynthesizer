package ast

class BinOperator(val name: String, val left: ASTNode, val right: ASTNode) extends ASTNode {
  override val arity: Int = 2

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    left.wrapIfNeeded(stringBuilder)
    stringBuilder += (' ') ++= (name) += (' ')
    right.wrapIfNeeded(stringBuilder)
  }
}
