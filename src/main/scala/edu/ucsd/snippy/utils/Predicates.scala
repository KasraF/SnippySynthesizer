package edu.ucsd.snippy.utils

import edu.ucsd.snippy.{PostProcessor, SynthesisTask}
import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types

import scala.util.control.Breaks.{break, breakable}

object Predicate {
	def getPredicate(
		varName: String,
		envs: List[Map[String, Any]],
		task: SynthesisTask): SingleVariablePredicate =
	{
		val values = envs.flatMap(map => map.filter(_._1 == varName).values)
		new SingleVariablePredicate(task, varName, Utils.getTypeOfAll(values), values)
	}
}

trait Predicate
{
	def evaluate(program: ASTNode): Option[String]
}

class SingleVariablePredicate(
	val task: SynthesisTask,
	val varName: String,
	val retType: Types,
	val values: List[Any],
) extends Predicate {
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
					this.task.oeManager.remove(program)
					None
				}
			} else {
				None
			}
		}
	}
}

class MultivariablePredicate(val predicates: Map[String, SingleVariablePredicate]) extends Predicate {

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