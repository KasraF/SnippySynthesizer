package edu.ucsd.snippy

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration.{BasicEnumerator, Enumerator, InputsValuesManager, OEValuesManager}
import edu.ucsd.snippy.utils.{BasicMultivariablePredicate, Edge, MultiEdge, MultilineMultivariablePredicate, Node, Predicate, SingleEdge, Utils}
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

	// Synthesizer state
	val oeManager : OEValuesManager,
	val enumerator: Enumerator,

	// Extra information for building the predicate
	pred: Option[Predicate],
	processedEnvs: List[Map[String, Any]])
{
	val predicate: Predicate = (pred, outputVariables) match {
		case (Some(pred), _) => pred
		case (None, single :: Nil) => Predicate.getPredicate(single, processedEnvs, this)
		case (None, multiple) => new BasicMultivariablePredicate(multiple.map(varName => varName -> Predicate.getPredicate(varName, processedEnvs, this)).toMap)
	}

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

		val additionalLiterals = getStringLiterals(processedEnvs, outputVarNames)

		val predicate = if (loopy && outputVarNames.size > 1) {
			// We can support multiline assignments, so let's build the graph
			// We start with enumerating all the possible environments
			val environments = this.enumerateEnvs(outputVarNames, contexts, processedEnvs)

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
							val values = processedEnvs.flatMap(map => map.filter(_._1 == newVars.head).values)
							Some(SingleEdge(None, newVars.head, Utils.getTypeOfAll(values), parent, child))
						} else {
							Some(MultiEdge(
								mutable.Map.from(newVars.map(_ -> None)),
								newVars.map(varName => varName -> Utils.getTypeOfAll(processedEnvs.flatMap(map => map.filter(_._1 == varName).values))).toMap,
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

			Some(new MultilineMultivariablePredicate(nodes.head))
		} else {
			None
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
		val vocab = SynthesisTask.vocabFactory(parameters, additionalLiterals.toList, size)
		val oeManager = new InputsValuesManager

		val enumerator = if (!size) {
			new BasicEnumerator(vocab, oeManager, contexts)
		} else {
			val bank = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
			val mini = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
			new enumeration.ProbEnumerator(
				vocab,
				oeManager,
				contexts,
				false,
				0,
				bank,
				mini)
		}

		new SynthesisTask(
			parameters,
			outputVarNames,
			vocab,
			contexts,
			oeManager,
			enumerator,
			predicate,
			processedEnvs)
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

	private def vocabFactory(variables: List[(String, Types.Value)], additionalLiterals: List[String], size: Boolean): VocabFactory =
	{
		val defaultStringLiterals = List(" ")
		val stringLiterals = (defaultStringLiterals ++ additionalLiterals).distinct

		val vocab: List[VocabMaker] =
			stringLiterals.map{str =>
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringLiteral]
					override val head: String = ""

					override def apply(children : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringLiteral(str, contexts.length)

				}
			} ++ List(
				// Literals
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(0, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(1, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(-1, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntLiteral]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntLiteral(3, contexts.length)
				},        // Binary Ops
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[GreaterThan]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						GreaterThan(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[LessThanEq]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						LessThanEq(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringConcat]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringConcat(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[BinarySubstring]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						BinarySubstring(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStep]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringStep(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Find]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Find(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[Contains]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Contains(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Count]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Count(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[StartsWith]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StartsWith(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[EndsWith]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						EndsWith(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.Iterable(Types.Any))
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Length]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Length(children.head.asInstanceOf[IterableNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.IntList)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Min]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Min(children.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.IntList)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[Max]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						Max(children.head.asInstanceOf[ListNode[Int]])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[IsAlpha]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IsAlpha(children.head.asInstanceOf[StringNode])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.Bool
					override val nodeType: Class[_ <: ASTNode] = classOf[IsNumeric]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IsNumeric(children.head.asInstanceOf[StringNode])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringLower]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringLower(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringUpper]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringUpper(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToInt]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringToInt(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToString]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntToString(children.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.String, Types.Int, Types.Int)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[TernarySubstring]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						TernarySubstring(
							children.head.asInstanceOf[StringNode],
							children(1).asInstanceOf[IntNode],
							children(2).asInstanceOf[IntNode])
				},

				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[StringSplit]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringSplit(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.StringList)
					override val returnType: Types = Types.String
					override val nodeType: Class[_ <: ASTNode] = classOf[StringJoin]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						StringJoin(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.StringList)
					override val returnType: Types = Types.StringList
					override val nodeType: Class[_ <: ASTNode] = classOf[SortedStringList]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						SortedStringList(children.head.asInstanceOf[ListNode[String]])
				},
				new ListCompVocabMaker(Types.String, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToStringListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new StringToStringListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[StringNode],
							this.varName)

					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.String)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.String, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringToIntListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new StringToIntListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[IntNode],
							this.varName)

					override val returnType: Types = Types.IntList
					override val childTypes: List[Types] = List(Types.String)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.Int, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToStringListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new IntToStringListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[StringNode],
							this.varName)

					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.Int)
					override val head: String = ""
				},
				new ListCompVocabMaker(Types.Int, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[IntToIntListCompNode]
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new IntToIntListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[IntNode],
							this.varName)

					override val returnType: Types = Types.IntList
					override val childTypes: List[Types] = List(Types.Int)
					override val head: String = ""
				},
				new MapCompVocabMaker(Types.String, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStringMapCompNode]
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						new StringStringMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[StringNode], this.varName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new MapCompVocabMaker(Types.String, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringIntMapCompNode]
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						new StringIntMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[IntNode], this.varName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new FilteredMapVocabMaker(Types.String, Types.String, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringStringFilteredMapNode]
					override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
						new StringStringFilteredMapNode(map.asInstanceOf[StringStringMapNode], filter, this.keyName)

					override val returnType: Types = Types.Unknown
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new FilteredMapVocabMaker(Types.String, Types.Int, size) {
					override val nodeType: Class[_ <: ASTNode] = classOf[StringIntFilteredMapNode]
					override def makeNode(map: ASTNode, filter: BoolNode) : ASTNode =
						new StringIntFilteredMapNode(map.asInstanceOf[MapNode[String,Int]], filter, this.keyName)
					override val returnType: Types = Types.StringList
					override val childTypes: List[Types] = List(Types.Unknown)
					override val head: String = ""
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Map(Types.String, Types.Int), Types.String)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[MapGet]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						MapGet(children.head.asInstanceOf[MapNode[String,Int]], children(1).asInstanceOf[StringNode])

				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntAddition]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntAddition(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntMultiply]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntMultiply(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntSubtraction]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntSubtraction(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int
					override val nodeType: Class[_ <: ASTNode] = classOf[IntDivision]
					override val head: String = ""

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						IntDivision(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				}
				)

		VocabFactory(variables.map {
			case (name, Types.String) => new BasicVocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.String
				override val nodeType: Class[_ <: ASTNode] = classOf[StringVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					StringVariable(name, contexts)
			}
			case (name, Types.Int) => new BasicVocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Int
				override val nodeType: Class[_ <: ASTNode] = classOf[IntVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					IntVariable(name, contexts)
			}
			case (name, Types.Bool) => new BasicVocabMaker
			{
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Bool
				override val nodeType: Class[_ <: ASTNode] = classOf[BoolVariable]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					BoolVariable(name, contexts)
			}
			case (name, Types.List(childType)) => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.List(childType)
				override val nodeType: Class[_ <: ASTNode] = classOf[ListVariable[Any]]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					ListVariable(name, contexts, childType)
			}
			case (name, Types.Map(keyType, valType)) => new BasicVocabMaker {
				override val arity: Int = 0
				override val childTypes: List[Types] = Nil
				override val returnType: Types = Types.Map(keyType, valType)
				override val nodeType: Class[_ <: ASTNode] = classOf[MapVariable[Any,Any]]
				override val head: String = ""

				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
					MapVariable(name, contexts, keyType, valType)
			}
			case (name, typ) =>
				assert(assertion = false, s"Input type $typ not supported for input $name")
				null
		} ++ vocab)
	}
}