package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode

import scala.collection.mutable

/**
 * A trait for implementing Observational Equivalence.
 */
trait OEValuesManager
{
	/**
	 * Returns whether the given program is representative. It can have side effects.
	 */
	def isRepresentative(program: ASTNode): Boolean

	/**
	 * Removes the class of programs if it was previously seen.
	 * @param program An instance of the class of programs to remove.
	 * @return Whether the class of programs had been previously seen.
	 */
	def remove(program: ASTNode): Boolean
}

class InputsValuesManager extends OEValuesManager
{
	val classValues: mutable.Set[List[Any]] = mutable.HashSet[List[Any]]()

	override def isRepresentative(program: ASTNode): Boolean =
	{
		try {
			val results: List[Any] = program.values
			classValues.add(results)
		} catch {
			case _: Exception => false
		}
	}

	override def remove(program: ASTNode): Boolean =
		this.classValues.remove(program.values)
}
