package edu.ucsd.snippy.solution
import edu.ucsd.snippy.ast.{ASTNode, BoolLiteral, BoolNode, BoolVariable, IntVariable, ListVariable, MapVariable, NegateBool, SetVariable, StringVariable, Types}
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.predicates.MultilineMultivariablePredicate
import edu.ucsd.snippy.utils.Utils.{falseForIndices, filterByIndices, getBinaryPartitions, trueForIndices}
import edu.ucsd.snippy.utils.{Assignment, BasicMultivariableAssignment, ConditionalAssignment, MultilineMultivariableAssignment, SingleAssignment, Utils}
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
			this.graph.computeShortestPaths
			for ((condStore, index) <- this.conditionals.zipWithIndex) {
				if (condStore.cond.isDefined && this.solution.isEmpty) {
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
	variables: Map[Variable, List[CondProgStore]])

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
			rs.thenCase.program = variable match {
				case Variable(name, Types.Bool) => Some(BoolVariable(name, envs))
				case Variable(name, Types.String) => Some(StringVariable(name, envs))
				case Variable(name, Types.Int) => Some(IntVariable(name, envs))
				case Variable(name, Types.IntList) => Some(ListVariable[Int](name, envs, Types.Int))
				case Variable(name, Types.StringList) => Some(ListVariable[String](name, envs, Types.String))
				case Variable(name, Types.IntIntMap) => Some(MapVariable[Int,Int](name, envs, Types.Int, Types.Int))
				case Variable(name, Types.IntStringMap) => Some(MapVariable[Int,String](name, envs, Types.Int, Types.String))
				case Variable(name, Types.StringIntMap) => Some(MapVariable[String,Int](name, envs, Types.String, Types.Int))
				case Variable(name, Types.StringStringMap) => Some(MapVariable[String,String](name, envs, Types.String, Types.String))
			}
		}

		val elseValues = filterByIndices(prevValues, partition._2)
		if (elseValues.forall(_.isDefined) && elseValues.map(_.get) == rs.elseCase.values) {
			rs.elseCase.program = variable match {
				case Variable(name, Types.Bool) => Some(BoolVariable(name, envs))
				case Variable(name, Types.String) => Some(StringVariable(name, envs))
				case Variable(name, Types.Int) => Some(IntVariable(name, envs))
				case Variable(name, Types.IntList) => Some(ListVariable[Int](name, envs, Types.Int))
				case Variable(name, Types.StringList) => Some(ListVariable[String](name, envs, Types.String))
				case Variable(name, Types.IntIntMap) => Some(MapVariable[Int,Int](name, envs, Types.Int, Types.Int))
				case Variable(name, Types.IntStringMap) => Some(MapVariable[Int,String](name, envs, Types.Int, Types.String))
				case Variable(name, Types.StringIntMap) => Some(MapVariable[String,Int](name, envs, Types.String, Types.Int))
				case Variable(name, Types.StringStringMap) => Some(MapVariable[String,String](name, envs, Types.String, Types.String))
				case Variable(name, Types.IntSet) => Some(SetVariable[Int](name, envs, Types.Int))
				case Variable(name, Types.StringSet) => Some(SetVariable[String](name, envs, Types.String))
			}
		}

		rs
	}

	def convert(
		parent: edu.ucsd.snippy.predicates.Node,
		partitionIndices: List[(Set[Int], Set[Int])],
		variables: List[(String, Types)],
		literals: Iterable[String],
		size: Boolean = true): Node = do_convert(parent,partitionIndices,variables,literals,size,mutable.Map.empty)

	private def do_convert (
		parent: edu.ucsd.snippy.predicates.Node,
		partitionIndices: List[(Set[Int], Set[Int])],
		variables: List[(String, Types)],
		literals: Iterable[String],
		size: Boolean,
		seen: mutable.Map[edu.ucsd.snippy.predicates.Node,Node]): Node = {
		if (seen.contains(parent)) return seen(parent)

		val enumerator = new ProbEnumerator(
			VocabFactory(variables, literals, size),
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
				e -> do_convert(e.child, partitionIndices, variables, literals, size, seen)
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
		if (n.edges.isEmpty) for (i <- 0 until n.distancesToEnd.length) n.distancesToEnd.update(i,(0,None))
		seen += parent -> n
		n
	}
}

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

	val distancesToEnd: Array[(Int,Option[Edge])] = Array.fill(this.partitionIndices.length)(Int.MaxValue,None)
	def computeShortestPaths(): Unit = {
		reset_seen()
		do_computeShortest()
	}
	def do_computeShortest(): Unit = {
		if (seen) return
		for (edge <- this.edges) {
			edge.child.do_computeShortest()
			for (i <- 0 until distancesToEnd.length) {
				if (edge.variables.map(_._2(i)).forall(_.isComplete) && edge.child.distancesToEnd(i)._1 < Int.MaxValue) {
					val distanceOnEdge = edge.child.distancesToEnd(i)._1 + edge.variables.map{ case (v,stores) =>
						stores(i).thenCase.program.get.terms + stores(i).elseCase.program.get.terms
					}.sum
					if (distanceOnEdge < distancesToEnd(i)._1 || (distanceOnEdge == distancesToEnd(i)._1 && distancesToEnd(i)._2.map(e => edge.variables.size < e.variables.size).getOrElse(false))) {
						distancesToEnd.update(i,(distanceOnEdge,Some(edge)))
					}
				}
			}
		}
		seen = true
	}

	def traverse(partitionIndex: Int): Option[(List[Assignment], List[Assignment])] = {
		if (edges.isEmpty) {
			Some((Nil, Nil))
		} else {
			//for (edge <- this.edges) {
			this.distancesToEnd(partitionIndex)._2.foreach{edge =>
				//if (edge.variables.map(_._2(partitionIndex)).forall(_.isComplete))
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

	def printGraph(nodeLabel: Node => String, edgeLabel: Edge => String) = {
		reset_seen()
		println("digraph G {")
		do_print(nodeLabel,edgeLabel)
		println("}")
	}
}
