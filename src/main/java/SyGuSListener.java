// Generated from SyGuS.g4 by ANTLR 4.7.2
package sygus;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SyGuSParser}.
 */
public interface SyGuSListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#syGuS}.
	 * @param ctx the parse tree
	 */
	void enterSyGuS(SyGuSParser.SyGuSContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#syGuS}.
	 * @param ctx the parse tree
	 */
	void exitSyGuS(SyGuSParser.SyGuSContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#sort}.
	 * @param ctx the parse tree
	 */
	void enterSort(SyGuSParser.SortContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#sort}.
	 * @param ctx the parse tree
	 */
	void exitSort(SyGuSParser.SortContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#bfTerm}.
	 * @param ctx the parse tree
	 */
	void enterBfTerm(SyGuSParser.BfTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#bfTerm}.
	 * @param ctx the parse tree
	 */
	void exitBfTerm(SyGuSParser.BfTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(SyGuSParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(SyGuSParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#sortedVar}.
	 * @param ctx the parse tree
	 */
	void enterSortedVar(SyGuSParser.SortedVarContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#sortedVar}.
	 * @param ctx the parse tree
	 */
	void exitSortedVar(SyGuSParser.SortedVarContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#varBinding}.
	 * @param ctx the parse tree
	 */
	void enterVarBinding(SyGuSParser.VarBindingContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#varBinding}.
	 * @param ctx the parse tree
	 */
	void exitVarBinding(SyGuSParser.VarBindingContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#feature}.
	 * @param ctx the parse tree
	 */
	void enterFeature(SyGuSParser.FeatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#feature}.
	 * @param ctx the parse tree
	 */
	void exitFeature(SyGuSParser.FeatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#cmd}.
	 * @param ctx the parse tree
	 */
	void enterCmd(SyGuSParser.CmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#cmd}.
	 * @param ctx the parse tree
	 */
	void exitCmd(SyGuSParser.CmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#smtCmd}.
	 * @param ctx the parse tree
	 */
	void enterSmtCmd(SyGuSParser.SmtCmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#smtCmd}.
	 * @param ctx the parse tree
	 */
	void exitSmtCmd(SyGuSParser.SmtCmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#sortDecl}.
	 * @param ctx the parse tree
	 */
	void enterSortDecl(SyGuSParser.SortDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#sortDecl}.
	 * @param ctx the parse tree
	 */
	void exitSortDecl(SyGuSParser.SortDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#dTDec}.
	 * @param ctx the parse tree
	 */
	void enterDTDec(SyGuSParser.DTDecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#dTDec}.
	 * @param ctx the parse tree
	 */
	void exitDTDec(SyGuSParser.DTDecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#dtConsDec}.
	 * @param ctx the parse tree
	 */
	void enterDtConsDec(SyGuSParser.DtConsDecContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#dtConsDec}.
	 * @param ctx the parse tree
	 */
	void exitDtConsDec(SyGuSParser.DtConsDecContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#grammarDef}.
	 * @param ctx the parse tree
	 */
	void enterGrammarDef(SyGuSParser.GrammarDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#grammarDef}.
	 * @param ctx the parse tree
	 */
	void exitGrammarDef(SyGuSParser.GrammarDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#groupedRuleList}.
	 * @param ctx the parse tree
	 */
	void enterGroupedRuleList(SyGuSParser.GroupedRuleListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#groupedRuleList}.
	 * @param ctx the parse tree
	 */
	void exitGroupedRuleList(SyGuSParser.GroupedRuleListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#gTerm}.
	 * @param ctx the parse tree
	 */
	void enterGTerm(SyGuSParser.GTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#gTerm}.
	 * @param ctx the parse tree
	 */
	void exitGTerm(SyGuSParser.GTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(SyGuSParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(SyGuSParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#index}.
	 * @param ctx the parse tree
	 */
	void enterIndex(SyGuSParser.IndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#index}.
	 * @param ctx the parse tree
	 */
	void exitIndex(SyGuSParser.IndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SyGuSParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(SyGuSParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link SyGuSParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(SyGuSParser.LiteralContext ctx);
}