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

	def irrelevant(program: ASTNode): Boolean

	def clear(): Unit

	/**
	 * Removes the class of programs if it was previously seen.
	 * @param program An instance of the class of programs to remove.
	 * @return Whether the class of programs had been previously seen.
	 */
	def remove(program: ASTNode): Boolean
}

class InputsValuesManager extends OEValuesManager
{
	val classValues: mutable.Set[List[Option[Any]]] = mutable.HashSet[List[Option[Any]]]()

	override def isRepresentative(program: ASTNode): Boolean =
	{
		try {
			classValues.add(program.values)
		} catch {
			case _: Exception => false
		}
	}

	override def irrelevant(program: ASTNode): Boolean =
	{
		val results: List[Option[Any]] = program.values
		program.includes("var") && program.terms > 1 && results.length > 1 && results.tail.forall(_ == results.head)
	}

	override def clear(): Unit = classValues.clear()

	override def remove(program: ASTNode): Boolean =
		this.classValues.remove(program.values)
}
