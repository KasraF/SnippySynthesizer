package edu.ucsd.snippy

import edu.ucsd.snippy.SynthesisTask.{Context, cleanupInputs, getStringLiterals}
import edu.ucsd.snippy.ast.{ASTNode, Types}
import edu.ucsd.snippy.enumeration.{InputsValuesManager, ProbEnumerator}
import edu.ucsd.snippy.parser.Python3Lexer
import edu.ucsd.snippy.predicates.Predicate
import edu.ucsd.snippy.solution.{BasicSolutionEnumerator, ConditionalSingleEnumMultivarSolutionEnumerator, Node}
import edu.ucsd.snippy.utils.Utils
import edu.ucsd.snippy.vocab.VocabFactory
import net.liftweb.json
import net.liftweb.json.{JObject, JsonParser}
import org.antlr.v4.runtime.CharStreams

import java.io.File
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.io.Source.fromFile
import scala.util.control.Breaks.{break, breakable}

//noinspection DuplicatedCode,SourceNotClosed,SourceNotClosed
object NoCFBenchmarks extends App
{
	val benchmarksDir = args.headOption match {
		case Some(s) => if (s.startsWith("-d=")) new File(s.drop("-d=".length))
						else new File("src/test/resources/no-cf")
		case _ => new File("src/test/resources/no-cf")
	}
	assert(benchmarksDir.isDirectory)

	val oneVar = mutable.ArrayBuffer[File]()
	val multiVar = mutable.ArrayBuffer[File]()
	for (dir <- benchmarksDir.listFiles())
		if (dir.isDirectory) {
			dir.listFiles()
				.filter(_.getName.contains(".examples.json"))
				.filter(!_.getName.contains(".out"))
				.foreach { file =>
					val taskStr = fromFile(file).mkString
					val task = json.parse(taskStr).asInstanceOf[JObject].values
					val vars = task("varNames").asInstanceOf[List[_]].length
					if (vars == 1) {
						oneVar += file
					} else {
						multiVar += file
					}
				}
		}

	val timeout: Int = args.lastOption.map(_.toIntOption) match {
		case Some(Some(t)) => t
		case _ => 7
	}

	//warmup
	for (file <- oneVar)
		doSimpleEnum(file) + "," + regularSolve(file)
	for (file <- multiVar)
		doMultivarEnum(file) + "," + regularSolve(file)

	println(s"Single variable (${oneVar.length})")
	println("--------------------")
	println("name,se_correct,se_time,se_count,loopy_correct,loopy_time,loopy_count")
	for (file <- oneVar)
		println(doSimpleEnum(file) + "," + regularSolve(file))
	println(s"Multivar (${multiVar.length})")
	println("-------------")
	println("name,se_correct,se_time,se_count,loopy_correct,loopy_time,loopy_count")
	for (file <- multiVar)
		println(doMultivarEnum(file) + "," + regularSolve(file))

	def regularSolve(file: File): String =
	{
		val taskStr = fromFile(file).mkString
		val task = json.parse(taskStr).asInstanceOf[JObject].values
		Snippy.synthesize(taskStr, timeout) match {
			case (None, time: Int, count: Int) =>
				f"t,${time / 1000.0}%.3f,$count%d"
			case (Some(program: String), time: Int, count: Int) =>
				val correct = task.get("solutions") match {
					case Some(solutions) if solutions.asInstanceOf[List[String]].contains(program) => '+'
					case Some(_) =>
						'-'
					case None =>
						'?'
				}
				f"$correct,${time / 1000.0}%.3f,$count%d"
		}
	}

