import ast._
import SyGuSParser._
import org.antlr.v4.runtime.ParserRuleContext

case class ResolutionException(msg: String) extends Exception

class ASTGenerator(task: SygusFileTask) extends SyGuSBaseVisitor[ASTNode] {

  override def visitBfTerm(ctx: BfTermContext): ASTNode = {
    val n = ctx.getChildCount
    if (n == 1) visitChildren(ctx)
    else {
      assert(n >= 4, "Strange parse node")
      val childASTs = (for {
        i <- Range(2, n - 1)
        c = ctx.getChild(i)
        res = c.accept(this)
      } yield res).toList
      val contexts = task.examples.map(_.input)
      task.vocab.nodeMakers.foreach(m => {
        if (m.canMake(childASTs)) {
          val node = m.apply(childASTs, contexts)
          if (sameExpr(ctx, node))
            return node
        }
      })
      throw ResolutionException(ctx.getText)
    }
  }

  override def visitIdentifier(ctx: IdentifierContext): ASTNode = {
    val contexts = task.examples.map(_.input)
    task.vocab.leavesMakers.foreach(m => {
      val node = m.apply(Nil, contexts)
      if (sameExpr(ctx, node))
        return node
    })
    throw ResolutionException(ctx.getText)
  }

  override def visitLiteral(ctx: LiteralContext): ASTNode = {
    val contexts = task.examples.map(_.input)
    task.vocab.leavesMakers.foreach(m => {
      val node = m.apply(Nil, contexts)
      if (sameExpr(ctx, node))
        return node
    })
    throw ResolutionException(ctx.getText)
  }

  private def sameExpr(ctx: ParserRuleContext, node: ASTNode): Boolean =
    ctx.getText.filterNot(_.isWhitespace) == node.code.filterNot(_.isWhitespace)
}
