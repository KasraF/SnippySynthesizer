package edu.ucsd.snippy.utils

import edu.ucsd.snippy.SynthesisTask
import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types

trait Predicate
{
	def evaluate(program: ASTNode, problem: SynthesisTask): Boolean
}

class EqualityPredicate(
	val retType: Types,
	val values: List[Any],
) extends Predicate {
	override def evaluate(program: ASTNode, problem: SynthesisTask): Boolean = {
		val results = values
			.zip(program.values)
			.map(pair => pair._1 == pair._2)
		if (results.forall(identity)) {
			if (program.usesVariables) {
				true
			}
			else {
				problem.oeManager.remove(program)
				false
			}
		} else {
			false
		}
	}
}