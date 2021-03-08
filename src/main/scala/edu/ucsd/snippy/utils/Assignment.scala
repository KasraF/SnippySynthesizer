package edu.ucsd.snippy.utils
import edu.ucsd.snippy.PostProcessor
import edu.ucsd.snippy.ast.{ASTNode, BoolNode, NegateBool, NegateInt}

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
		// Cleanup!

		// First check if the condition is all true or false
		if (cond.values.forall(_.get)) {
			this.cleanup(thenCase).map(_.code).mkString("\n")
		} else if (cond.values.forall(!_.get)) {
			this.cleanup(elseCase).map(_.code).mkString("\n")
		} else {
			// TODO This cleanup code is (a) very ugly and (b) not well tested
			var preCondition: List[Assignment] = List()
			var thenCode: List[Assignment] = this.cleanup(thenCase)
			var elseCode: List[Assignment] = this.cleanup(elseCase)

			while (thenCode.nonEmpty && elseCode.nonEmpty && thenCode.head.code == elseCode.head.code) {
				preCondition +:= thenCode.head
				thenCode = thenCode.tail
				elseCode = elseCode.tail
			}

			val precondString = preCondition.map(_.code).mkString("\n")
			val condString = (thenCode, elseCode) match {
				case (Nil, Nil) => ""
				case (thenCode, Nil) =>
					f"if ${PostProcessor.clean(cond).code}:\n" +
						"\t" + thenCode.map(_.code).mkString("\n\t")
				case (Nil, elseCode) =>
					f"if ${PostProcessor.clean(NegateBool(cond)).code}:\n" +
						"\t" + elseCode.map(_.code).mkString("\n\t")
				case (thenCode, elseCode) =>
					f"if ${PostProcessor.clean(cond).code}:\n" +
						"\t" + thenCode.map(_.code).mkString("\n\t") +
						"\nelse:\n" +
						"\t" + elseCode.map(_.code).mkString("\n\t")
			}

			(precondString, condString) match {
				case ("", "") => "None"
				case ("", rs) => rs
				case (rs, "") => rs
				case (pre, cond) => pre + "\n" + cond
			}
		}
	}

	def cleanup(a: Assignment): List[Assignment] = {
		a match {
			case a: SingleAssignment => a.code.split('=').map(_.trim).toList match {
				case left :: right :: Nil if left == right => Nil
				case _ => a :: Nil
			}
			case a: MultilineMultivariableAssignment =>
				// Remove redundant assignments
				a.assignments
					.filter(a => a.code.split('=').map(_.trim).toList match {
						case left :: right :: Nil => left != right
						case _ => true
					})
			case a => a :: Nil
		}
	}
}