//package edu.ucsd.snippy.solution
//
//import edu.ucsd.snippy.SynthesisTask.Context
//import edu.ucsd.snippy.ast.Types
//import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, ProbEnumerator}
//import edu.ucsd.snippy.vocab.VocabFactory
//
//class ConditionalRestrainedSolutionEnumerator(
//	val outputVariables: List[(String, Types.Value)],
//	val inputContexts: List[Context],
//	val outputEnvs: List[Map[String, Any]],
//	variables: List[(String, Types.Value)],
//	additionalLiterals: Iterable[String]) extends SolutionEnumerator
//{
//	val enumerators: List[(Node, Enumerator)] = predicate.graphStart.allNodes.map(node => {
//		val enumerator = new ProbEnumerator(
//			VocabFactory(variables, literals, size),
//			new InputsValuesManager,
//			node.state,
//			false,
//			0,
//			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
//			mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
//			100)
//		node -> enumerator
//	})
//	var solution: Option[Assignment] = None
//
//	def step(): Unit = {
//		for ((node, enumerator) <- enumerators) {
//			if (enumerator.hasNext) {
//				val program = enumerator.next()
//
//				if (program.usesVariables) {
//					val values: List[Any] = program.values
//					var graphChanged = false
//
//					for (edge <- node.edges) {
//						edge match {
//							case edge: SingleEdge =>
//								if (edge.program.isEmpty &&
//									edge.child.state
//										.map(_ (edge.variable))
//										.zip(values)
//										.forall(tup => tup._1 == tup._2)) {
//									edge.program = Some(program)
//									graphChanged = true
//								}
//							case edge: MultiEdge =>
//								// We need to check each variable
//								for ((variable, programOpt) <- edge.programs) {
//									if (programOpt.isEmpty &&
//										edge.child.state
//											.map(_ (variable))
//											.zip(values)
//											.forall(tup => tup._1 == tup._2)) {
//										edge.programs.update(variable, Some(program))
//										graphChanged = true
//									}
//								}
//						}
//					}
//
//					if (graphChanged) {
//						// See if we found a solution
//						predicate.traverse(predicate.graphStart) match {
//							case Some(lst) =>
//								this.solution = Some(MultilineMultivariableAssignment(lst))
//								return
//							case _ => ()
//						}
//					}
//				} else {
//					// this.oeManager.remove(program)
//					enumerator.oeManager.remove(program)
//				}
//			}
//		}
//	}
//
//	private def filterByIndices[T](lst: List[T], indices: Set[Int]): List[T] =
//		lst.zipWithIndex.filter(tup => indices.contains(tup._2)).map(_._1)
//
//	private def getBinaryPartitions[T](lst: List[T], startingIdx: Int = 0): List[(Set[Int], Set[Int])] = lst match {
//		case Nil => List((Set(), Set()))
//		case _ :: Nil => List((Set(startingIdx), Set()))
//		case _ :: tail =>
//			val parts: List[(Set[Int], Set[Int])] = getBinaryPartitions(tail, startingIdx + 1)
//			parts.map(p => (p._1 + startingIdx, p._2)) ++ parts.map(p => (p._1, p._2 + startingIdx))
//	}
//}
