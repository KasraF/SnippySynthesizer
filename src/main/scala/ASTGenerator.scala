package sygus
import ast._
import sygus.SyGuSParser._
import org.antlr.v4.runtime.ParserRuleContext

case class ResolutionException(badCtx: ParserRuleContext) extends Exception

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
          if (sameExpr(ctx, node, ignoreWhitespace = true))
            return node
        }
      })
      throw ResolutionException(ctx)
    }
  }

  override def visitIdentifier(ctx: IdentifierContext): ASTNode = visitTerminal(ctx)

  override def visitLiteral(ctx: LiteralContext): ASTNode = visitTerminal(ctx)

  private def visitTerminal(ctx: ParserRuleContext): ASTNode = {
    val contexts = task.examples.map(_.input)
    task.vocab.leavesMakers.foreach(m => {
      val node = m.apply(Nil, contexts)
      if (sameExpr(ctx, node, ignoreWhitespace = false))
        return node
    })
    throw ResolutionException(ctx)
  }

  private def sameExpr(ctx: ParserRuleContext, node: ASTNode, ignoreWhitespace: Boolean): Boolean =
    if (ignoreWhitespace)
      ctx.getText.filterNot(_.isWhitespace) == node.code.filterNot(_.isWhitespace)
    else
      ctx.getText == node.code
}
