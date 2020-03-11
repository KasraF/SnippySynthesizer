package ast

object Types extends Enumeration
{
	type Types = Value

	case class List(subtype: Types) extends super.Val {
		override def canEqual(that: Any): Boolean = this.equals(that)
		override def equals(that: Any): Boolean =
			that.isInstanceOf[Types.List] && that.asInstanceOf[Types.List].subtype.equals(this.subtype)
	}

	case class Set(subtype: Types) extends super.Val {
		override def canEqual(that: Any): Boolean = this.equals(that)
		override def equals(that: Any): Boolean =
			that.isInstanceOf[Types.List] && that.asInstanceOf[Types.List].subtype.equals(this.subtype)
	}

	case class Map(keyType: Types, valType: Types) extends super.Val {
		override def canEqual(that: Any): Boolean = this.equals(that)
		override def equals(that: Any): Boolean =
			that match {
				case that: Types.Map => that.keyType.equals(this.keyType) && that.valType.equals(this.valType)
				case _ => false
			}
	}

	val String, Int, Bool, Unknown = Value

	// TODO Is there a better way than hardcoding these?
	val StringList: List = Types.List(String)
	val IntList: List = Types.List(Int)
	val StringSet: Set = Types.Set(String)
	val IntSet: Set = Types.Set(Int)

	def listOf(t: Types.Value) : Types.Value = Types.List(t)

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
