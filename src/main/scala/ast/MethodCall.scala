package ast
import execution.Types
import execution.Types.Types

class MethodCall(val name: String, val arity: Int, val lhs: ASTNode, val formals: List[ASTNode], val nodeType: Types = Types.Any) extends ASTNode {
  override val height: Int = 1 + (formals :+ lhs).map(_.height).max

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    lhs.wrapIfNeeded(stringBuilder)
    stringBuilder ++= "." ++= name ++= "("
    for(arg <- formals.take(arity - 2)) {
      arg.toCodeInner(stringBuilder)
      stringBuilder ++= ","
    }
    for (arg <- formals.lastOption) {
      arg.toCodeInner(stringBuilder)
    }
    stringBuilder ++= ")"
  }
}
