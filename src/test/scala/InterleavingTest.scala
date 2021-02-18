//import edu.ucsd.snippy.ast.ASTNode
//import edu.ucsd.snippy.enumeration.{Enumerator, InputsValuesManager, ProbEnumerator}
//import edu.ucsd.snippy.predicates.{MultiEdge, MultilineMultivariablePredicate, Node, SingleEdge}
//import edu.ucsd.snippy.{Snippy, SynthesisTask}
//
//import scala.collection.mutable
//import scala.concurrent.duration._
//import scala.tools.nsc.io.JFile
//
//object InterleavingTest extends App {
//	def runTest(jsonString: String): Unit = {
//		val task = SynthesisTask.fromString(jsonString)
//
//		if (!task.predicate.isInstanceOf[MultilineMultivariablePredicate]) {
//			println("Task was not an instance of multiline multivariable synthesis.")
//			return
//		}
//
//		val predicate = task.predicate.asInstanceOf[MultilineMultivariablePredicate]
//
//		def collectNodes(graphStart: Node): List[Node] =
//			if (graphStart.isEnd) {
//				Nil
//			} else {
//				graphStart +: graphStart.edges.flatMap(edge => collectNodes(edge.child)).distinct
//			}
//
//		val nodes = collectNodes(predicate.graphStart)
//		val enumerators = nodes.map{n =>
//			// ridiculously dirty, here we go:
//			val newtask = SynthesisTask.fromString(jsonString)
//			val bank = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
//			val mini = mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]()
//			val enumerator = new ProbEnumerator(
//				newtask.vocab,
//				new InputsValuesManager,
//				n.state,
//				false,
//				0,
//				bank,
//				mini)
//			(n, enumerator)
//		}
//
//		def nextOnNode(node: Node, enumerator: Enumerator): Boolean = {
//			val program = enumerator.next()
//			if (!program.usesVariables) return false
//
//			val values: List[Any] = program.values
//
//			var graphChanged = false
//			for (edge <- node.edges) {
//				edge match {
//					case edge: SingleEdge =>
//						if (edge.program.isEmpty &&
//							edge.child.state
//								.map(_ (edge.variable))
//								.zip(values)
//								.forall(tup => tup._1 == tup._2)) {
//							edge.program = Some(program)
//							graphChanged = true
//						}
//					case edge: MultiEdge =>
//						// We need to check each variable
//						for ((variable, programOpt) <- edge.programs) {
//							if (programOpt.isEmpty &&
//								edge.child.state
//									.map(_ (variable))
//									.zip(values)
//									.forall(tup => tup._1 == tup._2)) {
//								edge.programs.update(variable, Some(program))
//								graphChanged = true
//							}
//						}
//				}
//
//			}
//			graphChanged
//		}
//
//		def interleaveEnumerators(enumerators: List[(Node, Enumerator)]): Option[List[String]] = {
//			for ((node,enumerator) <- enumerators) {
//				if (enumerator.hasNext) {
//					//TODO: count repetitions or something
//					if (nextOnNode(node,enumerator)) {
//						//look for a path
//						predicate.traverse(predicate.graphStart) match {
//							case None => None
//							case Some(lst) => return Some(lst) //found program
//						}
//					}
//				}
//			}
//			None
//		}
//
//		val timeout = 7
//		println("Per node:")
//		var res: Option[List[String]] = None
//		val deadline = timeout.seconds.fromNow
//		do {
//			res = interleaveEnumerators(enumerators)
//		} while (res.isEmpty && deadline.hasTimeLeft())
//		val time = (timeout * 1000.0 - deadline.timeLeft.toMillis) / 1000.0
//
//		res match {
//			case Some(rs) => println("| " + rs.mkString("\n| "))
//			case None => println("| None")
//		}
//		printf("Time: %.2f\n", time)
//		print("Programs seen: ")
//		val sizes = enumerators.map(enumerator => enumerator._2.oeManager.asInstanceOf[InputsValuesManager].classValues.size)
//		println(sizes.mkString(" + ")+ " = " + sizes.sum)
//		println("----")
//		println("All at once:")
//		val task2 = SynthesisTask.fromString(jsonString, true)
//		val r2 = Snippy.synthesize(task2, timeout)
//		println("| " + r2._1.getOrElse("None").replace("\n", "\n| "))
//		printf("Time: %.2f\n", r2._2 / 1000.0)
//		printf("Programs seen: %s\n", task2.enumerator.oeManager.asInstanceOf[InputsValuesManager].classValues.size)
//
//		System.gc()
//	}
//
//	if (args.nonEmpty) {
//		this.runTest(scala.io.Source.fromFile(args(0)).mkString)
//	} else {
//		val benchmarks = new JFile("src/test/resources/multivariable_benchmarks")
//		benchmarks.listFiles()
//			.filter(_.isFile)
//			.map(_.getAbsolutePath)
//			.filter(_.endsWith(".json"))
//			.sorted(Ordering.String)
//			.foreach(path => {
//				val bar = "-".repeat(path.size)
//				println()
//				println(path)
//				println(bar)
//				this.runTest(scala.io.Source.fromFile(path).mkString)
//				println(bar)
//			})
//	}
//}
