package enumeration

import ast.{ASTNode, BinaryOpNode, LiteralNode, QuaternaryOpNode, TernaryOpNode, UnaryOpNode}

import scala.collection.mutable

trait OEValuesManager
{
	def isRepresentative(program: ASTNode): Boolean
}

// TODO Prioritize cleaner programs
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
}
