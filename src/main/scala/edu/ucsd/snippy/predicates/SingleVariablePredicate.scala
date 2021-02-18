package edu.ucsd.snippy.predicates

import edu.ucsd.snippy.PostProcessor
import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.OEValuesManager

class SingleVariablePredicate(
	val oeManager: OEValuesManager,
	val varName: String,
	val retType: Types,
	val values: List[Any]) extends Predicate {
	var program: Option[ASTNode] = None

	override def evaluate(program: ASTNode): Option[String] = {
		if (this.program.isDefined || program.nodeType != this.retType) {
			None
		} else {
			val results = values
				.zip(program.values)
				.map(pair => pair._1 == pair._2)

			if (results.forall(identity)) {
				if (program.usesVariables) {
					this.program = Some(program)
					Some(this.varName + " = " + PostProcessor.clean(program).code)
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

