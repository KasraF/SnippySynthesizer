package edu.ucsd.snippy.solution

import edu.ucsd.snippy.SynthesisTask
import edu.ucsd.snippy.SynthesisTask.Context
import edu.ucsd.snippy.ast.{ASTNode, BoolNode, Types}
import edu.ucsd.snippy.enumeration.{InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.predicates.SingleVariablePredicate
import edu.ucsd.snippy.utils.Utils.{filterByIndices, getBinaryPartitions}
import edu.ucsd.snippy.utils.{Assignment, ConditionalAssignment}
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

@deprecated
class ConditionalMegazordSolutionEnumerator(
	val outputVariables: List[(String, Types.Value)],
	val inputContexts: List[Context],
	val outputEnvs: List[Map[String, Any]],
	variables: List[(String, Types.Value)],
	additionalLiterals: Iterable[String]) extends SolutionEnumerator
{
	private val partitions: Map[(Set[Int], Set[Int]), Partition] =
		getBinaryPartitions(inputContexts)
			.map { case (thenIndices, elseIndices) =>
				outputVariables match {
					case (varName, typ) :: Nil =>
						// Just a single variable
						val outputValues = outputEnvs.flatMap(map => map.filter(_._1 == varName).values)

						if (elseIndices.isEmpty) {
							val predicate = new SingleVariablePredicate(
								new InputsValuesManager,
								varName,
								typ,
								filterByIndices(outputValues, thenIndices))
							val enum = new ProbEnumerator(
								VocabFactory.apply(variables, additionalLiterals),
								predicate.oeManager,
								filterByIndices(inputContexts, thenIndices),
								false,
								0,
								mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
								mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
								100)
							(thenIndices, elseIndices) -> UnaryPartition(new BasicSolutionEnumerator(predicate, enum))
						} else {
							// Then case
							val thenPredicate = new SingleVariablePredicate(
								new InputsValuesManager,
								varName,
								typ,
								filterByIndices(outputValues, thenIndices))
							val thenEnum = new ProbEnumerator(
								VocabFactory.apply(variables, additionalLiterals),
								thenPredicate.oeManager,
								filterByIndices(inputContexts, thenIndices),
								false,
								0,
								mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
								mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
								100)

							// Else case
							val elsePredicate = new SingleVariablePredicate(
								new InputsValuesManager,
								varName,
								typ,
								filterByIndices(outputValues, elseIndices))
							val elseEnum = new ProbEnumerator(
								VocabFactory.apply(variables, additionalLiterals),
								elsePredicate.oeManager,
								filterByIndices(inputContexts, elseIndices),
								false,
								0,
								mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
								mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
								100)

							(thenIndices, elseIndices) -> BinaryPartition(
								new BasicSolutionEnumerator(thenPredicate, thenEnum),
								new BasicSolutionEnumerator(elsePredicate, elseEnum))
						}
					case multiple =>
						if (elseIndices.isEmpty) {
							val (_, pred) = SynthesisTask.mulitvariablePredicate(multiple.map(_._1), inputContexts, outputEnvs)
							val enum = new InterleavedSolutionEnumerator(pred, variables, additionalLiterals)
							(thenIndices, elseIndices) -> UnaryPartition(enum)
						} else {
							// Then case
							val thenInputContexts = filterByIndices(inputContexts, thenIndices)
							val thenOutputEnvs = filterByIndices(outputEnvs, thenIndices)
							val (_, thenPred) = SynthesisTask.mulitvariablePredicate(multiple.map(_._1), thenInputContexts, thenOutputEnvs)
							val thenEnum = new InterleavedSolutionEnumerator(thenPred, variables, additionalLiterals)

							// Else case
							val elseInputContexts = filterByIndices(inputContexts, elseIndices)
							val elseOutputEnvs = filterByIndices(outputEnvs, elseIndices)
							val (_, elsePred) = SynthesisTask.mulitvariablePredicate(multiple.map(_._1), elseInputContexts, elseOutputEnvs)
							val elseEnum = new InterleavedSolutionEnumerator(elsePred, variables, additionalLiterals)

							(thenIndices, elseIndices) -> BinaryPartition(thenEnum, elseEnum)
						}
				}
			}.toMap
	val condEnumerator = new ProbEnumerator(
		VocabFactory.apply(variables, additionalLiterals),
		new InputsValuesManager,
		inputContexts,
		false,
		0,
		mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
		mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
		100)

	var solution: Option[Assignment] = None

	override def step(): Unit = {
		// EUSolver algorithm:
		// If none of the partitions are complete, just step partitions and we're done
		if (this.partitions.map(_._2.step()).forall(!_)) return

		// Else if at least one of the partitions is complete,
		//   enumerate conditions and see if we have a complete tuple
		val nextCond = condEnumerator.next()

		if (nextCond.nodeType == Types.Bool && nextCond.values.forall(_.isDefined)) {
			val key = nextCond.values.asInstanceOf[List[Option[Boolean]]].zipWithIndex.foldLeft((Set[Int](), Set[Int]())) {
				case ((thenIdxs, elseIdxs), (cond, idx)) => if (cond.get) {
					(thenIdxs + idx, elseIdxs)
				}else {
					(thenIdxs, elseIdxs + idx)
				}
			}
			val part = if (this.partitions.contains(key)) {
				this.partitions(key)
			} else {
				this.partitions((key._2, key._1))
			}

			part.condition = Some(nextCond.asInstanceOf[BoolNode])
			part match {
				case part: UnaryPartition => if (part.program.isDefined) {
					this.solution = part.program
				}
				case part: BinaryPartition =>
					(part.thenProgram, part.elseProgram, part.condition) match {
						case (Some(thenCase), Some(elseCase), Some(condition)) =>
							this.solution = Some(ConditionalAssignment(condition, thenCase, elseCase))
						case _ => ()
					}
			}
		}
	}

	private sealed abstract class Partition {
		var condition: Option[BoolNode] = None
		def step(): Boolean
	}

	private case class UnaryPartition(enum: SolutionEnumerator) extends Partition {
		var program: Option[Assignment] = None

		override def step(): Boolean = program match {
			case Some(_) => true
			case None =>
				program = enum.next()
				program.isDefined
		}
	}

	private case class BinaryPartition(thenEnum: SolutionEnumerator, elseEnum: SolutionEnumerator) extends Partition
	{
		var thenProgram: Option[Assignment] = None
		var elseProgram: Option[Assignment] = None

		def step(): Boolean = {
			if (thenProgram.isEmpty) {
				this.thenProgram = this.thenEnum.next()
			}

			if (elseProgram.isEmpty) {
				this.elseProgram = this.elseEnum.next()
			}

			this.thenProgram.isDefined && this.elseProgram.isDefined
		}
	}

	override def programsSeen: Int = partitions.map(_._2 match {
		case UnaryPartition(e1) => e1.programsSeen
		case BinaryPartition(e1, e2) => e1.programsSeen + e2.programsSeen
	}).sum + condEnumerator.programsSeen
}
