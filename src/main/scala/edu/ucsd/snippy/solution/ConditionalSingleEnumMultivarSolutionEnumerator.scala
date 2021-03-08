package edu.ucsd.snippy.solution
import edu.ucsd.snippy.ast.{ASTNode, BoolNode, Types}
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.predicates.MultilineMultivariablePredicate
import edu.ucsd.snippy.utils.Utils.{filterByIndices, getBinaryPartitions}
import edu.ucsd.snippy.utils.{Assignment, BasicMultivariableAssignment, ConditionalAssignment, MultilineMultivariableAssignment, SingleAssignment, Utils}
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

class ConditionalSingleEnumMultivarSolutionEnumerator(
	predicate: MultilineMultivariablePredicate,
	variables: List[(String, Types)],
	literals: Iterable[String]) extends SolutionEnumerator
{
	val partitions = getBinaryPartitions(predicate.graphStart.state.indices.toList)
	val conditionals = this.partitions.map(_ => new CondStore)
	val graph: Node = Node.convert(predicate.graphStart, partitions, variables, literals)
	var solution: Option[Assignment] = None

	// Setup the conditional enum listener
	graph.onStep = (program: ASTNode) => {
		if (program.nodeType == Types.Bool && program.values.forall(_.isDefined)) {
			val values: List[Boolean] = program.values.map(_.get).asInstanceOf[List[Boolean]]
			for ((store, index) <- this.conditionals.zipWithIndex) {
				val (thenIndices, elseIndices) = this.partitions(index)
				if (store.cond.isEmpty &&
					filterByIndices(values, thenIndices).forall(identity) &&
					filterByIndices(values, elseIndices).forall(!_)) {
					if (program.usesVariables) {
						store.cond = Some(program.asInstanceOf[BoolNode])
					} else {
						graph.enum.oeManager.remove(program)
					}
				}
			}
		}
	}

	def step(): Unit = {
		if (this.graph.step()) {
			for ((condStore, index) <- this.conditionals.zipWithIndex) {
				if (condStore.cond.isDefined) {
					graph.traverse(index) match {
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
	}
}

case class Variable(typ: Types, name: String)

class CondStore {
	var cond: Option[BoolNode] = None
}

class ProgStore(val indices: Set[Int], val values: List[Any]) {
	var program: Option[ASTNode] = None
}

case class CondProgStore(thenCase: ProgStore, elseCase: ProgStore) {
	def isComplete(): Boolean = thenCase.program.isDefined && elseCase.program.isDefined
}

case class Edge(
	parent: Node,
	child: Node,
	variables: Map[Variable, List[CondProgStore]])

object Node {
	def convert(
		node: edu.ucsd.snippy.predicates.Node,
		partitionIndices: List[(Set[Int], Set[Int])],
		variables: List[(String, Types)],
		literals: Iterable[String],
		size: Boolean = true): Node = {
		val enumerator = new ProbEnumerator(
			VocabFactory(variables, literals, size),
			new InputsValuesManager,
			node.state,
			false,
			0,
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
			100)
		val n: Node = Node(enumerator, node.state, Nil, partitionIndices)
		val edges = node.edges
			.map(e => e -> convert(e.child, partitionIndices, variables, literals, size))
			.map {
				case (edu.ucsd.snippy.predicates.SingleEdge(_, variable, outputType, _, _), child) =>
					val newVariable = Variable(outputType, variable)
					val stores = partitionIndices.map(idxs => {
						val values = child.state.map(_(variable))
						CondProgStore(
							new ProgStore(idxs._1, filterByIndices(values, idxs._1)),
							new ProgStore(idxs._2, filterByIndices(values, idxs._2)))
					})
					Edge(n, child, List(newVariable -> stores).toMap)
				case (edu.ucsd.snippy.predicates.MultiEdge(programs, outputTypes, _, _), child) => {
					val variables = outputTypes.map {
						case (variable, outputType) => {
							val newVariable = Variable(outputType, variable)
							val stores = partitionIndices.map(idxs => {
								val values = child.state.map(_(variable))
								CondProgStore(
									new ProgStore(idxs._1, filterByIndices(values, idxs._1)),
									new ProgStore(idxs._2, filterByIndices(values, idxs._2)))
							})
							newVariable -> stores
						}
					}
					Edge(n, child, variables)
				}
		}

		n.edges = edges
		n
	}
}

case class Node(
	enum: Enumerator,
	state: List[Map[String, Any]],
	var edges: List[Edge],
	partitionIndices: List[(Set[Int], Set[Int])],
	var onStep: ASTNode => Unit = _ => ()) {

	def step(): Boolean = {
		var graphChanged = false

		if (this.enum.hasNext) {
			val program = this.enum.next()

			this.onStep(program)

			for (edge <- this.edges) {
				for ((variable, stores) <- edge.variables) {
					if (variable.typ == program.nodeType) {
						for (store <- stores) {
							if (store.thenCase.program.isEmpty) {
								val programValues = filterByIndices(program.values, store.thenCase.indices)
								if (programValues.zip(store.thenCase.values).forall(tup => tup._1.isDefined && tup._1.get == tup._2)) {
									if (program.usesVariables) {
										store.thenCase.program = Some(program)
										graphChanged = true
									} else {
										this.enum.oeManager.remove(program)
									}
								}
							}

							if (store.elseCase.program.isEmpty) {
								val programValues = filterByIndices(program.values, store.elseCase.indices)
								if (programValues.zip(store.elseCase.values).forall(tup => tup._1.isDefined && tup._1.get == tup._2)) {
									if (program.usesVariables) {
										store.elseCase.program = Some(program)
										graphChanged = true
									} else {
										this.enum.oeManager.remove(program)
									}
								}
							}
						}
					}
				}

				graphChanged |= edge.child.step()
			}
		}

		graphChanged
	}

	def traverse(partitionIndex: Int): Option[(List[Assignment], List[Assignment])] = {
		if (edges.isEmpty) {
			Some((Nil, Nil))
		} else {
			for (edge <- this.edges) {
				if (edge.variables.map(_._2(partitionIndex)).forall(_.isComplete()))
					edge.child.traverse(partitionIndex) match {
						case None => ()
						case Some((thenAssign, elseAssign)) =>
							val (newThenAssign, newElseAssign) = if (edge.variables.size == 1) {
								val (variable, store) = edge.variables.head
								(SingleAssignment(variable.name, store(partitionIndex).thenCase.program.get),
									SingleAssignment(variable.name, store(partitionIndex).elseCase.program.get))
							} else {
								val ordered = edge.variables.map(tup => tup._1 -> tup._2(partitionIndex)).toList
								val names = ordered.map(_._1.name)
								(BasicMultivariableAssignment(names, ordered.map(_._2.thenCase.program.get)),
									BasicMultivariableAssignment(names, ordered.map(_._2.elseCase.program.get)))
							}
							return Some(newThenAssign :: thenAssign, newElseAssign :: elseAssign)
					}
				}
			None
		}
	}
}
