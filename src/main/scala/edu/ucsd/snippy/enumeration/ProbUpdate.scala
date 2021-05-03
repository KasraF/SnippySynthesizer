package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.vocab.VocabFactory

import scala.collection.mutable

object ProbUpdate
{
	var fitCost: mutable.Map[Set[Any], Double] = mutable.Map[Set[Any], Double]()
	var fitProgs: mutable.ArrayBuffer[String] = mutable.ArrayBuffer()
	var fitMap: mutable.Map[(Class[_], Option[Any]), Double] = mutable.Map[(Class[_], Option[Any]), Double]()
	var probMap: mutable.Map[(Class[_], Option[Any]), Double] = mutable.Map[(Class[_], Option[Any]), Double]()
	var priors: mutable.Map[(Class[_], Option[Any]), Int] = mutable.Map[(Class[_], Option[Any]), Int]()
	val lnOf2: Double = scala.math.log(2) // natural log of 2
	def log2(x: Double): Double = scala.math.log(x) / lnOf2

	def expo(i: Double, exp: Double): Double = scala.math.pow(i, exp)

	def getAllNodeTypes(program: ASTNode): Set[(Class[_], Option[Any])] =
	{
		val recurseValue =
			if (program.isInstanceOf[StringLiteral] ||
				program.isInstanceOf[IntLiteral] ||
				program.isInstanceOf[StringVariable] ||
				program.isInstanceOf[IntVariable] ||
				program.isInstanceOf[StringLiteral] ||
				program.isInstanceOf[BoolLiteral] ||
				program.isInstanceOf[BoolVariable]) {
			program.getClass -> Some(program.code)
		} else {
			program.getClass -> None
		}
		program.children.flatMap(c => getAllNodeTypes(c)).toSet + recurseValue
	}

//	def update(fitsMap: mutable.Map[(Class[_], Option[Any]), Double], currLevelProgs: mutable.ArrayBuffer[ASTNode], task: SygusFileTask): mutable.Map[(Class[_], Option[Any]), Double] =
//	{
//		fitMap = fitsMap
//		for (program <- currLevelProgs) {
//			val exampleFit = task.fit(program)
//			val fit: Double = (exampleFit._1.toFloat) / exampleFit._2
//			if (fit > 0) {
//				val examplesPassed = task.fitExs(program)
//				if (!fitCost.contains(examplesPassed) || ((fitCost(examplesPassed) >= program.cost) && !fitProgs.contains(program.code))) {
//					fitCost += (examplesPassed -> program.cost)
//					fitProgs += program.code
//					val changed: Set[(Class[_], Option[Any])] = getAllNodeTypes(program)
//					for (changedNode <- changed) {
//						if (!fitMap.contains(changedNode) || fitMap(changedNode) > (1 - fit)) {
//							val update = expo(probMap(changedNode), (1 - fit))
//							fitMap += (changedNode -> update)
//							probMap += (changedNode -> update)
//						}
//					}
//					//println(program.code, examplesPassed, program.cost)
//				}
//			}
//		}
//		fitMap
//	}

	def updatePriors(probMap: mutable.Map[(Class[_], Option[Any]), Double]): Unit =
	{
		updateProb()
		probMap.foreach(d => priors += (d._1 -> roundValue(-log2(d._2))))
	}

	def updateProb(): Unit =
	{
		val probList = probMap.values
		probMap.foreach(c => probMap += (c._1 -> (c._2.toFloat / probList.sum)))
	}

	def getRootPrior(node: ASTNode): Int =
		if (node.isInstanceOf[StringLiteral] ||
			node.isInstanceOf[StringVariable] ||
			node.isInstanceOf[IntVariable] ||
			node.isInstanceOf[IntLiteral] ||
			node.isInstanceOf[BoolVariable] ||
			node.isInstanceOf[BoolLiteral]) {
			// TODO This is a workaround. Needs to be fixed properly
			// priors((node.getClass, Some(node.code)))
			1
	} else {
		// priors((node.getClass, None))
		1
	}

	def roundValue(num: Double): Int = if (num < 1) 1 else if (num - num.toInt > 0.5) math.ceil(num).toInt else math.floor(num).toInt

	def resetPrior(): mutable.Map[(Class[_], Option[Any]), Int] =
	{
		priors.foreach(c => c._1 -> roundValue(-log2(probMap((c._1)))))
		priors
	}

	def createPrior(vocab: VocabFactory): mutable.Map[(Class[_], Option[Any]), Int] =
	{
		vocab.leavesMakers.foreach(l => priors += ((l.nodeType, None) -> 1))
		vocab.nonLeaves().foreach(l => priors += ((l.nodeType, None) -> 1))
		//vocab.leavesMakers.foreach(l => priors += ((l.nodeType, None) -> roundValue(-log2(probMap((l.nodeType, None))))))
		//vocab.nonLeaves().foreach(l => priors += ((l.nodeType, None) -> roundValue(-log2(probMap((l.nodeType, None))))))
		priors
	}

	def createProbMap(vocab: VocabFactory): mutable.Map[(Class[_], Option[Any]), Double] =
	{
		val uniform = 1.0 / (vocab.leavesMakers.length + vocab.nonLeaves().length)
		vocab.leavesMakers.foreach(l => probMap += ((l.nodeType, None) -> uniform))
		vocab.nonLeaves().foreach(l => probMap += ((l.nodeType, None) -> uniform))
		probMap
	}
}
