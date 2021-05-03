package edu.ucsd.snippy.solution
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.predicates.MultilineMultivariablePredicate
import edu.ucsd.snippy.utils.Utils.{falseForIndices, filterByIndices, getBinaryPartitions, trueForIndices}
import edu.ucsd.snippy.utils._
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

class ConditionalSingleEnumMultivarSolutionEnumerator(
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
	val graph: Node = Node.convert(predicate.graphStart, partitions, variables, literals)
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
			this.graph.computeShortestPaths()
			if (this.solution.isEmpty) {
				val paths = for ((condStore, index) <- this.conditionals.zipWithIndex; if condStore.cond.isDefined) yield {
					val weight = if (graph.distancesToEnd(index).thenPath._1 == Int.MaxValue || graph.distancesToEnd(index).elsePath._1 == Int.MaxValue) Int.MaxValue
					else if (index == 0) 0
					else graph.distancesToEnd(index).thenPath._1 + graph.distancesToEnd(index).elsePath._1 + condStore.cond.get.terms
					(condStore,weight,index)
				}
				val (condStore, _, index) = paths.minBy(_._2)

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

	override def programsSeen: Int = this.graph.programsSeen
}

case class Variable(name: String, typ: Types)

class CondStore {
	var cond: Option[BoolNode] = None
}

class ProgStore(val indices: Set[Int], val values: List[Any]) {
	var program: Option[ASTNode] = None
}

case class CondProgStore(thenCase: ProgStore, elseCase: ProgStore) {
	def isComplete: Boolean = thenCase.program.isDefined && elseCase.program.isDefined
}

case class Edge(
	parent: Node,
	child: Node,
	variables: Map[Variable, List[CondProgStore]]) {
	override def toString: String = "Edge"
}

object Node {
	def createProgStore(
		prevEnvs: List[Map[String, Any]],
		envs: List[Map[String, Any]],
		variable: Variable,
		partition: (Set[Int], Set[Int])): CondProgStore = {
		val values = envs.map(_(variable.name))
		val rs = CondProgStore(
			new ProgStore(partition._1, filterByIndices(values, partition._1)),
			new ProgStore(partition._2, filterByIndices(values, partition._2)))

		// If the else case is empty, we can set it to any program, and it will be removed in post-
		// processing
		if (partition._2.isEmpty) {
			rs.elseCase.program = Some(BoolLiteral(value = true, partition._1.size))
		}

		// If the variable doesn't change between envs, assign it to itself so we can trivially
		// remove the assignment in post.
		val prevValues = prevEnvs.map(_.get(variable.name))
		val thenValues = filterByIndices(prevValues, partition._1)
		if (thenValues.forall(_.isDefined) &&
			thenValues.map(_.get) == rs.thenCase.values) {
			rs.thenCase.program = VariableNode.nodeFromType(variable.name,variable.typ,envs)
		}

		val elseValues = filterByIndices(prevValues, partition._2)
		if (elseValues.forall(_.isDefined) && elseValues.map(_.get) == rs.elseCase.values) {
			rs.elseCase.program = VariableNode.nodeFromType(variable.name,variable.typ,envs)
		}

		// If we have more than one example, with all values constants, we should trivially assign
		// to a constant
		if (rs.thenCase.program.isEmpty) {
			rs.thenCase.program = Utils.synthesizeLiteralOption(variable.typ, rs.thenCase.values)
		}

		if (rs.elseCase.program.isEmpty) {
			rs.elseCase.program = Utils.synthesizeLiteralOption(variable.typ, rs.elseCase.values)
		}

		rs
	}

	def convert (
		parent: edu.ucsd.snippy.predicates.Node,
		partitionIndices: List[(Set[Int], Set[Int])],
		variables: List[(String, Types)],
		literals: Iterable[String],
		seen: mutable.Map[edu.ucsd.snippy.predicates.Node,Node] = mutable.Map.empty): Node = {
		if (seen.contains(parent)) return seen(parent)

		val enumerator = new ProbEnumerator(
			VocabFactory(variables, literals),
			new InputsValuesManager,
			parent.state,
			false,
			0,
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
			100)
		val n: Node = Node(enumerator, parent.state, Nil, partitionIndices)
		val edges = parent.edges
			.map{e =>
				e -> convert(e.child, partitionIndices, variables, literals, seen)
			}
			.map {
				case (edu.ucsd.snippy.predicates.SingleEdge(_, variable, outputType, _, _), child) =>
					val newVariable = Variable(variable, outputType)
					val stores = partitionIndices.map(indices => createProgStore(parent.state, child.state, newVariable, indices))
					Edge(n, child, List(newVariable -> stores).toMap)
				case (edu.ucsd.snippy.predicates.MultiEdge(_, outputTypes, _, _), child) =>
					val variables = outputTypes.map {
						case (variable, outputType) =>
							val newVariable = Variable(variable, outputType)
							val stores = partitionIndices.map(idxs => createProgStore(parent.state, child.state, newVariable, idxs))
							newVariable -> stores
					}
					Edge(n, child, variables)
			}

		n.edges = edges
		if (n.edges.isEmpty) for (i <- n.distancesToEnd.indices) n.distancesToEnd.update(i,DistancePaths((0,None),(0,None)))
		seen += parent -> n
		n
	}
}

case class DistancePaths(thenPath: (Int,Option[Edge]),elsePath: (Int,Option[Edge]))
case class Node(
	enum: Enumerator,
	state: List[Map[String, Any]],
	var edges: List[Edge],
	partitionIndices: List[(Set[Int], Set[Int])],
	var onStep: ASTNode => Unit = _ => ()) {

	var seen = false
	private def reset_seen(): Unit = {
		seen = false
		for (edge <- edges)
			edge.child.reset_seen()
	}
	def step(): Boolean = {
		reset_seen()
		do_step()
	}
	var done = false
	private def do_step(): Boolean = {
		if (seen)
			false
		else {
			seen = true
			var graphChanged = false

		if (!done && this.enum.hasNext) {
			val program = this.enum.next()

			this.onStep(program)

			for (edge <- this.edges) {
				for ((variable, stores) <- edge.variables) {
					if (variable.typ == program.nodeType) {
						for (store <- stores) {
							if (store.thenCase.program.isEmpty) {
								val programValues = filterByIndices(program.values, store.thenCase.indices)
								if (programValues.zip(store.thenCase.values).forall(Utils.programConnects)) {
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
								if (programValues.zip(store.elseCase.values).forall(Utils.programConnects)) {
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
				graphChanged |= edge.child.do_step()
			}
			if (edges.forall(edge => edge.variables.forall(v => v._2.forall(store => store.isComplete))))
				done = true
		}
		else if (done) { //don't run this enumerator but still run children.
			for (edge <- this.edges) {
				graphChanged |= edge.child.do_step()
			}
		}

		graphChanged
		}
	}

	val distancesToEnd: Array[DistancePaths] = Array.fill(this.partitionIndices.length)(DistancePaths((Int.MaxValue,None),(Int.MaxValue,None)))
	def computeShortestPaths(): Unit = {
		reset_seen()
		do_computeShortest()
	}
	def do_computeShortest(): Unit = {
		if (seen) return
		for (edge <- this.edges) {
			edge.child.do_computeShortest()
			for (i <- distancesToEnd.indices) {
				//then case
				if (edge.variables.map(_._2(i)).forall(store => store.thenCase.program.isDefined) && edge.child.distancesToEnd(i).thenPath._1 < Int.MaxValue) {
					val distanceOnThenEdge = edge.child.distancesToEnd(i).thenPath._1 + edge.variables.map { case (_, stores) =>
						stores(i).thenCase.program.get.terms
					}.sum
					if (distanceOnThenEdge < distancesToEnd(i).thenPath._1 || (distanceOnThenEdge == distancesToEnd(i).thenPath._1 && distancesToEnd(i).thenPath._2.exists(e => edge.variables.size < e.variables.size))) {
						distancesToEnd.update(i,distancesToEnd(i).copy(thenPath = (distanceOnThenEdge,Some(edge))))
					}
				}
				//else case
				if (edge.variables.map(_._2(i)).forall(store => store.elseCase.program.isDefined) && edge.child.distancesToEnd(i).elsePath._1 < Int.MaxValue) {
					val distanceOnElseEdge = edge.child.distancesToEnd(i).elsePath._1 + edge.variables.map{ case (_,stores) =>
						stores(i).elseCase.program.get.terms
					}.sum


					if (distanceOnElseEdge < distancesToEnd(i).elsePath._1 || (distanceOnElseEdge == distancesToEnd(i).elsePath._1 && distancesToEnd(i).elsePath._2.exists(e => edge.variables.size < e.variables.size))) {
						distancesToEnd.update(i,distancesToEnd(i).copy(elsePath = (distanceOnElseEdge,Some(edge))))
					}
				}
			}
		}
		seen = true
	}

	def traverse(partitionIndex: Int): Option[(List[Assignment], List[Assignment])] = {
		if (this.distancesToEnd(partitionIndex).thenPath._2.isEmpty || this.distancesToEnd(partitionIndex).elsePath._2.isEmpty)
			None
		else {
			val thenAssigns = traverse(partitionIndex,thenBranch = true)
			val elseAssigns = traverse(partitionIndex,thenBranch = false)
			for (t <- thenAssigns; e <- elseAssigns) yield (t,e)
		}
	}
	def traverse(partitionIndex: Int, thenBranch: Boolean): Option[List[Assignment]] = {
		if (edges.isEmpty) {
			Some(Nil)
		} else {
			val path = if (thenBranch) this.distancesToEnd(partitionIndex).thenPath else this.distancesToEnd(partitionIndex).elsePath
			path._2.foreach { edge =>
					//if (edge.variables.map(_._2(partitionIndex)).forall(_.isComplete))
					edge.child.traverse(partitionIndex, thenBranch) match {
						case None => ()
						case Some(assign) =>
							val newAssign = if (edge.variables.size == 1) {
								val (variable, store) = edge.variables.head
								if (thenBranch)
									SingleAssignment(variable.name, store(partitionIndex).thenCase.program.get)
								else
									SingleAssignment(variable.name, store(partitionIndex).elseCase.program.get)
							} else {
								val ordered = edge.variables.map(tup => tup._1 -> tup._2(partitionIndex)).toList
								val names = ordered.map(_._1.name)
								if (thenBranch)
									BasicMultivariableAssignment(names, ordered.map(_._2.thenCase.program.get))
								else
									BasicMultivariableAssignment(names, ordered.map(_._2.elseCase.program.get))
							}
							return Some(newAssign :: assign)
					}
			}
			None
		}
	}

	def programsSeen: Int = {
		//This is not a map on purpose. :(
		val seen = mutable.ArrayBuffer[(Node,Int)]()
		get_progCount(seen)
		seen.map(_._2).sum
	}
	private def get_progCount(seen: mutable.ArrayBuffer[(Node, Int)]): Unit = {
		if (!seen.exists(_._1 == this)) {
			seen += (this -> this.enum.programsSeen)

			for (edge <- edges) {
				edge.child.get_progCount(seen)
			}
		}
	}

	def do_print(nodeLabel: Node => String, edgeLabel: Edge => String): Unit = {
		if (seen) return
		println(s"${System.identityHashCode(this).toString} [label=${'"' + nodeLabel(this) + '"'}]")
		for (edge <- edges) {
			edge.child.do_print(nodeLabel,edgeLabel)
			println(s"${System.identityHashCode(this)} -> ${System.identityHashCode(edge.child)} [label=${'"' + edgeLabel(edge) + '"'}]")
		}
		seen = true
	}

	def printGraph(nodeLabel: Node => String, edgeLabel: Edge => String): Unit = {
		reset_seen()
		println("digraph G {")
		do_print(nodeLabel,edgeLabel)
		println("}")
	}

	override def toString: String = "Node"
}
