package edu.ucsd.snippy.solution
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.predicates.MultilineMultivariablePredicate
import edu.ucsd.snippy.utils.Utils.{falseForIndices, filterByIndices, getBinaryPartitions, trueForIndices}
import edu.ucsd.snippy.utils._
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

class ConditionalSingleEnumMultivarSimultaneousSolutionEnumerator(
	predicate: MultilineMultivariablePredicate,
	variables: List[(String, Types)],
	literals: Iterable[String]) extends SolutionEnumerator
{
	val partitions: List[(Set[Int], Set[Int])] = getBinaryPartitions(predicate.graphStart.state.indices.toList)
	val conditionals: List[CondStore] = this.partitions.map(part => {
		val rs = new CondStore
		if (part._2.isEmpty) {
			rs.cond = Some(BoolLiteral(value = true, part._1.size))
		}
		rs
	})
	val graph: Node = Node.convert(
		new edu.ucsd.snippy.predicates.Node(
			predicate.graphStart.state,
			List(predicate.graphStart.edges.filter(_.child.isEnd).head),
			predicate.graphStart.valueIndices,
			predicate.graphStart.isEnd),
		partitions,
		variables,
		literals)
	var solution: Option[Assignment] = None

	// Setup the conditional enum listener
	graph.onStep = {
		case program: BoolNode if program.values.forall(_.isDefined) =>
			val values: List[Boolean] = program.values.map(_.get)
			for ((store, index) <- this.conditionals.zipWithIndex.filter(_._1.cond.isEmpty)) {
				val (thenIndices, elseIndices) = this.partitions(index)
				if (trueForIndices(values, thenIndices) && falseForIndices(values, elseIndices)) {
					if (program.usesVariables) {
						store.cond = Some(program)
					} else {
						graph.enum.oeManager.remove(program)
					}
				} else if (trueForIndices(values, elseIndices) && falseForIndices(values, thenIndices)) {
					if (program.usesVariables) {
						store.cond = Some(NegateBool(program))
					} else {
						graph.enum.oeManager.remove(program)
					}
				}
			}
		case _ => ()
	}

	def step(): Unit = {
		if (this.graph.step()) {
			this.graph.computeShortestPaths
			if (this.solution.isEmpty) {
				val paths = for ((condStore, index) <- this.conditionals.zipWithIndex; if condStore.cond.isDefined) yield {
					val weight = if (graph.distancesToEnd(index).thenPath._1 == Int.MaxValue || graph.distancesToEnd(index).elsePath._1 == Int.MaxValue) Int.MaxValue
					else if (index == 0) 0
					else graph.distancesToEnd(index).thenPath._1 + graph.distancesToEnd(index).elsePath._1 + condStore.cond.get.terms
					(condStore,weight,index)
				}
				val (condStore, _, index) = paths.minBy(_._2)

					graph.traverse(index) match {
						case Some((thenAssignment :: Nil, elseAssignment :: Nil)) =>
							this.solution = Some(ConditionalAssignment(
								condStore.cond.get,
								thenAssignment,
								elseAssignment))
						case Some((thenAssignments, elseAssignments)) =>
							this.solution = Some(ConditionalAssignment(
								condStore.cond.get,
								MultilineMultivariableAssignment(thenAssignments),
								MultilineMultivariableAssignment(elseAssignments)))
						case _ => ()
					}
			}

		}
	}

	override def programsSeen: Int = this.graph.programsSeen
}
