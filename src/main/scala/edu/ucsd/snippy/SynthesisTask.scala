package edu.ucsd.snippy

import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration.{BasicEnumerator, InputsValuesManager, OEValuesManager}
import edu.ucsd.snippy.predicates._
import edu.ucsd.snippy.solution.{BasicSolutionEnumerator, ConditionalSolutionEnumerator, InterleavedSolutionEnumerator, SolutionEnumerator}
import edu.ucsd.snippy.utils._
import edu.ucsd.snippy.vocab._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonParser

import scala.collection.mutable

class SynthesisTask(
	// Problem definition
	val parameters: List[(String, ast.Types.Value)],
	val outputVariables: List[String],
	val vocab     : VocabFactory,
	val contexts: List[Map[String, Any]],
	val predicate: Predicate,

	// Synthesizer state
	val oeManager : OEValuesManager,
	val enumerator: SolutionEnumerator)
{
	override def toString: String =
	{
		s"\tparameters: $parameters\n" +
			"\tvocab: [...]\n" +
			s"\tcontexts: $contexts" +
			s"\tpredicate: $predicate"
	}
}

object SynthesisTask
{
	type Context = Map[String, Any]
	val reserved_names: Set[String] =
		Set("time", "#", "$", "lineno", "prev_lineno", "next_lineno", "__run_py__")

	/**
	 * Checks whether we can use the output variables' previous values in the environment. To do so
	 * we need:
	 * 1. The variable to have been defined in the previous env
	 * 2. If in a loop, we have the full trace of the loop, and know what the previous value
	 * of the variable at each iteration of the loop was.
	 *
	 * @param previousEnv    The environment immediately before the first env
	 * @param outputVarNames The names of the output variables.
	 * @param envs           The environments we are going to use as synthesis spec.
	 * @return whether LooPy is enabled.
	 */
	def isLoopy(
		previousEnv   : Map[String, Any],
		outputVarNames: List[String],
		envs          : List[Map[String, Any]]
	): Boolean =
	{
		previousEnv.nonEmpty &&
			outputVarNames.forall(varName => previousEnv.contains(varName)) &&
			envs.head.contains("#") &&
			// Not in a loop, but reusing variable
			(envs.head("#").asInstanceOf[String].isEmpty ||
				// In a loop
				(envs.head("#").asInstanceOf[String].nonEmpty &&
					envs.tail.foldLeft(envs.head("#").asInstanceOf[String].toInt)((currIndex, env) => {
						if (currIndex >= 0 && env("#").asInstanceOf[String].toInt == currIndex + 1) {
							currIndex + 1
						} else {
							-1
						}
					}) != -1))
	}

	def fromString(jsonString: String, size: Boolean = true): SynthesisTask = {
		val input = JsonParser.parse(jsonString).asInstanceOf[JObject].values
		val outputVarNames: List[String] = input("varNames").asInstanceOf[List[String]]
		val envs: List[Map[String, Any]] = input("envs").asInstanceOf[List[Map[String, Any]]]
		val previousEnv: Map[String, Any] =
			cleanupInputs(input("previous_env").asInstanceOf[Map[String, Any]])
		val processedEnvs: List[Map[String, Any]] = envs
			.asInstanceOf[List[Map[String, Any]]]
			.map(cleanupInputs)
		val loopy = isLoopy(previousEnv, outputVarNames, envs)

		var contexts: List[Context] = if (loopy) {
			processedEnvs
				.zip(previousEnv :: processedEnvs.slice(0, processedEnvs.length - 1))
				.map {
					case (curr_env, prev_env) =>
						curr_env
							.filter(entry => !outputVarNames.contains(entry._1))
							.filter(_._1 != "#") ++
							outputVarNames.collect(varName => varName -> prev_env(varName)).toMap
				}
		} else {
			// Not in a loop. Do the usual.
			processedEnvs.map(env => env.filter(entry => !outputVarNames.contains(entry._1)).filter(_._1 != "#"))
		}

		val oeManager = new InputsValuesManager
		val additionalLiterals = getStringLiterals(processedEnvs, outputVarNames)

		val predicate: Predicate = outputVarNames match {
			case single :: Nil => Predicate.getPredicate(single, processedEnvs, oeManager)
			case multiple if loopy =>
				val (newContexts, pred) = this.mulitvariablePredicate(multiple, contexts, processedEnvs)
				contexts = newContexts
				pred
			case multiple => new BasicMultivariablePredicate(multiple.map(varName => varName -> Predicate.getPredicate(varName, processedEnvs, oeManager)).toMap)
		}

		val parameters =
			contexts.head
				.map { inputVar =>
					val varValueOpts = contexts.map(ex => ex.find(kv => kv._1 == inputVar._1))
					(inputVar._1, if (varValueOpts.exists(_.isEmpty)) Types.Unknown else Utils.getTypeOfAll(varValueOpts.flatten.map(_._2)))
				}
				// TODO Handle empty sets
				.filter(!_._2.equals(Types.Unknown))
				.toList
		val vocab: VocabFactory = VocabFactory(parameters, additionalLiterals, size)

		val enumerator: SolutionEnumerator = predicate match {
			case pred: MultilineMultivariablePredicate =>
				new InterleavedSolutionEnumerator(pred, size, parameters, additionalLiterals)
			case _ if size && outputVarNames.length == 1 => {
				val varName = outputVarNames.head
				val values = processedEnvs.flatMap(map => map.filter(_._1 == varName).values)
				new ConditionalSolutionEnumerator(parameters, contexts, values, parameters, additionalLiterals)
			}
			case _ if size =>
				val bank = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
				val mini = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
				val enumerator = new enumeration.ProbEnumerator(vocab, oeManager, contexts, false, 0, bank, mini, 100)
				new BasicSolutionEnumerator(predicate, enumerator)
			case _ =>
				val enumerator = new BasicEnumerator(vocab, oeManager, contexts)
				new BasicSolutionEnumerator(predicate, enumerator)
		}

		new SynthesisTask(
			parameters,
			outputVarNames,
			vocab,
			contexts,
			predicate,
			oeManager,
			enumerator)
	}


