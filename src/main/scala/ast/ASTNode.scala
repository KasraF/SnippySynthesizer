package ast

trait ASTNode {
  val nodeType: Types.Types
  val values: List[Any]
  val code: String
  val height: Int
  val terms: Int
  def includes(varName: String): Boolean
}

trait StringNode extends ASTNode {
  override val values: List[String]
  override val nodeType = Types.String
}

trait IntNode extends ASTNode {
  override val values: List[Int]
  override val nodeType = Types.Int
}

trait BoolNode extends ASTNode {
  override val values: List[Boolean]
  override val nodeType = Types.Bool
}