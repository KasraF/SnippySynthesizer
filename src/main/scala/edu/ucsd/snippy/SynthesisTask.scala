package edu.ucsd.snippy

import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, OEValuesManager}
import edu.ucsd.snippy.utils.{MultivariablePredicate, Predicate, Utils}
import edu.ucsd.snippy.vocab._
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonParser

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
	processedEnvs: List[Map[String, Any]])
{
	val predicate: Predicate = outputVariables match {
		case single :: Nil => Predicate.getPredicate(single, processedEnvs, this)
		case multiple => new MultivariablePredicate(multiple.map(varName => varName -> Predicate.getPredicate(varName, processedEnvs, this)).toMap)
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

	def fromString(jsonString: String): SynthesisTask =
	{
		val input = JsonParser.parse(jsonString).asInstanceOf[JObject].values
		val outputVarNames: List[String] = input("varNames").asInstanceOf[List[String]]
		val envs: List[Map[String, Any]] = input("envs").asInstanceOf[List[Map[String, Any]]]
		val previousEnv: Map[String, Any] =
			cleanupInputs(input("previous_env").asInstanceOf[Map[String, Any]])
		val processedEnvs: List[Map[String, Any]] = envs
			.asInstanceOf[List[Map[String, Any]]]
			.map(cleanupInputs)

		val contexts: List[Context] = if (isLoopy(previousEnv, outputVarNames, envs)) {
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

		val parameters =
			contexts.head
				.map { inputVar =>
					val varValueOpts = contexts.map(ex => ex.find(kv => kv._1 == inputVar._1))
					(inputVar._1, if (varValueOpts.exists(_.isEmpty)) Types.Unknown else Utils.getTypeOfAll(varValueOpts.flatten.map(_._2)))
				}
				// TODO Handle empty sets
				.filter(!_._2.equals(Types.Unknown))
				.toList
		val additionalLiterals = getStringLiterals(processedEnvs, outputVarNames)
		val vocab = SynthesisTask.vocabFactory(parameters, additionalLiterals)
		val oeManager = new InputsValuesManager
		val enumerator = new Enumerator(vocab, oeManager, contexts)

		new SynthesisTask(
			parameters,
			outputVarNames,
			vocab,
			contexts,
			oeManager,
			enumerator,
			processedEnvs)
	}

	private def getStringLiterals(examples: List[Map[String, Any]], outputNames: List[String]): List[String] =
	{
		if (outputNames
			.flatMap(outputName => examples.map(entry => entry(outputName)))
			.exists(value => Types.typeof(value) != Types.String)) //this is only for strings
		{
			return Nil
		}

		val opts =
			examples.map {
				ex =>
					val outputValues = ex.filter(entry => outputNames.contains(entry._1)).values.asInstanceOf[Iterable[String]]
					val stringInputs = for ((inputName, inputVal) <- ex; if Types.typeof(inputVal) == Types.String && !outputNames.contains(inputName))
						yield inputVal.asInstanceOf[String]
					val chars: Iterable[String] = outputValues.flatMap(
						outputVal =>
							for (char <- outputVal; if stringInputs.forall(inputVal => !inputVal.contains(char.toLower) && !inputVal.contains(char.toUpper)))
								yield char.toString
						)
					chars.toSet
			}
		val intersection = opts.reduce((a, b) => a.intersect(b))
		intersection.toList
	}

	private def vocabFactory(variables: List[(String, Types.Value)], additionalLiterals: List[String]): VocabFactory =
	{
		val defaultStringLiterals = List(" ")
		val stringLiterals = (defaultStringLiterals ++ additionalLiterals).distinct

		val vocab: List[VocabMaker] =
			stringLiterals.map { str =>
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.String

					override def apply(children    : List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringLiteral(str, contexts.length)
				}
			} ++ List(
				// Literals
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new IntLiteral(0, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new IntLiteral(1, contexts.length)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 0
					override val childTypes: List[Types] = Nil
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new IntLiteral(-1, contexts.length)
				},
				// Binary Ops
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Bool

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new GreaterThan(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Bool

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new LessThanEq(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.String

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringConcat(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.Int)
					override val returnType: Types = Types.String

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new BinarySubstring(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.Int)
					override val returnType: Types = Types.String

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringStep(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new Find(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Bool

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new Contains(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new Count(children.head.asInstanceOf[StringNode], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.AnyIterable)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new Length(children.head.asInstanceOf[IterableNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.IntList)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new Min(children.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.IntList)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new Max(children.head.asInstanceOf[ListNode[Int]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.String

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringLower(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.String)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringToInt(children.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.Int)
					override val returnType: Types = Types.String

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new IntToString(children.head.asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 3
					override val childTypes: List[Types] = List(Types.String, Types.Int, Types.Int)
					override val returnType: Types = Types.String

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new TernarySubstring(
							children.head.asInstanceOf[StringNode],
							children(1).asInstanceOf[IntNode],
							children(2).asInstanceOf[IntNode])
				},
				//			new BasicVocabMaker
				//			{
				//				override val arity: Int = 3
				//				override val childTypes: List[Types] = List(Types.String, Types.String, Types.String)
				//				override val returnType: Types = Types.String
				//
				//				override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
				//					new StringReplace(
				//						children.head.asInstanceOf[StringNode],
				//						children(1).asInstanceOf[StringNode],
				//						children(2).asInstanceOf[StringNode])
				//			},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.String)
					override val returnType: Types = Types.StringList

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringSplit(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.String, Types.StringList)
					override val returnType: Types = Types.String

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new StringJoin(children.head.asInstanceOf[StringNode], children.tail.head.asInstanceOf[ListNode[String]])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 1
					override val childTypes: List[Types] = List(Types.StringList)
					override val returnType: Types = Types.StringList

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new SortedStringList(children.head.asInstanceOf[ListNode[String]])
				},
				new ListCompVocabMaker(Types.String, Types.String)
				{
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new StringToStringListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[StringNode],
							this.varName)
				},
				new ListCompVocabMaker(Types.String, Types.Int)
				{
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new StringToIntListCompNode(
							lst.asInstanceOf[ListNode[String]],
							map.asInstanceOf[IntNode],
							this.varName)
				},
				new ListCompVocabMaker(Types.Int, Types.String)
				{
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new IntToStringListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[StringNode],
							this.varName)
				},
				new ListCompVocabMaker(Types.Int, Types.Int)
				{
					override def makeNode(lst: ASTNode, map: ASTNode): ASTNode =
						new IntToIntListCompNode(
							lst.asInstanceOf[ListNode[Int]],
							map.asInstanceOf[IntNode],
							this.varName)
				},
				new MapCompVocabMaker(Types.String, Types.String)
				{
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						new StringStringMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[StringNode], this.varName)
				},
				new MapCompVocabMaker(Types.String, Types.Int)
				{
					override def makeNode(lst: ASTNode, key: ASTNode, value: ASTNode): ASTNode =
						new StringIntMapCompNode(lst.asInstanceOf[StringNode], key.asInstanceOf[StringNode], value.asInstanceOf[IntNode], this.varName)
				},
				new FilteredMapVocabMaker(Types.String, Types.String)
				{
					override def makeNode(map: ASTNode, filter: BoolNode): ASTNode =
						new StringStringFilteredMapNode(map.asInstanceOf[MapNode[String,String]], filter, this.keyName)
				},
				new FilteredMapVocabMaker(Types.String, Types.Int)
				{
					override def makeNode(map: ASTNode, filter: BoolNode): ASTNode =
						new StringIntFilteredMapNode(map.asInstanceOf[MapNode[String,Int]], filter, this.keyName)
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.StringIntMap, Types.String)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new MapGet(children.head.asInstanceOf[MapNode[String, Int]], children(1).asInstanceOf[StringNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new IntAddition(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new IntSubtraction(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				},
				new BasicVocabMaker
				{
					override val arity: Int = 2
					override val childTypes: List[Types] = List(Types.Int, Types.Int)
					override val returnType: Types = Types.Int

					override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
						new IntDivision(children.head.asInstanceOf[IntNode], children(1).asInstanceOf[IntNode])
				}
				)

		VocabFactory(
			variables.
				map {
					case (name, Types.String) => new BasicVocabMaker
					{
						override val arity: Int = 0
						override val childTypes: List[Types] = Nil
						override val returnType: Types = Types.String

						override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
							new StringVariable(name, contexts)
					}
					case (name, Types.Int) => new BasicVocabMaker
					{
						override val arity: Int = 0
						override val childTypes: List[Types] = Nil
						override val returnType: Types = Types.Int

						override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
							new IntVariable(name, contexts)
					}
					case (name, Types.Bool) => new BasicVocabMaker
					{
						override val arity: Int = 0
						override val childTypes: List[Types] = Nil
						override val returnType: Types = Types.Bool

						override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
							new BoolVariable(name, contexts)
					}
					case (name, Types.List(childType)) => new BasicVocabMaker
					{
						override val arity: Int = 0
						override val childTypes: List[Types] = Nil
						override val returnType: Types = Types.listOf(childType)

						override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
							new ListVariable(name, contexts, childType)
					}
					case (name, Types.Map(keyType, valType)) => new BasicVocabMaker
					{
						override val arity: Int = 0
						override val childTypes: List[Types] = Nil
						override val returnType: Types = Types.mapOf(keyType, valType)

						override def apply(children: List[ASTNode], contexts: List[Map[String, Any]]): ASTNode =
							new MapVariable(name, contexts, keyType, valType)
					}
					case (name, typ) =>
						assert(assertion = false, s"Input type $typ not supported for input $name")
						null
				} ++ vocab
			)
	}
}