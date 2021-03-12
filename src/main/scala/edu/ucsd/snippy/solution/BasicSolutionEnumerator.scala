package edu.ucsd.snippy.solution

import edu.ucsd.snippy.enumeration.Enumerator
import edu.ucsd.snippy.predicates.Predicate
import edu.ucsd.snippy.utils.Assignment

class BasicSolutionEnumerator(val predicate: Predicate, val enumerator: Enumerator) extends SolutionEnumerator
{
	var solution: Option[Assignment] = None

	override def step(): Unit =
		if (solution.isEmpty && enumerator.hasNext)
			this.solution = predicate.evaluate(enumerator.next)
}
