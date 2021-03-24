package edu.ucsd.snippy.solution

import edu.ucsd.snippy.utils.Assignment

trait SolutionEnumerator extends Iterator[Option[Assignment]] {
	def step(): Unit
	def solution: Option[Assignment]
	def programsSeen: Int

	override def hasNext: Boolean = true

	override def next(): Option[Assignment] =
	{
		step()
		solution
	}
}