package edu.ucsd.snippy.predicates

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.utils.{Assignment, BasicMultivariableAssignment, MultilineMultivariableAssignment, SingleAssignment}

import scala.collection.mutable

class MultilineMultivariablePredicate(val graphStart: Node) extends Predicate {
	override def evaluate (program: ASTNode): Option[MultilineMultivariableAssignment] = {
		if (!program.usesVariables) return None

		// Update the graph
		if (this.graphStart.update(program)) {
			// The graph was changed. See if we have a complete path to the end now
			this.traverse(graphStart) match {
				case None => None
				case Some(lst) => Some(MultilineMultivariableAssignment(lst))
			}
		} else {
			None
		}
	}

	def traverse(node: Node): Option[List[Assignment]] =
	{
		if (node.isEnd) {
			Some(Nil)
		} else {
			for (edge <- node.edges) {
				if (edge.isComplete) {
					(edge, traverse(edge.child)) match {
						case (_, None) => ()
						case (edge: SingleEdge, Some(assignments)) =>
							return Some(SingleAssignment(edge.variable, edge.program.get) :: assignments)
						case (edge: MultiEdge, Some(assignments)) =>
							val orderedPrograms = edge.programs.toList
							return Some(BasicMultivariableAssignment(orderedPrograms.map(_._1), orderedPrograms.map(_._2.get)) :: assignments)
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
	def allNodes: List[Node] =
	{
		this :: this.edges.flatMap(_.child.allNodes)
	}

	def update(program: ASTNode): Boolean = {
		var graphChanged = false

		// Check, for this starting state, what the final values of the program are:
		val values: List[Option[Any]] = program.values
			.zipWithIndex
			.filter(tup => this.valueIndices.contains(tup._2))
			.map(_._1)

		if (!values.contains(None)) {
			// Now, values contains the output of this program in this state.
			// We need to check if assigning this program to any of the "old" variables will
			// take us to another node, and if so, add that as the edge between them.

			for (edge <- this.edges) {
				edge match {
					case edge: SingleEdge =>
						if (edge.program.isEmpty &&
							edge.outputType == program.nodeType &&
							edge.child.state
								.map(_(edge.variable))
								.zip(values)
								.forall(tup => tup._1 == tup._2.get)) {
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
									.forall(tup => tup._1 == tup._2.get)) {
								edge.programs.update(variable, Some(program))
								graphChanged = true
							}
						}
				}

				// Propagate the program through the rest of the graph
				graphChanged |= edge.child.update(program)
			}
		}

		graphChanged
	}
}