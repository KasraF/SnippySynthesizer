package ast

import execution.Types.{Any, Types}

class Variable(val name: String, val nodeType: Types = Any) extends ASTNode {
  override val arity: Int = 0
  override val height: Int = 0

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    stringBuilder.append(name)
  }
}
