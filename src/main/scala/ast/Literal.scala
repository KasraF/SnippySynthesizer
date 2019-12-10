package ast

import execution.Types.{Types,Any}

class Literal(val name: String, val nodeType: Types = Any) extends ASTNode{
  override val arity: Int = 0
  override val height: Int = 0

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    stringBuilder.append(name)
  }

  override def equals(obj: scala.Any): Boolean = if (obj.isInstanceOf[Literal])
    obj.asInstanceOf[Literal].name == this.name && obj.asInstanceOf[Literal].nodeType == this.nodeType
  else false
}