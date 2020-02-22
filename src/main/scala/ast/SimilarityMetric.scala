package ast

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object SimilarityMetric
{
	def compute(lhs: ASTNode, rhs: ASTNode): Int =
	{
		val accumulatedTerms = mutable.ListBuffer[Int]()
		val usedTerms        = mutable.Set[String]()
		doRecurse(lhs, rhs, accumulatedTerms, usedTerms)
		accumulatedTerms.sum
	}

	def doRecurse(lhs: ASTNode, rhs: ASTNode, accumulatedTerms: ListBuffer[Int], usedTerms: mutable.Set[String]): Unit =
	{
		if (lhs.height == 0 || rhs.height == 0) return

		if (lhs.code == rhs.code) {
			if (!usedTerms.contains(lhs.code)) accumulatedTerms += lhs.terms
			return
		}

		if (rhs.code.contains(lhs.code)) {
			for (child <- rhs.children) doRecurse(lhs, child, accumulatedTerms, usedTerms.clone)
			usedTerms += lhs.code
		} else {
			if (lhs.getClass == rhs.getClass) { //root node is the same
				//partition so only separately count children not in this score already
				val (sameChildren, diffChildren) = lhs.children.zip(rhs.children).partition { case (l, r) => l.height > 0 && l.code == r.code }
				if (sameChildren.nonEmpty) {
					accumulatedTerms += 1 + sameChildren.filter(child => !usedTerms.contains(child._1.code)).map(_._1.terms).sum
					usedTerms ++= sameChildren.map(_._1.code)
				}
				for (child <- diffChildren.map(_._1)) doRecurse(child, rhs, accumulatedTerms, usedTerms)
			}
			else {
				for (child <- lhs.children) doRecurse(child, rhs, accumulatedTerms, usedTerms)
			}
		}
	}

}
