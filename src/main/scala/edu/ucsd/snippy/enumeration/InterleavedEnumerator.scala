package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.predicates.{FalsePredicate, MultiEdge, MultilineMultivariablePredicate, Node, SingleEdge}
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

/**
 * This enumerator is a top-level-only enumerator used only with multiline multivariable predicates
 * that interleaves calls to multiple enumerators (one per-node) and looks for solutions as it goes.
 * Sadly, the existing API really isn't fit for this interconnection between the enumerator and
 * predicate. So to quote a great programmer, "ridiculously dirty, here we go"
 */
class InterleavedEnumerator(
	val predicate: MultilineMultivariablePredicate,
	override val vocab: VocabFactory,
	override val oeManager: OEValuesManager,
	override val contexts: List[Map[String, Any]],
	size: Boolean,
	variables: List[(String, Types)],
	literals: Iterable[String]) extends Enumerator
{
	val enumerators: List[(Node, Enumerator)] = predicate.graphStart.allNodes.map(node => {
		val enumerator = new ProbEnumerator(
			FalsePredicate,
			VocabFactory(variables, literals, size),
			new InputsValuesManager,
			node.state,
			false,
			0,
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](), 100)
		node -> enumerator
	})
	var nextProgram: Option[(ASTNode, Option[String])] = None

	override def hasNext: Boolean = {
		if (this.nextProgram.isEmpty) this.interleaveEnumerators()
		this.nextProgram.isDefined
	}

	override def next(): (ASTNode, Option[String]) = {
		if (this.nextProgram.isEmpty) this.interleaveEnumerators()
		val rs = this.nextProgram.get
		this.nextProgram = None
		rs
	}

	def interleaveEnumerators(): Unit = {
		for ((node, enumerator) <- enumerators) {
			if (enumerator.hasNext) {
				val (program, _) = enumerator.next()

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
								this.nextProgram = Some((program, Some(lst.mkString("\n"))))
								return
							case _ => ()
						}
					} else {
						this.nextProgram = Some(program, None)
					}
				} else {
					// this.oeManager.remove(program)
					enumerator.oeManager.remove(program)
					this.nextProgram = Some(program, None)
				}
			}
		}
	}
}