// Generated from SyGuS.g4 by ANTLR 4.7.2
package sygus;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SyGuSParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SyGuSVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#syGuS}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSyGuS(SyGuSParser.SyGuSContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#sort}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort(SyGuSParser.SortContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#bfTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBfTerm(SyGuSParser.BfTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(SyGuSParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#sortedVar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortedVar(SyGuSParser.SortedVarContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#varBinding}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarBinding(SyGuSParser.VarBindingContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#feature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFeature(SyGuSParser.FeatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmd(SyGuSParser.CmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#smtCmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSmtCmd(SyGuSParser.SmtCmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#sortDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortDecl(SyGuSParser.SortDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#dTDec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDTDec(SyGuSParser.DTDecContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#dtConsDec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDtConsDec(SyGuSParser.DtConsDecContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#grammarDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrammarDef(SyGuSParser.GrammarDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#groupedRuleList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupedRuleList(SyGuSParser.GroupedRuleListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#gTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGTerm(SyGuSParser.GTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(SyGuSParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex(SyGuSParser.IndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link SyGuSParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(SyGuSParser.LiteralContext ctx);
}