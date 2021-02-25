package edu.ucsd.snippy.enumeration

object ProbCosts
{
	def getCosts(childrenCost: Double, childrenCosts: Array[Int], childrenArity: Int): Array[Array[Int]] =
	{
		var candidateCosts = Array[Array[Int]]()
		val combinations = repeatElementsInList(childrenCosts, childrenArity)
			.combinations(childrenArity).filter(c => c.sum == childrenCost)
		candidateCosts = combinations.flatMap(c => c.permutations).toArray
		candidateCosts = candidateCosts.distinct
		candidateCosts
	}

	 def repeatElementsInList(list: Array[Int], times: Int): Array[Int] = {
		list.flatMap (x =>
			List.fill(times)(x)
		)
	}
}
