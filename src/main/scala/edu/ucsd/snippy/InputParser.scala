package edu.ucsd.snippy

import edu.ucsd.snippy.parser.Python3Parser._
import edu.ucsd.snippy.parser._
import org.antlr.v4.runtime.tree.TerminalNode
import org.antlr.v4.runtime.{BailErrorStrategy, BufferedTokenStream, CharStreams}

import scala.jdk.CollectionConverters.ListHasAsScala

class InputParser extends Python3BaseVisitor[Option[Any]] {
	def parse(code: String): Option[Any] = {
		val lexer = new Python3Lexer(CharStreams.fromString(code.trim))
		lexer.removeErrorListeners()
		// lexer.addErrorListener(new ThrowingLexerErrorListener)
		val parser = new Python3Parser(new BufferedTokenStream(lexer))
		parser.removeErrorListeners()
		parser.setErrorHandler(new BailErrorStrategy)
		this.visit(parser.expr())
	}

	override def visitArith_expr(ctx: Arith_exprContext): Option[Any] = {
		if (ctx.term().size() > 1) None
		else this.visitChildren(ctx)
	}

	override def visitTerm(ctx: TermContext): Option[Any] = {
		if (ctx.factor().size() > 1) None
		else this.visitChildren(ctx)
	}


	override def visitAtom(ctx: AtomContext): Option[Any] = {
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
				return Some(Map())
			}
		}


		visitChildren(ctx)
	}

	override def visitTestlist_comp(ctx: Testlist_compContext): Option[Any] = {
		val rs = ctx.children.asScala
			.map(_.accept(this))
			.filter(_.isDefined)
			.map(_.get)
			.toList


		if (rs.nonEmpty) {
			val typ = rs.head.getClass

			// TODO Print a better error message?
			if (rs.exists(!_.getClass.equals(typ))) return None
		}

		Some(rs)
	}

	override def visitDictorsetmaker(ctx: DictorsetmakerContext): Option[Any] = {
		if (ctx.children.isEmpty) {
			// This is an empty map
			Some(Map())
		} else if (!ctx.COLON().isEmpty) {
			// This is a map
			val map = ctx.children.asScala
				.map(_.accept(this))
				.filter(_.isDefined)
				.map(_.get)
				.zipWithIndex
				.groupBy(_._2 / 2)
				.map(arr => (arr._2.head._1, arr._2.tail.head._1))
			val keyTypes = map.keys.map(_.getClass)
			val valTypes = map.values.map(_.getClass)

			if (keyTypes.exists(!_.equals(keyTypes.head)) || valTypes.exists(!_.equals(valTypes.head))) {
				None
			} else {
				Some(map)
			}
		} else {
			// This is a set
			val set = ctx.children.asScala
				.map(_.accept(this))
				.filter(_.isDefined)
				.map(_.get)
				.toSet

			val types = set.map(_.getClass)
			if (types.exists(!_.equals(types.head))) {
				None
			} else {
				Some(set)
			}
		}
	}


	override def visitFactor(ctx: FactorContext): Option[Any] = {
		if (ctx.MINUS() != null) {
			// Probably a negative number?
			this.visitChildren(ctx) match {
				case Some(a: Int) => Some(-a)
				case Some(a: Double) => Some(-a)
				case _ => None
			}
		} else {
			this.visitChildren(ctx)
		}
	}


	override def visitTerminal(node: TerminalNode): Option[Any] = {
		// TODO How to get the actual value?
		node.getSymbol.getType match {
			case Python3Lexer.TRUE => Some(true)
			case Python3Lexer.FALSE => Some(false)
			case Python3Lexer.INTEGER =>
				Some(node.getText.toInt)
			case Python3Lexer.FLOAT_NUMBER =>
				Some(node.getText.toDouble)
			case _ => None
		}
	}
}
