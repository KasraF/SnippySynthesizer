package edu.ucsd.snippy.predicates
import edu.ucsd.snippy.ast.ASTNode

object FalsePredicate extends Predicate {
	override def evaluate(program: ASTNode): Option[String] = None
}
