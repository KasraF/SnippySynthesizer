package enumeration
import ast.ASTNode


import scala.collection.mutable.ListBuffer

object EnumerationHeuristics {
  def keep(program: ASTNode): Boolean = ??? /*program match {
    case binNode: BinOperator => if (binNode.name == "+" && binNode.nodeType == Types.String) {
      val literals = ListBuffer[Option[Literal]]()
      collectAddedLiterals(binNode,literals)
      literals.zip(literals.tail).forall(x => (for(lhs <- x._1; rhs <- x._2) yield lhs != rhs).getOrElse(true))
    }
    else true
    case _ => true
  }

  def collectAddedLiterals(operator: BinOperator, acc: ListBuffer[Option[Literal]]): Unit = {
    assert(operator.nodeType == Types.String)
    val left = operator.left
    if (left.isInstanceOf[Literal]) acc += Some(left.asInstanceOf[Literal])
    else if (left.isInstanceOf[BinOperator] && left.nodeType == Types.String && left.name == "+")
      collectAddedLiterals(left.asInstanceOf[BinOperator],acc)
    else acc += None

    val right = operator.right
    if (right.isInstanceOf[Literal]) acc += Some(right.asInstanceOf[Literal])
    else if (right.isInstanceOf[BinOperator] && right.nodeType == Types.String && right.name == "+")
      collectAddedLiterals(right.asInstanceOf[BinOperator],acc)
    else acc += None
  }*/

}
