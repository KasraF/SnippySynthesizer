package edu.ucsd.snippy.predicates

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.utils.BasicMultivariableAssignment

import scala.collection.mutable

class BasicMultivariablePredicate(val predicates: Map[String, SingleVariablePredicate]) extends Predicate {
	val programs: mutable.Map[String, Option[ASTNode]] = new mutable.HashMap().addAll(predicates.keys.map(_ -> None))

	override def evaluate(program: ASTNode): Option[BasicMultivariableAssignment] = {
		this.predicates.foreachEntry((name, pred) => pred.evaluate(program) match {
			case Some(assignment) => this.programs.addOne(name, Some(assignment.program))
			case None => ()
		})

		if (this.programs.values.forall(_.isDefined)) {
			val rs = programs.map(tup => (tup._1, tup._2.get)).toList
			Some(BasicMultivariableAssignment(rs.map(_._1), rs.map(_._2)))
		} else {
			None
		}
	}
}