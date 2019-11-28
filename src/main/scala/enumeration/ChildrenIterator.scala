package enumeration

import ast.ASTNode

class ChildrenIterator(val childrenCandidates: List[ASTNode], val arity: Int, val currHeight: Int) extends Iterator[List[ASTNode]]{
  val candidates = ChildrenIterator.cross(
    (0 until arity).map(x => childrenCandidates).toList)
    .filter(children => children.exists(child => child.height == currHeight - 1))
  val candidatesIter = candidates.iterator
  override def hasNext: Boolean = candidatesIter.hasNext
  override def next(): List[ASTNode] = candidatesIter.next
}

object ChildrenIterator {
  def cross[T](inputVals: List[List[T]]) = inputVals.length match {
    case 1 => inputVals.head.map(x => List(x))
    case _ => cartesianProduct(inputVals:_*)
  }

  def cartesianProduct[T](lst: List[T]*): List[List[T]] = {

    /**
      * Prepend single element to all lists of list
      * @param e single elemetn
      * @param ll list of list
      * @param a accumulator for tail recursive implementation
      * @return list of lists with prepended element e
      */
    def pel(e: T,
            ll: List[List[T]],
            a: List[List[T]] = Nil): List[List[T]] =
      ll match {
        case Nil => a.reverse
        case x :: xs => pel(e, xs, (e :: x) :: a )
      }

    lst.toList match {
      case Nil => Nil
      case x :: Nil => List(x)
      case x :: _ =>
        x match {
          case Nil => Nil
          case _ =>
            lst.par.foldRight(List(x))( (l, a) =>
              l.flatMap(pel(_, a))
            ).map(_.dropRight(x.size))
        }
    }
  }
}