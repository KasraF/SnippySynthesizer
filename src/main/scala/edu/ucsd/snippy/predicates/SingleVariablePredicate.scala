package edu.ucsd.snippy.predicates

import scala.util.matching.Regex
import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.OEValuesManager
import edu.ucsd.snippy.utils.SingleAssignment

class SingleVariablePredicate(
	val oeManager: OEValuesManager,
	val varName: String,
	val retType: Types,
	val values: List[Any]) extends Predicate {

	// CODE BELOW MODIFIED FROM POPPY

	// map all strings "xyz" to "xyz.*"
	def valuesToSpec(s: Any): Any = s match {
		case s: String =>
			("^" + Regex.quote(s) + ".*" + "$").r
		case l: List[Any] => l.map(valuesToSpec)
		case s => s
	}

	val specs: List[Any] = values.map(valuesToSpec)

	/**
	 * compare function for lists specifically. Factored this out of compare(Any, Any) to keep it
	 * clean.
	 * The length of the given (possibly incomplete) spec should be less than or equal to that of the found completion
	 * @param spec The specification to check against
	 * @param sol The solution to check
	 * @return whether the solution matches the spec
	 */
	def compare(spec: List[Any], sol: List[Any]): Boolean = (spec, sol) match {
		// cases of [...] have already been handled; here are the base cases
		//case (Ellipsis :: t1, _ :: t2) => compare(t1, t2) || compare(spec, t2)
		case (h1 :: t1, h2 :: t2) => compare(h1, h2) && compare(t1, t2)
		case (Nil, _) => true
		case _ => false
	}

	/**
	 * Use types to match everything!
	 * @param spec The specification to check against
	 * @param sol The solution to check
	 * @return whether the solution matches the spec
	 */
	def compare(spec: Any, sol: Any): Boolean = (spec, sol) match {
		case (spec: List[Any], sol: List[Any]) => compare(spec, sol)
		case (spec: Regex, sol: String) => spec.matches(sol)
		case _ => spec == sol
	}

	// RETURNED TO ORIGINAL LOOPY CODE
	override def evaluate(program: ASTNode): Option[SingleAssignment] = {
		if (program.nodeType != this.retType) {
			None
		} else {
			val result = specs
				.zip(program.values)
				.forall { case (spec, sol) => compare(spec, sol) }

			if (result) {
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

	// ORIGINAL LOOPY CODE
//	override def evaluate(program: ASTNode): Option[SingleAssignment] = {
//		if (program.nodeType != this.retType || program.values.contains(None)) {
//			None
//		} else {
//			if (values.zip(program.values).forall(pair => pair._1 == pair._2.get)) {
//				if (program.usesVariables) {
//					Some(SingleAssignment(this.varName, program))
//				}
//				else {
//					this.oeManager.remove(program)
//					None
//				}
//			} else {
//				None
//			}
//		}
//	}
}

