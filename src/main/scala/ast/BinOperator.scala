package ast

class BinOperator(val name: String, val left: ASTNode, val right: ASTNode) extends ASTNode {
  override val arity: Int = 2
  override val height: Int = Math.max(left.height, right.height) + 1

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    left.wrapIfNeeded(stringBuilder)
    stringBuilder += (' ') ++= (name) += (' ')
    right.wrapIfNeeded(stringBuilder)
  }
}
