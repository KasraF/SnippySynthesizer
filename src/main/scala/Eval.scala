package execution
import org.python.core.{PyException, PyObject}
import org.python.util.PythonInterpreter

object Eval {
  val pyInterp = new PythonInterpreter()
  def apply(code: String, vars: Map[String, AnyRef]): PyObject = {
    vars.foreach(varval => pyInterp.set(varval._1,varval._2))
    val res: PyObject = try {
      pyInterp.eval(code)
    }
    catch {
      case e: PyException => e.`type`
    }
    pyInterp.getLocals.asInstanceOf[org.python.core.PyStringMap].clear()
    res
  }

  //def apply(code: ast.ASTNode, vars: Map[String,AnyRef]): PyObject = this(code.code,vars)
}
