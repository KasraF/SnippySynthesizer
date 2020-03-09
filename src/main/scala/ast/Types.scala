package ast

object Types extends Enumeration
{
	type Types = Value
	val String, Int, Bool, StringList, IntList, Unknown = Value

	def listOf(t: Types.Value) : Types.Value = t match {
		case Int => IntList
		case String => StringList
		case t =>
			assert(assertion = false, s"list of type not supported: $t")
			Unknown
	}

	def isListType(t: Types.Value) : Boolean = t match {
		case IntList => true
		case StringList => true
		case _ => false
	}

	def typeof(x: Any) : Types.Value = {
		 x match {
			 case _: String => Types.String
			 case _: Int => Types.Int
			 case _: Boolean => Types.Bool
			 case _ =>
				 println("Could not determine type of " + x)
				 Unknown
		 }
	}
}
