package ast

import execution.Types.{Any, Types}

class Variable(val name: String, val nodeType: Types = Any) extends ASTNode {
  override val arity: Int = 0
  override val height: Int = 0

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    stringBuilder.append(name)
  }

  override def equals(obj: scala.Any): Boolean = if (obj.isInstanceOf[Variable])
    obj.asInstanceOf[Variable].name == this.name && obj.asInstanceOf[Variable].nodeType == this.nodeType
  else false
}
