package ast

object Types extends Enumeration
{
	type Types = Value
	val String, Int, Bool, StringList, Unknown = Value

	def typeof(x: Any) : Types.Value = {
		 x match {
			 case _: String => Types.String
			 case _: Int => Types.Int
			 case _: Boolean => Types.Bool
			 case _ => {
				 println("Could not determine type of " + x)
				 Unknown
			 }
		 }
	}
}
