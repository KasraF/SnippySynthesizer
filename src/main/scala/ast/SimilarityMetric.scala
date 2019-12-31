package ast

import scala.collection.mutable

object SimilarityMetric {
  def compute(lhs: ASTNode, rhs: String): Int = {
    if (lhs.height > 0 && lhs.code == rhs) lhs.terms
    else {
      val lhsSubTrees = mutable.ListBuffer[ASTNode]()

      findEmbededSubtrees(lhs,rhs,lhsSubTrees)

//      val rhsSubTrees = mutable.ListBuffer[ASTNode]()
//      findEmbededSubtrees(rhs,lhs.code,rhsSubTrees)

      /*Math.max(*/lhsSubTrees.filter(t => t.height > 0).map(_.terms).sum//,rhsSubTrees.filter(t => t.height > 0).map(_.terms).sum)
    }
    //if subtree(lhs) in full(rhs) (or vice versa)
    //try larger tree
    //if nontrivial , keep
    //sum all kept
  }

  def findEmbededSubtrees(tree: ASTNode, otherCode: String, collection: mutable.ListBuffer[ASTNode]): Boolean = {
    if (otherCode.contains(tree.code)) true
    else {
      for (child <- tree.children) {
        if (findEmbededSubtrees(child,otherCode,collection))
          collection += child
      }
      false
    }
  }
}
