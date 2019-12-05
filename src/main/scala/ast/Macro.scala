package ast
import execution.Types.{Types,Any}

class Macro(val macroPieces: Seq[String], val children: List[ASTNode], val nodeType: Types = Any) extends ASTNode {
  override val name: String = "macro"
  override val arity: Int = macroPieces.length - 1
  override val height: Int = if (children.isEmpty) 0 else 1 + children.map(_.height).max

  override def toCodeInner(stringBuilder: StringBuilder): Unit = {
    for((piece,child) <- macroPieces.zip(children)) {
      stringBuilder ++= piece
      child.wrapIfNeeded(stringBuilder)
    }
    stringBuilder ++= macroPieces.last
  }
}
