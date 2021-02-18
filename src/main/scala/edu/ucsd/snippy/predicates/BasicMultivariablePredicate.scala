package edu.ucsd.snippy.predicates

import edu.ucsd.snippy.PostProcessor
import edu.ucsd.snippy.ast.ASTNode

class BasicMultivariablePredicate(val predicates: Map[String, SingleVariablePredicate]) extends Predicate {
	override def evaluate(program: ASTNode): Option[String] = {
		this.predicates.foreachEntry((_, pred) => pred.evaluate(program))

		if (this.predicates.forall(entry => entry._2.program.isDefined)) {
			val programList = this.predicates.map(entry => entry._1 -> entry._2).toList
			val lhs = programList.map(_._1).mkString(", ")
			val rhs = programList.map(_._2).map(pred => PostProcessor.clean(pred.program.get).code).mkString(", ")
			Some(lhs + " = " + rhs)
		} else {
			None
		}
	}
}