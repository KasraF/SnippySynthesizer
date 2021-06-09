package edu.ucsd.snippy.predicates

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.enumeration.OEValuesManager
import edu.ucsd.snippy.utils.{Assignment, Utils}

object Predicate {
	def getPredicate(
		varName: String,
		values: List[Option[Any]],
		oeManager: OEValuesManager): SingleVariablePredicate =
	{
		new SingleVariablePredicate(
			oeManager,
			varName,
			Utils.getTypeOfAll(values.filter(_.isDefined).map(_.get)),
			values)
	}
}

trait Predicate
{
	def evaluate(program: ASTNode): Option[Assignment]
}