	def mulitvariablePredicate(
		outputVarNames: List[String],
		inputContexts: List[Context],
		outputEnvs: List[Map[String, Any]]
	): (List[Context], MultilineMultivariablePredicate) = {
		var contexts = inputContexts

		// We can support multiline assignments, so let's build the graph
		// We start with enumerating all the possible environments
		val environments = this.enumerateEnvs(outputVarNames, contexts, outputEnvs)

		// enviornments.head := Same as "contexts", the environment with all old values -> The starting node
		// environment.last := processedEnvs, the environment with all the new values -> The end node

		// We need to add all but the above to the evaluation context, both to preserve completeness under OE, and
		// because the MultilineMultivariablePredicate uses them to determine when Synthesis is complete.
		// We also need to keep track of their indices in the final context, since the context is a flatmap of them,
		// while the predicate needs to be able to extract only the relevant values from synthesized ASTNodes.

		// We can actually just overwrite `context` here, since the original context is represented by the
		// first graph node.

		val nodes = environments
			.map { case (_, envs) =>
				contexts = contexts ++ envs
				(envs, Range(contexts.length - envs.length, contexts.length).toList)
			}
			.map { case (env, indices) => new Node(env, Nil, indices, false) }

		// We never evaluate in the final env, so can we pop that from context.
		contexts = contexts.dropRight(1)

		// Set the final node
		nodes.last.isEnd = true

		// Note: There's a dependency here between the order of nodes, and the order of the `environments`, which
		// we use below to find the nodes that should have a variable in-between.

		// Now we need to create the edges and set them in the nodes
		environments.zipWithIndex.foreach{ case ((thisVars, _), thisIdx) =>
			val nodeEdges: List[Edge] = Range(thisIdx + 1, environments.length).map(thatIdx => {
				val thatVars = environments(thatIdx)._1
				if (thatVars.size > thisVars.size && thisVars.forall(thatVars.contains)) {
					// We need to create an edge between the two
					val newVars = thatVars -- thisVars
					val parent = nodes(thisIdx)
					val child = nodes(thatIdx)

					if (newVars.size == 1) {
						val values = outputEnvs.flatMap(map => map.filter(_._1 == newVars.head).values)
						Some(SingleEdge(None, newVars.head, Utils.getTypeOfAll(values), parent, child))
					} else {
						Some(MultiEdge(
							mutable.Map.from(newVars.map(_ -> None)),
							newVars.map(varName => varName -> Utils.getTypeOfAll(outputEnvs.flatMap(map => map.filter(_._1 == varName).values))).toMap,
							parent,
							child))
					}
				} else {
					// There is no edge between these nodes
					None
				}
			})
				.filter(_.isDefined)
				.map(_.get)
				.toList

			nodes(thisIdx).edges = nodeEdges
		}

		(contexts, new MultilineMultivariablePredicate(nodes.head))
	}

	private def cleanupInputs(input: Map[String, Any]): Map[String, Any] =
	{
		val parser = new InputParser
		input
			.filter(v => !reserved_names.contains(v._1))
			// TODO Is there a cleaner way to do this?
			.filter(_._2.isInstanceOf[String])
			.map(variable => parser.parse(variable._2.asInstanceOf[String]) match {
				case None =>
					DebugPrints.eprintln(s"Input not recognized: $variable")
					(variable._1, null)
				case Some(v) =>
					(variable._1, v)
			})
			.filter(v => v._2 != null)
	}

	private def enumerateEnvs(
		outputVariables: List[String],
		startingEnvs: List[Map[String, Any]],
		endingEnvs: List[Map[String, Any]]): List[(Set[String], List[Map[String, Any]])] = {
		outputVariables match {
			case last :: Nil => List(
				(Set(), startingEnvs),
				(Set(last), startingEnvs.zip(endingEnvs).map(envs => envs._1 + (last -> envs._2(last)))))
			case first :: rest =>
				enumerateEnvs(rest, startingEnvs, endingEnvs) ++
				enumerateEnvs(
					rest,
					startingEnvs.zip(endingEnvs).map(envs => envs._1 + (first -> envs._2(first))),
					endingEnvs)
					.map(tup => (tup._1 + first, tup._2))
		}
	}

	private def getStringLiterals(
		startingContexts: List[Map[String, Any]],
		outputNames: List[String]): Set[String] =
	{
		outputNames
			.filter(name => startingContexts.map(_(name)).exists(Types.typeof(_) == Types.String))
			.flatMap(outputName =>
			{
				startingContexts.flatMap {
						ex =>
							val stringInputs = ex
								.filter { case (name, value) => !outputNames.contains(name) && Types.typeof(value) == Types.String }
								.map(_._2.asInstanceOf[String])
							ex(outputName)
								.asInstanceOf[String]
								.filter(char => stringInputs.forall(inputVal => !inputVal.contains(char.toLower) && !inputVal.contains(char.toUpper)))
								.map(_.toString)
								.toSet
					}
			})
		.toSet
	}
}