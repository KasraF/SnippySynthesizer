package edu.ucsd.snippy.utils
import edu.ucsd.snippy.PostProcessor
import edu.ucsd.snippy.ast.{ASTNode, BoolNode, NegateBool, VariableNode}
import edu.ucsd.snippy.utils.Utils.filterByIndices

sealed abstract class Assignment {
	def code(): String
}

case class SingleAssignment(name: String, var program: ASTNode) extends Assignment {
	override def code(): String = f"$name = ${PostProcessor.clean(program).code}"
}

case class BasicMultivariableAssignment(names: List[String], programs: List[ASTNode]) extends Assignment
{
	override def code(): String = {
		val programList = this.names.zip(programs).map(entry => entry._1 -> entry._2)
		val lhs = programList.map(_._1).mkString(", ")
		val rhs = programList.map(_._2).map(pred => PostProcessor.clean(pred).code).mkString(", ")
		f"$lhs = $rhs"
	}
}

case class MultilineMultivariableAssignment(assignments: List[Assignment]) extends Assignment
{
	override def code(): String = assignments.map(_.code).mkString("\n")
}

case class ConditionalAssignment(cond: BoolNode, thenCase: Assignment, elseCase: Assignment) extends Assignment
{
	override def code(): String = {
		// Cleanup!

		// First check if the condition is all true or false
		if (cond.values.forall(_.get)) {
			this.flatten(thenCase).filter(deadCodeFilter).map(_.code()).mkString("\n")
		} else if (cond.values.forall(!_.get)) {
			this.flatten(elseCase).filter(deadCodeFilter).map(_.code()).mkString("\n")
		} else {
			// TODO This cleanup code is (a) very ugly and (b) not well tested
			val thenPartition: Set[Int] = cond.values.zipWithIndex.filter(_._1.get).map(_._2).toSet
			val elsePartition: Set[Int] = cond.values.zipWithIndex.filter(!_._1.get).map(_._2).toSet

			var preCondition: List[Assignment] = List()
			var thenCode: List[Assignment] = this.flatten(thenCase)
			var elseCode: List[Assignment] = this.flatten(elseCase)
			var postCondition: List[Assignment] = List()

			// First, check if one branch is the more general solution. If so, use the general one for both cases.
			for ((thenAssignment, thenIndex) <- thenCode.zipWithIndex) {
				thenAssignment match {
					case SingleAssignment(name, thenProgram) =>
						elseCode.zipWithIndex.find(c => c._1.isInstanceOf[SingleAssignment] && c._1.asInstanceOf[SingleAssignment].name == name) match {
							case Some((SingleAssignment(_, elseProgram), elseIndex)) =>
								// See if we can even make this comparison
								val thenModifiedVariables = this.varsAssigned(thenCode.slice(0, thenIndex))
								val elseModifiedVariables = this.varsAssigned(elseCode.slice(0, elseIndex))

								if (!(thenModifiedVariables.exists(thenProgram.includes) ||
									elseModifiedVariables.exists(elseProgram.includes))) {
									val thenThenValues = filterByIndices(thenProgram.values, thenPartition)
									val elseThenValues = filterByIndices(elseProgram.values, thenPartition)
									val thenElseValues = filterByIndices(thenProgram.values, elsePartition)
									val elseElseValues = filterByIndices(elseProgram.values, elsePartition)

									if (thenThenValues == elseThenValues) {
										// Else is the more general solution
										thenCode(thenIndex).asInstanceOf[SingleAssignment].program = elseProgram
									} else if (thenElseValues == elseElseValues) {
										// Then is the more general solution
										elseCode(elseIndex).asInstanceOf[SingleAssignment].program = thenProgram
									}
								}
							case None => ()
						}
					case _ => ()
				}
			}

			// Now take out common prefixes,...
			while (thenCode.nonEmpty && elseCode.nonEmpty && thenCode.head.code() == elseCode.head.code()) {
				preCondition = preCondition :+ thenCode.head
				thenCode = thenCode.tail
				elseCode = elseCode.tail
			}

			// ...Lines arbitrarily sandwiched b/w other assignments,...
			var i = 0
			while(i < thenCode.length) {
				// TODO This can/should? be part of the loop above. But this whole code is hacky
				//  spaghetti, so ¯\_('')_/¯
				thenCode(i) match {
					case SingleAssignment(name, thenProgram) => {
						elseCode.zipWithIndex.find(a => a._1.isInstanceOf[SingleAssignment] && a._1.asInstanceOf[SingleAssignment].name == name) match {
							case Some((SingleAssignment(_, elseProgram), j)) =>
								val thenModifiedVariables = this.varsAssigned(thenCode.slice(0, i))
								val elseModifiedVariables = this.varsAssigned(elseCode.slice(0, j))
								if (!(thenModifiedVariables.exists(thenProgram.includes) ||
									elseModifiedVariables.exists(elseProgram.includes)) &&
									thenProgram.code == elseProgram.code) {
									preCondition = preCondition :+ thenCode(i)
									thenCode = thenCode.slice(0, i) ++ thenCode.slice(i + 1, thenCode.length)
									elseCode = elseCode.slice(0, i) ++ elseCode.slice(i + 1, elseCode.length)
								} else {
									i += 1
								}
							case _ => i += 1
						}
					}
					case _ => i += 1
				}
			}

			// ...and postfixes.
			while (thenCode.nonEmpty && elseCode.nonEmpty && thenCode.last.code() == elseCode.last.code()) {
				postCondition = thenCode.last :: postCondition
				thenCode = thenCode.slice(0, thenCode.length - 1)
				elseCode = elseCode.slice(0, elseCode.length - 1)
			}

			preCondition = preCondition.filter(deadCodeFilter)
			thenCode = thenCode.filter(deadCodeFilter)
			elseCode = elseCode.filter(deadCodeFilter)
			postCondition = postCondition.filter(deadCodeFilter)

			val preCondString = preCondition.map(_.code()).mkString("\n")
			val postCondString = postCondition.map(_.code()).mkString("\n")
			val condString = (thenCode, elseCode) match {
				case (Nil, Nil) => ""
				case (thenCode, Nil) =>
					f"if ${PostProcessor.clean(cond).code}:\n" +
						"\t" + thenCode.map(_.code()).mkString("\n\t")
				case (Nil, elseCode) =>
					f"if ${PostProcessor.clean(NegateBool(cond)).code}:\n" +
						"\t" + elseCode.map(_.code()).mkString("\n\t")
				case (thenCode, elseCode) =>
					f"if ${PostProcessor.clean(cond).code}:\n" +
						"\t" + thenCode.map(_.code()).mkString("\n\t") +
						"\nelse:\n" +
						"\t" + elseCode.map(_.code()).mkString("\n\t")
			}

			(preCondString, condString, postCondString) match {
				case ("", "", "") => "None"
				case ("", rs, "") => rs
				case (rs, "", "") => rs
				case (pre, cond, "") => pre + "\n" + cond
				case ("", "", post) => post
				case ("", rs, post) => rs + "\n" + post
				case (rs, "", post) => rs + "\n" + post
				case (pre, cond, post) => pre + "\n" + cond + "\n" + post
			}
		}
	}

	def varsAssigned(lst: List[Assignment]): Set[String] =
		lst.foldRight(Set[String]()) {
			case (SingleAssignment(name, _), soFar) => soFar + name
			case (BasicMultivariableAssignment(names, _), soFar) => soFar ++ names
			case (MultilineMultivariableAssignment(assigns), soFar) => soFar ++ varsAssigned(assigns)
			case (ConditionalAssignment(_, thenCase, elseCase), soFar) => soFar ++ varsAssigned(thenCase :: elseCase :: Nil)
		}

	def flatten(a: Assignment): List[Assignment] = a match {
		case a: MultilineMultivariableAssignment => a.assignments
		case a => a :: Nil
	}

	def deadCodeFilter(assign: Assignment): Boolean = assign match {
		case SingleAssignment(name, v: VariableNode[_]) => name != v.name
		case _ => true
	}
}