package edu.ucsd.snippy.ast

import edu.ucsd.snippy.DebugPrints.eprintln

object Types extends Enumeration
{
	type Types = Value

	case class Iterable(childType: Types) extends super.Val
	{
		override def canEqual(that: Any): Boolean = this.equals(that)

		override def equals(that: Any): Boolean =
			that match {
				case String => this.childType.equals(String)
				case List(t) => this.childType.equals(t)
				case Set(t) => this.childType.equals(t)
				case Iterable(t) => this.childType.equals(t)
				case Map(t, _) => this.childType.equals(t)
				case _ => false
			}

		override def toString(): String = s"Iterable[$childType]"
	}

	case class List(childType: Types) extends super.Val
	{
		override def canEqual(that: Any): Boolean = this.equals(that)

		override def equals(that: Any): Boolean =
			that.isInstanceOf[Types.List] && that.asInstanceOf[Types.List].childType.equals(this.childType)

		override def toString(): String = s"List[$childType]"
	}

	case class Set(childType: Types) extends super.Val
	{
		override def canEqual(that: Any): Boolean = this.equals(that)

		override def equals(that: Any): Boolean =
			that.isInstanceOf[Types.Set] && that.asInstanceOf[Types.Set].childType.equals(this.childType)

		override def toString(): String = s"Set[$childType]"
	}

	case class Map(keyType: Types, valType: Types) extends super.Val
	{
		override def canEqual(that: Any): Boolean = this.equals(that)

		override def equals(that: Any): Boolean =
			that match {
				case that: Types.Map => that.keyType.equals(this.keyType) && that.valType.equals(this.valType)
				case _ => false
			}

		override def toString(): String = s"Map[$keyType,$valType]"
	}

	case object Any extends super.Val
	{
		override def canEqual(that: Any): Boolean = this.equals(that)

		override def equals(that: Any): Boolean = true

		override def toString(): String = "Any"
	}

	val String, Int, Bool, Double, Void, Unknown = Value

	// TODO Is there a better way than hardcoding these?
	val StringList: List = Types.List(String)
	val IntList: List = Types.List(Int)
	val BoolList: List = Types.List(Bool)
	val DoubleList: List = Types.List(Double)
	val StringSet: Set = Types.Set(String)
	val IntSet: Set = Types.Set(Int)
	val DoubleSet: Set = Types.Set(Double)

	val StringStringMap: Map = Map(String, String)
	val StringIntMap: Map = Map(String, Int)
	val IntStringMap: Map = Map(String, String)
	val IntIntMap: Map = Map(Int, Int)

	val AnyList: List = Types.List(Types.Any)
	val AnyIterable: Iterable = Types.Iterable(Types.Any)

	@inline def listOf(t: Types.Value): Types.Value = t match {
		case Types.String => StringList
		case Types.Int => IntList
		case Types.Bool => BoolList
		case Types.Double => DoubleList
		case _ =>
			eprintln("Could not determine type of " + t)
			AnyList
	}

	@inline def mapOf(k: Types.Value, v: Types.Value): Map = (k, v) match {
		case (Int, Int) => IntIntMap
		case (Int, String) => IntStringMap
		case (String, Int) => StringIntMap
		case (String, String) => StringStringMap
		case _ =>
			eprintln(s"Could not determine type of ($k, $v)")
			Map(Types.Any, Types.Any)
	}

	@inline def setOf(t: Types.Value): Types.Value = t match {
		case Types.String => StringSet
		case Types.Int => IntSet
		case Types.Double => DoubleSet
		case _ =>
			eprintln("Could not determine type of " + t)
			AnyList
	}

	@inline def childOf(t: Types.Types): Types.Types = t match {
		case Types.List(t) => t
		case Types.Set(t) => t
		case Types.String => t
		case _ =>
			eprintln("Could not determine type of " + t)
			Unknown
	}

	@inline def isListType(t: Types.Value): Boolean = t match {
		case Types.List(_) => true
		case _ => false
	}

	def typeof(x: Any): Types.Value =
	{
		x match {
			case _: String => String
			case _: Int => Int
			case _: Boolean => Bool
			case _: Double => Double
			case x: scala.List[_] if x.nonEmpty => x.head match {
				case _: String => StringList
				case _: Int => IntList
				case _: Double => DoubleList
				case (a, b) => (a, b) match {
					case (_: String, _: String) => StringStringMap
					case (_: String, _: Int) => StringIntMap
					case (_: Int, _: String) => IntStringMap
					case (_: Int, _: Int) => IntIntMap
					case _ =>
						eprintln("Could not determine type of " + x)
						Unknown
				}
				case _ =>
					eprintln("Could not determine type of " + x)
					Unknown
			}
			case x: scala.collection.Set[_] if x.nonEmpty => x.head match {
				case _: String => StringSet
				case _: Int => IntSet
				case _: Double => DoubleSet
				case _ =>
					eprintln("Could not determine type of " + x)
					Unknown
			}
			case x: scala.collection.Map[_, _] if x.nonEmpty => x.head match {
				case (_: String, _: String) => StringStringMap
				case (_: String, _: Int) => StringIntMap
				case (_: Int, _: String) => IntStringMap
				case (_: Int, _: Int) => IntIntMap
				case _ =>
					eprintln("Could not determine type of " + x)
					Unknown
			}
			case _ =>
				eprintln("Could not determine type of " + x)
				Unknown
		}
	}
}
