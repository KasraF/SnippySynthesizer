package edu.ucsd.snippy.solution
import edu.ucsd.snippy.ast.{ASTNode, BoolLiteral, BoolNode, BoolVariable, IntVariable, ListVariable, MapVariable, NegateBool, StringVariable, Types, VariableNode}
import edu.ucsd.snippy.enumeration.Enumerator
import edu.ucsd.snippy.utils.{Assignment, ConditionalAssignment, SingleAssignment}
import edu.ucsd.snippy.utils.Utils.{filterByIndices, getBinaryPartitions}

class ConditionalSingleEnumSingleVarSolutionEnumerator(
	val enumerator: Enumerator,
	val varName: String,
	val retType: Types.Types,
	val values: List[Any],
	val contexts: List[Map[String, Any]]) extends SolutionEnumerator
{
	val stores: List[((Set[Int], Set[Int]), SolutionStore)] = getBinaryPartitions(contexts.indices.toList)
		.map{part =>
			val store = new SolutionStore(
				filterByIndices(values, part._1),
				filterByIndices(values, part._2))
			val thenSource = filterByIndices(contexts,part._1)
			if (thenSource.forall(_.contains(varName)) && thenSource.map(_(varName)).zip(store.thenVals).forall(t => t._1 == t._2))
				store.thenCase = Some(VariableNode.nodeFromType(varName,retType, contexts))
			val elseSource = filterByIndices(contexts,part._2)
			if (elseSource.forall(_.contains(varName)) && elseSource.map(_(varName)).zip(store.elseVals).forall(t => t._1 == t._2))
				store.elseCase = Some(VariableNode.nodeFromType(varName,retType, contexts))
			part -> store
		}
	var solution: Option[Assignment] = None

	override def step(): Unit = {
		val program = enumerator.next()
		val paths = for (((thenPart, elsePart), store) <- stores) yield {
			var updated = false

			if (store.cond.isEmpty &&
				program.nodeType == Types.Bool &&
				program.values.forall(_.isDefined)) {

				if (program.values.asInstanceOf[List[Option[Boolean]]]
					.map(_.get)
					.zipWithIndex
					.forall(tup =>
						(thenPart.contains(tup._2) && tup._1) ||
							(elsePart.contains(tup._2) && !tup._1))) {
					// Is the correct condition for the `true` case
					if (program.usesVariables) {
						store.cond = Some(program.asInstanceOf[BoolNode])
						updated = true
					} else {
						enumerator.oeManager.remove(program)
					}
				} else if (program.values.asInstanceOf[List[Option[Boolean]]]
					.zipWithIndex
					.forall(tup =>
						(thenPart.contains(tup._2) && !tup._1.get) ||
							(elsePart.contains(tup._2) && tup._1.get))) {
					// Is the correct condition for the `false` case, so invert it!
					if (program.usesVariables) {
						store.cond = Some(NegateBool(program.asInstanceOf[BoolNode]))
						updated = true
					} else {
						enumerator.oeManager.remove(program)
					}
				}
			}

			if (program.nodeType == this.retType) {
				if (store.thenCase.isEmpty &&
					filterByIndices(program.values, thenPart)
						.zip(store.thenVals)
						.forall(tup => tup._1.isDefined && tup._1.get == tup._2)) {
					if (program.usesVariables) {
						store.thenCase = Some(program)
						updated = true
					} else {
						enumerator.oeManager.remove(program)
					}
				}

				if (store.elseCase.isEmpty &&
					filterByIndices(program.values, elsePart)
						.zip(store.elseVals)
						.forall(tup => tup._1.isDefined && tup._1.get == tup._2)) {
					if (program.usesVariables) {
						store.elseCase = Some(program)
						updated = true
					} else {
						enumerator.oeManager.remove(program)
					}
				}
			}
			(store, if (updated && store.isComplete())  if (store.cond.get.isInstanceOf[BoolLiteral]) 0 else
				store.thenCase.get.terms + store.elseCase.get.terms + store.cond.get.terms else Int.MaxValue)
		}
		for ((store, weight) <- paths.sortBy(_._2); if store.isComplete()) {
				this.solution = Some(ConditionalAssignment(
					store.cond.get,
					SingleAssignment(varName, store.thenCase.get),
					SingleAssignment(varName, store.elseCase.get)))
				return
			}

	}

	override def programsSeen: Int = enumerator.programsSeen
}

class SolutionStore(val thenVals: List[Any], val elseVals: List[Any]) {
	var cond: Option[BoolNode] = if (elseVals.isEmpty) Some(BoolLiteral(true, thenVals.length)) else None
	var thenCase: Option[ASTNode] = None
	var elseCase: Option[ASTNode] = if (elseVals.isEmpty) Some(BoolLiteral(true, thenVals.length)) else None

	@inline def isComplete(): Boolean = cond.isDefined && thenCase.isDefined && elseCase.isDefined
}