	def doSimpleEnum(file: File): String =
	{
		val taskStr = fromFile(file).mkString
		val input = JsonParser.parse(taskStr).asInstanceOf[JObject].values
		val outputVarNames: List[String] = input("varNames").asInstanceOf[List[String]]
		assert(outputVarNames.length == 1)
		val envs: List[Map[String, Any]] = input("envs").asInstanceOf[List[Map[String, Any]]]
		val previousEnvMap: Map[Int, Map[String, Any]] = input("previousEnvs").asInstanceOf[Map[String, Map[String, Any]]].map(tup => tup._1.toInt -> tup._2)
		val allEnvs: List[(Option[Map[String, Any]], Map[String, Any])] = envs.map(env => {
			val time = env("time").asInstanceOf[BigInt].toInt
			if (previousEnvMap.contains(time)) {
				Some(previousEnvMap(time)) -> env
			} else {
				// We need to use the time + iter to see if we can find the previous env in the
				// other envs
				val iterStr = env("#").asInstanceOf[String]
				if (iterStr.isEmpty) {
					// Not a loop, and no prev env, so no luck :(
					None -> env
				} else {
					// Find the nearest entry with the iter one less than this one
					val iter = iterStr.toInt
					val prevEnv = envs
						.filter(env => env("time").asInstanceOf[BigInt].toInt < time)
						.filter(env => env.contains("#") && env("#").asInstanceOf[String].toInt == iter - 1)
						.lastOption
					prevEnv -> env
				}
			}
		})
			.map(tup => tup._1.map(cleanupInputs) -> cleanupInputs(tup._2))
		val justEnvs = allEnvs.map(_._2)

		val contexts: List[Context] = allEnvs.map {
			case (Some(prevEnv), env) =>
				env.filter(entry => !outputVarNames.contains(entry._1)) ++
					outputVarNames.filter(prevEnv.contains).collect(varName => varName -> prevEnv(varName)).toMap
			case (None, env) => env.filter(entry => !outputVarNames.contains(entry._1))
		}
		val oeManager = new InputsValuesManager
		val predicate = Predicate.getPredicate(outputVarNames.head, justEnvs, oeManager)
		val additionalLiterals = getStringLiterals(justEnvs, outputVarNames)
		val parameters = contexts.flatMap(_.keys)
			.toSet[String]
			.map(varName => varName -> Utils.getTypeOfAll(contexts.map(ex => ex.get(varName)).filter(_.isDefined).map(_.get)))
			.filter(!_._2.equals(Types.Unknown))
			.toList
		val vocab: VocabFactory = VocabFactory(parameters, additionalLiterals)
		val enumerator = //new BasicEnumerator(vocab, oeManager, contexts)
			new ProbEnumerator(vocab, oeManager, contexts, false, 0, mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
				mutable.Map[Int, mutable.ArrayBuffer[ASTNode]](),
				100)
		val enum = new BasicSolutionEnumerator(predicate, enumerator)
		val sTask = new SynthesisTask(
			parameters,
			outputVarNames,
			vocab,
			contexts,
			predicate,
			oeManager,
			enum)
		val name = file.getParentFile.getName + "/" + file.getName
		Snippy.synthesize(sTask, timeout) match {
			case (None, time: Int, count: Int) =>
				f"$name%22s,t,${time / 1000.0}%.3f,$count%d"
			case (Some(program: String), time: Int, count: Int) =>
				val correct = input.get("solutions") match {
					case Some(solutions) if solutions.asInstanceOf[List[String]].contains(program) => '+'
					case Some(_) =>
						'-'
					case None =>
						'?'
				}
				f"$name%22s,$correct,${time / 1000.0}%.3f,$count%d"
		}
	}

	def doMultivarEnum(file: File): String =
	{
		val taskStr = fromFile(file).mkString
		val name = file.getParentFile.getName + "/" + file.getName
		val input = JsonParser.parse(taskStr).asInstanceOf[JObject].values
		val outputVarNames: List[String] = input("varNames").asInstanceOf[List[String]]
		val orderedVars = input("solutions").asInstanceOf[List[String]].head.linesIterator.map { line =>
			val lex = new Python3Lexer(CharStreams.fromString(line));
			val token = lex.nextToken()
			assert(token.getType == Python3Lexer.NAME)
			token.getText
		}.toList
		assert(orderedVars.toSet == outputVarNames.toSet, file.getPath)
		val origSynthesisTask = SynthesisTask.fromString(taskStr)
		val graph: Node = origSynthesisTask.enumerator.asInstanceOf[ConditionalSingleEnumMultivarSolutionEnumerator].graph
		val pathNodes = (0 to orderedVars.length).map { i =>
			val prefix = orderedVars.take(i)
			val node = prefix.foldLeft(graph) { case (n: Node, v: String) =>
				n.edges.find(e =>
					e.variables.size == 1 && e.variables.head._1.name == v)
					.get.child
			}
			node
		}.toList
		val solutionEnumerators = pathNodes.sliding(2).zip(orderedVars).map { case (n1 :: n2 :: Nil, v) =>
			val pred = Predicate.getPredicate(v, n2.state, n1.enum.oeManager)
			new BasicSolutionEnumerator(pred, n1.enum)
		}
		val deadline = timeout.seconds.fromNow
		val rs = mutable.ArrayBuffer[(Option[String], Int)]()
		for (se <- solutionEnumerators) {
			breakable {
				if (!deadline.hasTimeLeft) {
					rs.append((None, 0))
					break
				}
				for (solution <- se) {
					solution match {
						case Some(assignment) =>
							rs.append((Some(assignment.code), se.programsSeen))
							break
						case _ => ()
					}

					if (!deadline.hasTimeLeft) {
						rs.append((None, se.programsSeen))
						break
					}
				}
			}
		}
		val time = timeout * 1000 - deadline.timeLeft.toMillis.toInt
		val isTimeout = rs.exists(r => r._1.isEmpty)
		val count = rs.map(_._2).sum


		if (isTimeout) {
			f"$name%22s,t,${time / 1000.0}%.3f,$count%d"
		} else {
			val program = rs.map(_._1.get).mkString("\n")
			val correct = input.get("solutions") match {
				case Some(solutions) if solutions.asInstanceOf[List[String]].contains(program) => '+'
				case Some(_) =>
					'-'
				case None =>
					'?'
			}
			f"$name%22s,$correct,${time / 1000.0}%.3f,$count%d"
		}
	}
}
