package sygus

import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.{BailErrorStrategy, BufferedTokenStream, CharStreams}
import sygus.Python3Parser.{AtomContext, DictorsetmakerContext, Testlist_compContext}

import scala.collection.JavaConverters._

class InputParser extends Python3BaseVisitor[Option[Any]]
{
	def parse(code: String) : Option[Any] =
	{
		val lexer = new Python3Lexer(CharStreams.fromString(code.trim))
		lexer.removeErrorListeners()
		// lexer.addErrorListener(new ThrowingLexerErrorListener)
		val parser = new Python3Parser(new BufferedTokenStream(lexer))
		parser.removeErrorListeners()
		parser.setErrorHandler(new BailErrorStrategy)
		this.visit(parser.term())
	}

	override def visitAtom(ctx: AtomContext): Option[Any] =
	{
		val strs = ctx.STRING()
		if (!strs.isEmpty) {
			// TODO Is there a more robust way of removing string quotes?
			return Some(
				strs.stream()
				  .map(_.getSymbol.getText)
				  .map((v1: String) => v1.substring(1, v1.length - 1))
				  .reduce("", (t: String, u: String) => t + u))
		}

		if (ctx.OPEN_BRACK() != null) {
			// We have a list
			val lst = ctx.testlist_comp()
			if (lst != null) {
				// Process the list
				return lst.accept(this)
			} else {
				// Empty list
				return Some(List())
			}
		}

		if (ctx.OPEN_BRACE() != null) {
			// We have a dict or set!
			val thing = ctx.dictorsetmaker()
			if (thing != null) {
				return thing.accept(this)
			} else {
				// Empty dictionary
				return Some(List())
			}
		}


		visitChildren(ctx)
	}

	override def visitTestlist_comp(ctx: Testlist_compContext): Option[Any] =
	{
		val rs = ctx.children.asScala
		  .map(_.accept(this))
		  .filter(_.isDefined)
		  .map(_.get)
		  .toList


		if (rs.nonEmpty) {
			val typ = rs(0).getClass

			// TODO Print a better error message?
			if (rs.exists(!_.getClass.equals(typ))) return None
		}

		Some(rs)
	}

	override def visitDictorsetmaker(ctx: DictorsetmakerContext): Option[Any] =
	{
		if (!ctx.COLON().isEmpty) {
			// This is a map
			val lst = ctx.children.asScala
			  .map(_.accept(this))
			  .filter(_.isDefined)
			  .map(_.get)
			var rs = List((lst.head, lst.tail.head))

			for (i <- 2 until lst.length by 2) {
				val key = lst(i)
				val value = lst(i+1)
				rs = rs :+ (key, value)
			}

			val keyType = rs.head._1.getClass
			val valType = rs.head._2.getClass

			if (rs.exists(tup => !tup._1.getClass.equals(keyType) || !tup._2.getClass.equals(valType))) {
				return None
			} else {
				return Some(rs)
			}

			// TODO We should support these as maps in Scala
			// Currently we require them as list, because the
			// order matters. :(
//			return Some(ctx.children.asScala
//			  .map(_.accept(this))
//			  .filter(_.isDefined)
//			  .map(_.get)
//			  .zipWithIndex
//			  .groupBy(_._2 / 2)
//			  .map(arr => (arr._2.head._1, arr._2.tail.head._1)))
		} else if (!ctx.COMMA().isEmpty) {
			// This is a set
			Some(ctx.children.asScala
			       .map(_.accept(this))
			       .filter(_.isDefined)
			       .map(_.get)
			       .toSet)
		}

		None
	}


	override def visitTerminal(node: TerminalNode): Option[Any] =
	{
		// TODO How to get the actual value?
		node.getSymbol.getType match {
			case Python3Lexer.TRUE => Some(true)
			case Python3Lexer.FALSE => Some(false)
			case Python3Lexer.NUMBER => Some(node.getText.toInt)
			case _ => None
		}
	}
}
