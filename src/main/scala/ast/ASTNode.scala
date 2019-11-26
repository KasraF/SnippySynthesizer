package ast

trait ASTNode {
  val name: String
  val arity: Int
  def code = {
    val sb = new StringBuilder()
    toCodeInner(sb)
    sb.mkString
  }

  def toCodeInner(stringBuilder: StringBuilder)

  def wrapIfNeeded(stringBuilder: StringBuilder) = this match {
    case _ : Literal | _ : Variable => {
      this.toCodeInner(stringBuilder)
    }
    case _ => {
      stringBuilder.append("(")
      this.toCodeInner(stringBuilder)
      stringBuilder.append(")")
    }
  }
}
