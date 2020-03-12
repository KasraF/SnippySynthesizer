package ast

object Types extends Enumeration
{
	type Types = Value

	case class List(childType: Types) extends super.Val {
		override def canEqual(that: Any): Boolean = this.equals(that)
		override def equals(that: Any): Boolean =
			that.isInstanceOf[Types.List] && that.asInstanceOf[Types.List].childType.equals(this.childType)
	}

	case class Set(childType: Types) extends super.Val {
		override def canEqual(that: Any): Boolean = this.equals(that)
		override def equals(that: Any): Boolean =
			that.isInstanceOf[Types.List] && that.asInstanceOf[Types.List].childType.equals(this.childType)
	}

	case class Map(keyType: Types, valType: Types) extends super.Val {
		override def canEqual(that: Any): Boolean = this.equals(that)
		override def equals(that: Any): Boolean =
			that match {
				case that: Types.Map => that.keyType.equals(this.keyType) && that.valType.equals(this.valType)
				case _ => false
			}

		override def toString(): String = s"Map[$keyType,$valType]"
	}

	val String, Int, Bool, Unknown = Value

	// TODO Is there a better way than hardcoding these?
	val StringList: List = Types.List(String)
	val IntList: List = Types.List(Int)
	val StringSet: Set = Types.Set(String)
	val IntSet: Set = Types.Set(Int)

	def listOf(t: Types.Value) : Types.Value = Types.List(t)

	def childOf(t: Types.Types): Types.Types = t match {
		case Types.List(t) => t
		case Types.Set(t) => t
		case Types.String => t
		case _ => Unknown
	}

	def isListType(t: Types.Value) : Boolean = t match {
		case Types.List(_) => true
		case _ => false
	}

	def typeof(x: Any) : Types.Value = {
		 x match {
			 case _: String => String
			 case _: Int => Int
			 case _: Boolean => Bool
			 case x: scala.List[_] if x.nonEmpty => x.head match {
				 case _: String => StringList
				 case _: Int    => IntList
				 case (a, b) => Map(typeof(a), typeof(b))
				 case _         => Unknown
			 }
			 case x: scala.collection.Set[_] if x.nonEmpty => x.head match {
				 case _: String => StringSet
				 case _: Int    => IntSet
				 case _         => Unknown
			 }
			 case x: scala.collection.Map[_,_] if x.nonEmpty => x.head match {
				 case (_: String, _: String) => Map(String, String)
				 case (_: String, _: Int)    => Map(String, Int)
				 case (_: Int,    _: String) => Map(String, String)
				 case (_: Int,    _: Int)    => Map(Int, Int)
				 case _                      => Unknown
			 }
			 case _ =>
				 println("Could not determine type of " + x)
				 Unknown
		 }
	}
}
