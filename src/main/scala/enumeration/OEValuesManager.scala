package enumeration
import ast.ASTNode
import execution.Eval
import org.python.core.PyObject

import scala.collection.mutable

trait OEValuesManager {
  def isRepresentative(program: ast.ASTNode): Boolean
}
class InputsValuesManager(inputValues: List[Map[String, AnyRef]]) extends OEValuesManager {
  case class RefinedEqualityPyObject(val inner: PyObject) {
    def doEquals(lhs: PyObject, rhs: PyObject): Boolean = if (lhs.getType == rhs.getType) lhs == rhs else false
    override def equals(rhs: scala.Any): Boolean = rhs match {
      case refEq: RefinedEqualityPyObject => doEquals(this.inner,refEq.inner)
      case pyObj: PyObject => doEquals(inner,pyObj)
      case _ => super.equals(rhs)
    }

    override def hashCode(): Int = inner.hashCode()
  }
  val classValues = mutable.Set[List[RefinedEqualityPyObject]]()
  override def isRepresentative(program: ASTNode): Boolean = {
    val results: List[PyObject] = inputValues.map(singleInput => Eval(program.code,singleInput))
    classValues.add(results.map(RefinedEqualityPyObject))
  }
}
