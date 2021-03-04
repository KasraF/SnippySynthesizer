package edu.ucsd.snippy

import edu.ucsd.snippy.ast.{ASTNode, Types}
import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.predicates.{MultiEdge, MultilineMultivariablePredicate, Node, Predicate, SingleEdge}
import edu.ucsd.snippy.utils.{Assignment, MultilineMultivariableAssignment}
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

trait SolutionEnumerator extends Iterator[Option[Assignment]] {
	def step(): Unit
	def solution: Option[Assignment]

	override def hasNext: Boolean = true

	override def next(): Option[Assignment] =
	{
		step()
		solution
	}
}

class BasicSolutionEnumerator(val predicate: Predicate, val enumerator: Enumerator) extends SolutionEnumerator
{
	var solution: Option[Assignment] = None

	override def step(): Unit =
		if (solution.isEmpty && enumerator.hasNext)
			this.solution = predicate.evaluate(enumerator.next)
}

class InterleavedSolutionEnumerator(
	val predicate: MultilineMultivariablePredicate,
	size: Boolean,
	variables: List[(String, Types.Types)],
	literals: Iterable[String]) extends SolutionEnumerator
{
	val enumerators: List[(Node, Enumerator)] = predicate.graphStart.allNodes.map(node => {
		val enumerator = new ProbEnumerator(
			VocabFactory(variables, literals, size),
			new InputsValuesManager,
			node.state,
			false,
			0,
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
			100)
		node -> enumerator
	})
	var solution: Option[Assignment] = None

	def step(): Unit = {
		for ((node, enumerator) <- enumerators) {
			if (enumerator.hasNext) {
				val program = enumerator.next()

				if (program.usesVariables) {
					val values: List[Any] = program.values
					var graphChanged = false

					for (edge <- node.edges) {
						edge match {
							case edge: SingleEdge =>
								if (edge.program.isEmpty &&
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
										edge.child.state
											.map(_ (variable))
											.zip(values)
											.forall(tup => tup._1 == tup._2)) {
										edge.programs.update(variable, Some(program))
										graphChanged = true
									}
								}
						}
					}

					if (graphChanged) {
						// See if we found a solution
						predicate.traverse(predicate.graphStart) match {
							case Some(lst) =>
								this.solution = Some(MultilineMultivariableAssignment(lst))
								return
							case _ => ()
						}
					}
				} else {
					// this.oeManager.remove(program)
					enumerator.oeManager.remove(program)
				}
			}
		}
	}
}


