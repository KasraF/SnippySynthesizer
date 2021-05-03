package edu.ucsd.snippy.utils

import edu.ucsd.snippy.SynthesisTask
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.ast.{ASTNode, IntLiteral, ListLiteral, StringLiteral, Types}

object Utils
{
	@inline
	def createUniqueName(name: String, context: List[SynthesisTask.Context]): String =
	{
		val existingNames = context.flatMap(_.keys).toSet
		var uniqueName = name
		while (existingNames.contains(uniqueName)) uniqueName = '_' + uniqueName
		uniqueName
	}

	def synthesizeLiteralOption(typ: Types, vals: List[Any]): Option[ASTNode] =
		if (vals.length < 1)
			None
		else
			vals.map(Some(_))
				.reduce[Option[Any]] {
					case (a, b) if a == b => a
					case _ => None
				} match {
					case Some("") => Some(StringLiteral("", vals.length))
					case Some(" ") => Some(StringLiteral(" ", vals.length))
					case Some(-1) => Some(IntLiteral(-1, vals.length))
					case Some(0) => Some(IntLiteral(0, vals.length))
					case Some(1) => Some(IntLiteral(1, vals.length))
					case Some(List()) if typ == Types.IntList =>
						Some(ListLiteral[Int](typ, List(), vals.length))
					case Some(List()) if typ == Types.StringList =>
						Some(ListLiteral[String](typ, List(), vals.length))
					case Some(List()) if typ == Types.BoolList =>
						Some(ListLiteral[Boolean](typ, List(), vals.length))
					case _ => None
				}

	def getTypeOfAll(values: List[Any]): Types =
	{
		val (empty, nonempty) = values.partition(v => v.isInstanceOf[Iterable[_]] && v.asInstanceOf[Iterable[_]].isEmpty)
		val neType = if (nonempty.isEmpty) Types.Unknown else nonempty.map(v => Types.typeof(v)).reduce((acc, t) => if (acc == t) t else Types.Unknown)
		if (empty.nonEmpty) {
			if (nonempty.isEmpty) {
				val defaultTypes: Set[Types] = empty.map {
					case _: List[_] => Types.StringList
					case _: Map[_, _] => Types.StringIntMap
				}.toSet
				return if (defaultTypes.size == 1) defaultTypes.head else Types.Unknown
			}
			else {
				for (v <- empty) {
					if (neType match {
						case Types.StringList | Types.IntList => !v.isInstanceOf[List[_]]
						case Types.Map(_, _) => !v.isInstanceOf[Map[_, _]]
						case _ => false //nonempties are not a list/map, fail.
					}) {
						return Types.Unknown
					}
				}
			}
			neType
		}
		else {
			neType
		}
	}

	def getBinaryPartitions[T](lst: List[T], startingIdx: Int = 0): List[(Set[Int], Set[Int])] = lst match {
		case Nil => List((Set(), Set()))
		case _ :: Nil => List((Set(startingIdx), Set()))
		case _ :: tail =>
			val parts: List[(Set[Int], Set[Int])] = getBinaryPartitions(tail, startingIdx + 1)
			parts.map(p => (p._1 + startingIdx, p._2)) ++ parts.map(p => (p._1, p._2 + startingIdx))
	}

	@inline def filterByIndices[T](lst: List[T], indices: Set[Int]): List[T] =
		lst.zipWithIndex.filter(tup => indices.contains(tup._2)).map(_._1)

	@inline def trueForIndices(lst: List[Boolean], indices: Set[Int]): Boolean =
		lst.zipWithIndex.filter { case (_, i) => indices.contains(i) }.forall(_._1)
	@inline def falseForIndices(lst: List[Boolean], indices: Set[Int]): Boolean =
		lst.zipWithIndex.filter { case (_, i) => indices.contains(i) }.forall(!_._1)

	implicit object FuzzyDoubleEq extends spire.algebra.Eq[Double] {
		def eqv(a:Double, b:Double): Boolean = (a-b).abs < 1e-12
	}
	import spire.syntax.all._
	import spire.std.seq._
	@inline def programConnects(tup: (Option[Any], Any)): Boolean = {
		tup._1 match {
			case None => false
			case Some(a:Double) => a === tup._2.asInstanceOf[Double]
			case Some(x: scala.List[_]) if x.nonEmpty => x.head match { //type erasure is bad and gross
				case _: Double =>
					//x.length == tup._2.length && x.asInstanceOf[List[Double]].zip(b.asInstanceOf[List[Double]]).forall(t => t._1 === t._2)
					x.asInstanceOf[List[Double]] === tup._2.asInstanceOf[List[Double]]
				case _ => tup._1.get == tup._2
			}
			case _ => tup._1.isDefined && tup._1.get == tup._2
		}
	}
}
