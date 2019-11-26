package ast

class Variable(val name: String) extends ASTNode {
  override val arity: Int = 0

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    stringBuilder.append(name)
  }
}
