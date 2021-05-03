package edu.ucsd.snippy.predicates

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.OEValuesManager
import edu.ucsd.snippy.utils.SingleAssignment

class SingleVariablePredicate(
	val oeManager: OEValuesManager,
	val varName: String,
	val retType: Types,
	val values: List[Any]) extends Predicate {

	override def evaluate(program: ASTNode): Option[SingleAssignment] = {
		if (program.nodeType != this.retType || program.values.contains(None)) {
			None
		} else {
			if (values.zip(program.values).forall(pair => pair._1 == pair._2.get)) {
				if (program.usesVariables) {
					Some(SingleAssignment(this.varName, program))
				}
				else {
					this.oeManager.remove(program)
					None
				}
			} else {
				None
			}
		}
	}
}

