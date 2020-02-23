package ast

object Types extends Enumeration
{
	type Types = Value
	val String, Int, Bool, Unknown = Value

	def typeof(x: Any) : Types.Value = {
		 x match {
			 case _: String => Types.String
			 case _: Integer => Types.Int
			 case _: Boolean => Types.Bool
			 case _ => Unknown
		 }
	}
}
