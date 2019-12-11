package execution

object Types extends Enumeration {
  type Types = Value
  val Any, String, Int, Double, Bool = Value
  def subclassEq(lhs:Types, rhs: Types): Boolean = (rhs == Any) || lhs == rhs
}
