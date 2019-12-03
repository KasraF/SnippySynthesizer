package ast

import execution.Types.{Any, Types}

class FunctionCall(val name: String, val arity: Int, val args: List[ASTNode], val nodeType: Types = Any) extends ASTNode {
  assert(args.length == arity)
  override val height: Int = if (arity == 0) 0 else (args.map(x => x.height).max + 1)
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
