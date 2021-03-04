package edu.ucsd.snippy.utils
import edu.ucsd.snippy.PostProcessor
import edu.ucsd.snippy.ast.{ASTNode, BoolNode}

sealed abstract class Assignment {
	val code: String
}

final case class SingleAssignment(name: String, program: ASTNode) extends Assignment {
	override lazy val code: String = f"$name = ${PostProcessor.clean(program).code}"
}

case class BasicMultivariableAssignment(names: List[String], programs: List[ASTNode]) extends Assignment
{
	override lazy val code: String = {
		val programList = this.names.zip(programs).map(entry => entry._1 -> entry._2)
		val lhs = programList.map(_._1).mkString(", ")
		val rhs = programList.map(_._2).map(pred => PostProcessor.clean(pred).code).mkString(", ")
		f"$lhs = $rhs"
	}
}

case class MultilineMultivariableAssignment(assignments: List[Assignment]) extends Assignment
{
	override lazy val code: String = assignments.map(_.code).mkString("\n")
}

case class ConditionalAssignment(cond: BoolNode, thenCase: Assignment, elseCase: Assignment) extends Assignment
{
	override val code: String = {
		f"if ${PostProcessor.clean(cond).code}:\n" +
			f"\t${thenCase.code.replaceAll("\n", "\n\t")}\n" +
		"else:\n" +
			f"\t${elseCase.code.replaceAll("\n", "\n\t")}"
	}
}