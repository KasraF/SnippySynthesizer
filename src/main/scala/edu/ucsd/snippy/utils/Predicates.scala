package edu.ucsd.snippy.utils

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.{PostProcessor, SynthesisTask}

import scala.collection.mutable

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

class MultilineMultivariablePredicate(val graphStart: Node) extends Predicate {
	override def evaluate (program: ASTNode): Option[String] = {
		if (!program.usesVariables) return None

		// Update the graph
		if (this.graphStart.update(program)) {
			// The graph was changed. See if we have a complete path to the end now
			this.traverse(graphStart) match {
				case None => None
				case Some(lst) => Some(lst.mkString("\n"))
			}
		} else {
			None
		}
	}

	def traverse(node: Node): Option[List[String]] =
	{
		if (node.isEnd) {
			Some(Nil)
		} else {
			for (edge <- node.edges) {
				if (edge.isComplete) {
					(edge, traverse(edge.child)) match {
						case (_, None) => ()
						case (edge: SingleEdge, Some(assignments)) =>
							return Some(edge.variable + " = " + PostProcessor.clean(edge.program.get).code :: assignments)
						case (edge: MultiEdge, Some(assignments)) =>
							return Some(
								{
									val orderedPrograms = edge.programs.toList
									val variables = orderedPrograms.map(_._1).mkString(", ")
									val programs = orderedPrograms.map(_._2.get).map(PostProcessor.clean).map(_.code).mkString(", ")
									variables + " = " + programs
								} :: assignments)
					}
				}
			}

			None
		}
	}
}

abstract sealed class Edge(
	val parent: Node,
	val child: Node,
) {
	def isComplete: Boolean
}

case class SingleEdge(
	var program: Option[ASTNode],
	variable: String,
	outputType: Types,
	override val parent: Node,
	override val child: Node,
) extends Edge(parent, child) {
	override def isComplete: Boolean = program.isDefined
}

case class MultiEdge(
	var programs: mutable.Map[String, Option[ASTNode]],
	var outputTypes: Map[String, Types],
	override val parent: Node,
	override val child: Node,
) extends Edge(parent, child) {
	override def isComplete: Boolean = this.programs.values.forall(_.isDefined)
}

class Node(
	val state: List[Map[String, Any]],
	var edges: List[Edge],
	val valueIndices: List[Int],
	var isEnd: Boolean)
{
	def update(program: ASTNode): Boolean = {

		var graphChanged = false

		// Check, for this starting state, what the final values of the program are:
		val values: List[Any] = program.values
			.zipWithIndex
			.filter(tup => this.valueIndices.contains(tup._2))
			.map(_._1)

		// Now, values contains the output of this program in this state.
		// We need to check if assigning this program to any of the "old" variables will
		// take us to another node, and if so, add that as the edge between them.

		for (edge <- this.edges) {
			edge match {
				case edge: SingleEdge =>
					if (edge.program.isEmpty &&
						edge.outputType == program.nodeType &&
						edge.child.state
							.map(_ (edge.variable))
							.zip(values)
							.forall(tup => tup._1 == tup._2)) {
						edge.program = Some(program)
						graphChanged = true
					}
				case edge: MultiEdge =>
					// We need to check each variable
					for ((variable, programOpt) <- edge.programs) {
						if (programOpt.isEmpty &&
							edge.outputTypes(variable) == program.nodeType &&
							edge.child.state
								.map(_ (variable))
								.zip(values)
								.forall(tup => tup._1 == tup._2)) {
							edge.programs.update(variable, Some(program))
							graphChanged = true
						}
					}
			}

			// Propagate the program through the rest of the graph
			graphChanged |= edge.child.update(program)
		}

		graphChanged
	}
}