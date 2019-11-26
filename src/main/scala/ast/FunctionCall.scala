package ast

class FunctionCall(val name: String, val arity: Int, val args: List[ASTNode]) extends ASTNode {
  assert(args.length == arity)
  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    stringBuilder ++= name ++= "("
    for(arg <- args.take(arity - 1)) {
        arg.toCodeInner(stringBuilder)
        stringBuilder += ','
    }
    for(arg <- args.lastOption) {
      arg.toCodeInner(stringBuilder)
    }
    stringBuilder ++= ")"
  }
}
