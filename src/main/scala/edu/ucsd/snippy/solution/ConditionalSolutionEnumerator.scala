package edu.ucsd.snippy.solution

import edu.ucsd.snippy.SynthesisTask
import edu.ucsd.snippy.SynthesisTask.Context
import edu.ucsd.snippy.ast.{ASTNode, BoolNode, Types}
import edu.ucsd.snippy.enumeration.{InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.predicates.SingleVariablePredicate
import edu.ucsd.snippy.utils.{Assignment, ConditionalAssignment, SingleAssignment}
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

class ConditionalSolutionEnumerator(
	val parameters: List[(String, Types.Value)],
	val inputContexts: List[Context],
	// val outputEnvs: List[Map[String, Any]],
	val outputValues: List[Any],
	variables: List[(String, Types.Value)],
	additionalLiterals: Iterable[String]) extends SolutionEnumerator
{
	private val partitions: Map[(Set[Int], Set[Int]), Partition] =
		getBinaryPartitions(inputContexts, 0)
			.map { case (thenIndices, elseIndices) =>
				parameters match {
					case (varName, typ) :: Nil =>
						// Just a single variable
						// val outputValues = outputEnvs.map(_(varName))

						if (elseIndices.isEmpty) {
							val predicate = new SingleVariablePredicate(
								new InputsValuesManager,
								varName,
								typ,
								filterByIndices(outputValues, thenIndices))
							val enum = new ProbEnumerator(
								VocabFactory.apply(variables, additionalLiterals, size = true),
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
								VocabFactory.apply(variables, additionalLiterals, size = true),
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
								VocabFactory.apply(variables, additionalLiterals, size = true),
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
					case multiple => ???
//						if (elseIndices.isEmpty) {
//							val (_, pred) = SynthesisTask.mulitvariablePredicate(multiple.map(_._1), inputContexts, outputEnvs)
//							val enum = new InterleavedSolutionEnumerator(pred, true, variables, additionalLiterals)
//							(thenIndices, elseIndices) -> UnaryPartition(enum)
//						} else {
//							// Then case
//							val thenInputContexts = this.filterByIndices(inputContexts, thenIndices)
//							val thenOutputEnvs = this.filterByIndices(outputEnvs, thenIndices)
//							val (_, thenPred) = SynthesisTask.mulitvariablePredicate(multiple.map(_._1), thenInputContexts, thenOutputEnvs)
//							val thenEnum = new InterleavedSolutionEnumerator(thenPred, true, variables, additionalLiterals)
//
//							// Else case
//							val elseInputContexts = this.filterByIndices(inputContexts, elseIndices)
//							val elseOutputEnvs = this.filterByIndices(outputEnvs, elseIndices)
//							val (_, elsePred) = SynthesisTask.mulitvariablePredicate(multiple.map(_._1), elseInputContexts, elseOutputEnvs)
//							val elseEnum = new InterleavedSolutionEnumerator(elsePred, true, variables, additionalLiterals)
//
//							(thenIndices, elseIndices) -> BinaryPartition(thenEnum, elseEnum)
//						}
				}
			}.toMap
	val condEnumerator = new ProbEnumerator(
		VocabFactory.apply(variables, additionalLiterals, size = true),
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
		for (next <- condEnumerator) {
			if (next.nodeType == Types.Bool) {
				val key = next.values.asInstanceOf[List[Boolean]].zipWithIndex.foldLeft((Set[Int](), Set[Int]())) {
					case ((thenIdxs, elseIdxs), (cond, idx)) => if (cond) {
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

				part.condition = Some(next.asInstanceOf[BoolNode])
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
				return
			}
		}
	}

	private def filterByIndices[T](lst: List[T], indices: Set[Int]): List[T] =
		lst.zipWithIndex.filter(tup => indices.contains(tup._2)).map(_._1)

	private def getBinaryPartitions[T](lst: List[T], startingIdx: Int): List[(Set[Int], Set[Int])] = lst match {
		case Nil => List((Set(), Set()))
		case _ :: Nil => List((Set(startingIdx), Set()))
		case _ :: tail =>
			val parts: List[(Set[Int], Set[Int])] = getBinaryPartitions(tail, startingIdx + 1)
			parts.map(p => (p._1 + startingIdx, p._2)) ++ parts.map(p => (p._1, p._2 + startingIdx))
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
}
