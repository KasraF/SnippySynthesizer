package ast

import execution.Types.Types

trait ASTNode {
  val name: String
  val arity: Int
  val height: Int
  val nodeType: Types
  def code = {
    val sb = new StringBuilder()
    toCodeInner(sb)
    sb.mkString
  }

  def toCodeInner(stringBuilder: StringBuilder)

  def wrapIfNeeded(stringBuilder: StringBuilder) = this match {
    case _ : Literal | _ : Variable | _ : FunctionCall | _ : MethodCall => {
      this.toCodeInner(stringBuilder)
    }
    case _ => {
      stringBuilder.append("(")
      this.toCodeInner(stringBuilder)
      stringBuilder.append(")")
    }
  }
}
