package edu.ucsd.snippy.predicates

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.enumeration.OEValuesManager
import edu.ucsd.snippy.utils.{Assignment, Utils}

object Predicate {
	def getPredicate(
		varName: String,
		envs: List[Map[String, Any]],
		oeManager: OEValuesManager): SingleVariablePredicate =
	{
		val values = envs.flatMap(map => map.filter(_._1 == varName).values)
		new SingleVariablePredicate(oeManager, varName, Utils.getTypeOfAll(values), values)
	}
}

trait Predicate
{
	def evaluate(program: ASTNode): Option[Assignment]
}