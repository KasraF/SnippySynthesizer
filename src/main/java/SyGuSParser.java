// Generated from SyGuS.g4 by ANTLR 4.7.2
package sygus;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SyGuSParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, LineComment=28, WS=29, Numeral=30, Decimal=31, 
		BoolConst=32, HexConst=33, BinConst=34, StringConst=35, Symbol=36;
	public static final int
		RULE_syGuS = 0, RULE_sort = 1, RULE_bfTerm = 2, RULE_term = 3, RULE_sortedVar = 4, 
		RULE_varBinding = 5, RULE_feature = 6, RULE_cmd = 7, RULE_smtCmd = 8, 
		RULE_sortDecl = 9, RULE_dTDec = 10, RULE_dtConsDec = 11, RULE_grammarDef = 12, 
		RULE_groupedRuleList = 13, RULE_gTerm = 14, RULE_identifier = 15, RULE_index = 16, 
		RULE_literal = 17;
	private static String[] makeRuleNames() {
		return new String[] {
			"syGuS", "sort", "bfTerm", "term", "sortedVar", "varBinding", "feature", 
			"cmd", "smtCmd", "sortDecl", "dTDec", "dtConsDec", "grammarDef", "groupedRuleList", 
			"gTerm", "identifier", "index", "literal"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'exists'", "'forall'", "'let'", "'grammars'", "'fwd-decls'", 
			"'recursion'", "'check-synth'", "'constraint'", "'declare-var'", "'inv-constraint'", 
			"'set-feature'", "':'", "'synth-fun'", "'synth-inv'", "'declare-datatype'", 
			"'declare-datatypes'", "'declare-sort'", "'define-fun'", "'define-sort'", 
			"'set-info'", "'set-logic'", "'set-option'", "'Constant'", "'Variable'", 
			"'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, "LineComment", "WS", "Numeral", "Decimal", "BoolConst", 
			"HexConst", "BinConst", "StringConst", "Symbol"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "SyGuS.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SyGuSParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SyGuSContext extends ParserRuleContext {
		public List<CmdContext> cmd() {
			return getRuleContexts(CmdContext.class);
		}
		public CmdContext cmd(int i) {
			return getRuleContext(CmdContext.class,i);
		}
		public SyGuSContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_syGuS; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterSyGuS(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitSyGuS(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitSyGuS(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SyGuSContext syGuS() throws RecognitionException {
		SyGuSContext _localctx = new SyGuSContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_syGuS);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(36);
				cmd();
				}
				}
				setState(41);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SortContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<SortContext> sort() {
			return getRuleContexts(SortContext.class);
		}
		public SortContext sort(int i) {
			return getRuleContext(SortContext.class,i);
		}
		public SortContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sort; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterSort(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitSort(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitSort(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortContext sort() throws RecognitionException {
		SortContext _localctx = new SortContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_sort);
		int _la;
		try {
			setState(52);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(42);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(43);
				match(T__0);
				setState(44);
				identifier();
				setState(46); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(45);
					sort();
					}
					}
					setState(48); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__0 || _la==Symbol );
				setState(50);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BfTermContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public List<BfTermContext> bfTerm() {
			return getRuleContexts(BfTermContext.class);
		}
		public BfTermContext bfTerm(int i) {
			return getRuleContext(BfTermContext.class,i);
		}
		public BfTermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bfTerm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterBfTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitBfTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitBfTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BfTermContext bfTerm() throws RecognitionException {
		BfTermContext _localctx = new BfTermContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_bfTerm);
		int _la;
		try {
			setState(65);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(54);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(55);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(56);
				match(T__0);
				setState(57);
				identifier();
				setState(59); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(58);
					bfTerm();
					}
					}
					setState(61); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << Numeral) | (1L << Decimal) | (1L << BoolConst) | (1L << HexConst) | (1L << BinConst) | (1L << StringConst) | (1L << Symbol))) != 0) );
				setState(63);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<SortedVarContext> sortedVar() {
			return getRuleContexts(SortedVarContext.class);
		}
		public SortedVarContext sortedVar(int i) {
			return getRuleContext(SortedVarContext.class,i);
		}
		public List<VarBindingContext> varBinding() {
			return getRuleContexts(VarBindingContext.class);
		}
		public VarBindingContext varBinding(int i) {
			return getRuleContext(VarBindingContext.class,i);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_term);
		int _la;
		try {
			int _alt;
			setState(110);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(67);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(68);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(69);
				match(T__0);
				setState(70);
				identifier();
				setState(72); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(71);
					term();
					}
					}
					setState(74); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << Numeral) | (1L << Decimal) | (1L << BoolConst) | (1L << HexConst) | (1L << BinConst) | (1L << StringConst) | (1L << Symbol))) != 0) );
				setState(76);
				match(T__1);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(78);
				match(T__0);
				setState(79);
				match(T__2);
				setState(81); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(80);
						sortedVar();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(83); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(85);
				term();
				setState(86);
				match(T__1);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(88);
				match(T__0);
				setState(89);
				match(T__3);
				setState(91); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(90);
						sortedVar();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(93); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(95);
				term();
				setState(96);
				match(T__1);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(98);
				match(T__0);
				setState(99);
				match(T__4);
				setState(100);
				match(T__0);
				setState(102); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(101);
					varBinding();
					}
					}
					setState(104); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__0 );
				setState(106);
				match(T__1);
				setState(107);
				term();
				setState(108);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SortedVarContext extends ParserRuleContext {
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public SortedVarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortedVar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterSortedVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitSortedVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitSortedVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortedVarContext sortedVar() throws RecognitionException {
		SortedVarContext _localctx = new SortedVarContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_sortedVar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			match(T__0);
			setState(113);
			match(Symbol);
			setState(114);
			sort();
			setState(115);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VarBindingContext extends ParserRuleContext {
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public VarBindingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varBinding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterVarBinding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitVarBinding(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitVarBinding(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarBindingContext varBinding() throws RecognitionException {
		VarBindingContext _localctx = new VarBindingContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_varBinding);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(T__0);
			setState(118);
			match(Symbol);
			setState(119);
			term();
			setState(120);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FeatureContext extends ParserRuleContext {
		public FeatureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_feature; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterFeature(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitFeature(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitFeature(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FeatureContext feature() throws RecognitionException {
		FeatureContext _localctx = new FeatureContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_feature);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__6) | (1L << T__7))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CmdContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public List<TerminalNode> Symbol() { return getTokens(SyGuSParser.Symbol); }
		public TerminalNode Symbol(int i) {
			return getToken(SyGuSParser.Symbol, i);
		}
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public FeatureContext feature() {
			return getRuleContext(FeatureContext.class,0);
		}
		public TerminalNode BoolConst() { return getToken(SyGuSParser.BoolConst, 0); }
		public List<SortedVarContext> sortedVar() {
			return getRuleContexts(SortedVarContext.class);
		}
		public SortedVarContext sortedVar(int i) {
			return getRuleContext(SortedVarContext.class,i);
		}
		public GrammarDefContext grammarDef() {
			return getRuleContext(GrammarDefContext.class,0);
		}
		public SmtCmdContext smtCmd() {
			return getRuleContext(SmtCmdContext.class,0);
		}
		public CmdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cmd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterCmd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitCmd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitCmd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CmdContext cmd() throws RecognitionException {
		CmdContext _localctx = new CmdContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_cmd);
		int _la;
		try {
			setState(185);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(124);
				match(T__0);
				setState(125);
				match(T__8);
				setState(126);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(127);
				match(T__0);
				setState(128);
				match(T__9);
				setState(129);
				term();
				setState(130);
				match(T__1);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(132);
				match(T__0);
				setState(133);
				match(T__10);
				setState(134);
				match(Symbol);
				setState(135);
				sort();
				setState(136);
				match(T__1);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(138);
				match(T__0);
				setState(139);
				match(T__11);
				setState(140);
				match(Symbol);
				setState(141);
				match(Symbol);
				setState(142);
				match(Symbol);
				setState(143);
				match(Symbol);
				setState(144);
				match(T__1);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(145);
				match(T__0);
				setState(146);
				match(T__12);
				setState(147);
				match(T__13);
				setState(148);
				feature();
				setState(149);
				match(BoolConst);
				setState(150);
				match(T__1);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(152);
				match(T__0);
				setState(153);
				match(T__14);
				setState(154);
				match(Symbol);
				setState(155);
				match(T__0);
				setState(159);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__0) {
					{
					{
					setState(156);
					sortedVar();
					}
					}
					setState(161);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(162);
				match(T__1);
				setState(163);
				sort();
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(164);
					grammarDef();
					}
				}

				setState(167);
				match(T__1);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(169);
				match(T__0);
				setState(170);
				match(T__15);
				setState(171);
				match(Symbol);
				setState(172);
				match(T__0);
				setState(176);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__0) {
					{
					{
					setState(173);
					sortedVar();
					}
					}
					setState(178);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(179);
				match(T__1);
				setState(181);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(180);
					grammarDef();
					}
				}

				setState(183);
				match(T__1);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(184);
				smtCmd();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SmtCmdContext extends ParserRuleContext {
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public List<DTDecContext> dTDec() {
			return getRuleContexts(DTDecContext.class);
		}
		public DTDecContext dTDec(int i) {
			return getRuleContext(DTDecContext.class,i);
		}
		public SortDeclContext sortDecl() {
			return getRuleContext(SortDeclContext.class,0);
		}
		public TerminalNode Numeral() { return getToken(SyGuSParser.Numeral, 0); }
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public List<SortedVarContext> sortedVar() {
			return getRuleContexts(SortedVarContext.class);
		}
		public SortedVarContext sortedVar(int i) {
			return getRuleContext(SortedVarContext.class,i);
		}
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public SmtCmdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_smtCmd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterSmtCmd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitSmtCmd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitSmtCmd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SmtCmdContext smtCmd() throws RecognitionException {
		SmtCmdContext _localctx = new SmtCmdContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_smtCmd);
		int _la;
		try {
			setState(251);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(187);
				match(T__0);
				setState(188);
				match(T__16);
				setState(189);
				match(Symbol);
				setState(190);
				dTDec();
				setState(191);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(193);
				match(T__0);
				setState(194);
				match(T__17);
				setState(195);
				match(T__0);
				setState(196);
				sortDecl();
				setState(197);
				match(T__1);
				setState(198);
				match(T__0);
				setState(200); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(199);
					dTDec();
					}
					}
					setState(202); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==T__0 );
				setState(204);
				match(T__1);
				setState(205);
				match(T__1);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(207);
				match(T__0);
				setState(208);
				match(T__18);
				setState(209);
				match(Symbol);
				setState(210);
				match(Numeral);
				setState(211);
				match(T__1);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(212);
				match(T__0);
				setState(213);
				match(T__19);
				setState(214);
				match(Symbol);
				setState(215);
				match(T__0);
				setState(219);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__0) {
					{
					{
					setState(216);
					sortedVar();
					}
					}
					setState(221);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(222);
				match(T__1);
				setState(223);
				sort();
				setState(224);
				term();
				setState(225);
				match(T__1);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(227);
				match(T__0);
				setState(228);
				match(T__20);
				setState(229);
				match(Symbol);
				setState(230);
				sort();
				setState(231);
				match(T__1);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(233);
				match(T__0);
				setState(234);
				match(T__21);
				setState(235);
				match(T__13);
				setState(236);
				match(Symbol);
				setState(237);
				literal();
				setState(238);
				match(T__1);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(240);
				match(T__0);
				setState(241);
				match(T__22);
				setState(242);
				match(Symbol);
				setState(243);
				match(T__1);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(244);
				match(T__0);
				setState(245);
				match(T__23);
				setState(246);
				match(T__13);
				setState(247);
				match(Symbol);
				setState(248);
				literal();
				setState(249);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SortDeclContext extends ParserRuleContext {
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public TerminalNode Numeral() { return getToken(SyGuSParser.Numeral, 0); }
		public SortDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sortDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterSortDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitSortDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitSortDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SortDeclContext sortDecl() throws RecognitionException {
		SortDeclContext _localctx = new SortDeclContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_sortDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(253);
			match(T__0);
			setState(254);
			match(Symbol);
			setState(255);
			match(Numeral);
			setState(256);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DTDecContext extends ParserRuleContext {
		public List<DtConsDecContext> dtConsDec() {
			return getRuleContexts(DtConsDecContext.class);
		}
		public DtConsDecContext dtConsDec(int i) {
			return getRuleContext(DtConsDecContext.class,i);
		}
		public DTDecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dTDec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterDTDec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitDTDec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitDTDec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DTDecContext dTDec() throws RecognitionException {
		DTDecContext _localctx = new DTDecContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_dTDec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			match(T__0);
			setState(260); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(259);
				dtConsDec();
				}
				}
				setState(262); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__0 );
			setState(264);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DtConsDecContext extends ParserRuleContext {
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public List<SortedVarContext> sortedVar() {
			return getRuleContexts(SortedVarContext.class);
		}
		public SortedVarContext sortedVar(int i) {
			return getRuleContext(SortedVarContext.class,i);
		}
		public DtConsDecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dtConsDec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterDtConsDec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitDtConsDec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitDtConsDec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DtConsDecContext dtConsDec() throws RecognitionException {
		DtConsDecContext _localctx = new DtConsDecContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_dtConsDec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			match(T__0);
			setState(267);
			match(Symbol);
			setState(271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(268);
				sortedVar();
				}
				}
				setState(273);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(274);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GrammarDefContext extends ParserRuleContext {
		public List<GroupedRuleListContext> groupedRuleList() {
			return getRuleContexts(GroupedRuleListContext.class);
		}
		public GroupedRuleListContext groupedRuleList(int i) {
			return getRuleContext(GroupedRuleListContext.class,i);
		}
		public GrammarDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grammarDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterGrammarDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitGrammarDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitGrammarDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GrammarDefContext grammarDef() throws RecognitionException {
		GrammarDefContext _localctx = new GrammarDefContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_grammarDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276);
			match(T__0);
			setState(278); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(277);
				groupedRuleList();
				}
				}
				setState(280); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==T__0 );
			setState(282);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupedRuleListContext extends ParserRuleContext {
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public List<GTermContext> gTerm() {
			return getRuleContexts(GTermContext.class);
		}
		public GTermContext gTerm(int i) {
			return getRuleContext(GTermContext.class,i);
		}
		public GroupedRuleListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupedRuleList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterGroupedRuleList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitGroupedRuleList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitGroupedRuleList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupedRuleListContext groupedRuleList() throws RecognitionException {
		GroupedRuleListContext _localctx = new GroupedRuleListContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_groupedRuleList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
			match(T__0);
			setState(285);
			match(Symbol);
			setState(286);
			sort();
			setState(287);
			match(T__0);
			setState(289); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(288);
				gTerm();
				}
				}
				setState(291); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << Numeral) | (1L << Decimal) | (1L << BoolConst) | (1L << HexConst) | (1L << BinConst) | (1L << StringConst) | (1L << Symbol))) != 0) );
			setState(293);
			match(T__1);
			setState(294);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GTermContext extends ParserRuleContext {
		public SortContext sort() {
			return getRuleContext(SortContext.class,0);
		}
		public BfTermContext bfTerm() {
			return getRuleContext(BfTermContext.class,0);
		}
		public GTermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gTerm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterGTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitGTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitGTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GTermContext gTerm() throws RecognitionException {
		GTermContext _localctx = new GTermContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_gTerm);
		try {
			setState(307);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(296);
				match(T__0);
				setState(297);
				match(T__24);
				setState(298);
				sort();
				setState(299);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(301);
				match(T__0);
				setState(302);
				match(T__25);
				setState(303);
				sort();
				setState(304);
				match(T__1);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(306);
				bfTerm();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public List<IndexContext> index() {
			return getRuleContexts(IndexContext.class);
		}
		public IndexContext index(int i) {
			return getRuleContext(IndexContext.class,i);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_identifier);
		int _la;
		try {
			setState(320);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Symbol:
				enterOuterAlt(_localctx, 1);
				{
				setState(309);
				match(Symbol);
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(310);
				match(T__0);
				setState(311);
				match(T__26);
				setState(312);
				match(Symbol);
				setState(314); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(313);
					index();
					}
					}
					setState(316); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==Numeral || _la==Symbol );
				setState(318);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexContext extends ParserRuleContext {
		public TerminalNode Numeral() { return getToken(SyGuSParser.Numeral, 0); }
		public TerminalNode Symbol() { return getToken(SyGuSParser.Symbol, 0); }
		public IndexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterIndex(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitIndex(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitIndex(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexContext index() throws RecognitionException {
		IndexContext _localctx = new IndexContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_index);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(322);
			_la = _input.LA(1);
			if ( !(_la==Numeral || _la==Symbol) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode Numeral() { return getToken(SyGuSParser.Numeral, 0); }
		public TerminalNode Decimal() { return getToken(SyGuSParser.Decimal, 0); }
		public TerminalNode BoolConst() { return getToken(SyGuSParser.BoolConst, 0); }
		public TerminalNode HexConst() { return getToken(SyGuSParser.HexConst, 0); }
		public TerminalNode BinConst() { return getToken(SyGuSParser.BinConst, 0); }
		public TerminalNode StringConst() { return getToken(SyGuSParser.StringConst, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SyGuSListener ) ((SyGuSListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SyGuSVisitor ) return ((SyGuSVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Numeral) | (1L << Decimal) | (1L << BoolConst) | (1L << HexConst) | (1L << BinConst) | (1L << StringConst))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3&\u0149\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\3\2\7\2(\n\2\f\2\16\2+\13\2\3\3\3\3\3\3\3\3\6\3\61\n\3\r\3"+
		"\16\3\62\3\3\3\3\5\3\67\n\3\3\4\3\4\3\4\3\4\3\4\6\4>\n\4\r\4\16\4?\3\4"+
		"\3\4\5\4D\n\4\3\5\3\5\3\5\3\5\3\5\6\5K\n\5\r\5\16\5L\3\5\3\5\3\5\3\5\3"+
		"\5\6\5T\n\5\r\5\16\5U\3\5\3\5\3\5\3\5\3\5\3\5\6\5^\n\5\r\5\16\5_\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\6\5i\n\5\r\5\16\5j\3\5\3\5\3\5\3\5\5\5q\n\5\3\6"+
		"\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t\u00a0\n\t\f\t\16\t\u00a3\13\t"+
		"\3\t\3\t\3\t\5\t\u00a8\n\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\7\t\u00b1\n\t\f"+
		"\t\16\t\u00b4\13\t\3\t\3\t\5\t\u00b8\n\t\3\t\3\t\5\t\u00bc\n\t\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\6\n\u00cb\n\n\r\n\16\n\u00cc"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\7\n\u00dc\n\n\f\n"+
		"\16\n\u00df\13\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u00fe"+
		"\n\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\6\f\u0107\n\f\r\f\16\f\u0108\3\f"+
		"\3\f\3\r\3\r\3\r\7\r\u0110\n\r\f\r\16\r\u0113\13\r\3\r\3\r\3\16\3\16\6"+
		"\16\u0119\n\16\r\16\16\16\u011a\3\16\3\16\3\17\3\17\3\17\3\17\3\17\6\17"+
		"\u0124\n\17\r\17\16\17\u0125\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\5\20\u0136\n\20\3\21\3\21\3\21\3\21\3\21"+
		"\6\21\u013d\n\21\r\21\16\21\u013e\3\21\3\21\5\21\u0143\n\21\3\22\3\22"+
		"\3\23\3\23\3\23\2\2\24\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$\2\5"+
		"\3\2\b\n\4\2  &&\3\2 %\2\u0161\2)\3\2\2\2\4\66\3\2\2\2\6C\3\2\2\2\bp\3"+
		"\2\2\2\nr\3\2\2\2\fw\3\2\2\2\16|\3\2\2\2\20\u00bb\3\2\2\2\22\u00fd\3\2"+
		"\2\2\24\u00ff\3\2\2\2\26\u0104\3\2\2\2\30\u010c\3\2\2\2\32\u0116\3\2\2"+
		"\2\34\u011e\3\2\2\2\36\u0135\3\2\2\2 \u0142\3\2\2\2\"\u0144\3\2\2\2$\u0146"+
		"\3\2\2\2&(\5\20\t\2\'&\3\2\2\2(+\3\2\2\2)\'\3\2\2\2)*\3\2\2\2*\3\3\2\2"+
		"\2+)\3\2\2\2,\67\5 \21\2-.\7\3\2\2.\60\5 \21\2/\61\5\4\3\2\60/\3\2\2\2"+
		"\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2\2\2\63\64\3\2\2\2\64\65\7\4\2\2"+
		"\65\67\3\2\2\2\66,\3\2\2\2\66-\3\2\2\2\67\5\3\2\2\28D\5 \21\29D\5$\23"+
		"\2:;\7\3\2\2;=\5 \21\2<>\5\6\4\2=<\3\2\2\2>?\3\2\2\2?=\3\2\2\2?@\3\2\2"+
		"\2@A\3\2\2\2AB\7\4\2\2BD\3\2\2\2C8\3\2\2\2C9\3\2\2\2C:\3\2\2\2D\7\3\2"+
		"\2\2Eq\5 \21\2Fq\5$\23\2GH\7\3\2\2HJ\5 \21\2IK\5\b\5\2JI\3\2\2\2KL\3\2"+
		"\2\2LJ\3\2\2\2LM\3\2\2\2MN\3\2\2\2NO\7\4\2\2Oq\3\2\2\2PQ\7\3\2\2QS\7\5"+
		"\2\2RT\5\n\6\2SR\3\2\2\2TU\3\2\2\2US\3\2\2\2UV\3\2\2\2VW\3\2\2\2WX\5\b"+
		"\5\2XY\7\4\2\2Yq\3\2\2\2Z[\7\3\2\2[]\7\6\2\2\\^\5\n\6\2]\\\3\2\2\2^_\3"+
		"\2\2\2_]\3\2\2\2_`\3\2\2\2`a\3\2\2\2ab\5\b\5\2bc\7\4\2\2cq\3\2\2\2de\7"+
		"\3\2\2ef\7\7\2\2fh\7\3\2\2gi\5\f\7\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3"+
		"\2\2\2kl\3\2\2\2lm\7\4\2\2mn\5\b\5\2no\7\4\2\2oq\3\2\2\2pE\3\2\2\2pF\3"+
		"\2\2\2pG\3\2\2\2pP\3\2\2\2pZ\3\2\2\2pd\3\2\2\2q\t\3\2\2\2rs\7\3\2\2st"+
		"\7&\2\2tu\5\4\3\2uv\7\4\2\2v\13\3\2\2\2wx\7\3\2\2xy\7&\2\2yz\5\b\5\2z"+
		"{\7\4\2\2{\r\3\2\2\2|}\t\2\2\2}\17\3\2\2\2~\177\7\3\2\2\177\u0080\7\13"+
		"\2\2\u0080\u00bc\7\4\2\2\u0081\u0082\7\3\2\2\u0082\u0083\7\f\2\2\u0083"+
		"\u0084\5\b\5\2\u0084\u0085\7\4\2\2\u0085\u00bc\3\2\2\2\u0086\u0087\7\3"+
		"\2\2\u0087\u0088\7\r\2\2\u0088\u0089\7&\2\2\u0089\u008a\5\4\3\2\u008a"+
		"\u008b\7\4\2\2\u008b\u00bc\3\2\2\2\u008c\u008d\7\3\2\2\u008d\u008e\7\16"+
		"\2\2\u008e\u008f\7&\2\2\u008f\u0090\7&\2\2\u0090\u0091\7&\2\2\u0091\u0092"+
		"\7&\2\2\u0092\u00bc\7\4\2\2\u0093\u0094\7\3\2\2\u0094\u0095\7\17\2\2\u0095"+
		"\u0096\7\20\2\2\u0096\u0097\5\16\b\2\u0097\u0098\7\"\2\2\u0098\u0099\7"+
		"\4\2\2\u0099\u00bc\3\2\2\2\u009a\u009b\7\3\2\2\u009b\u009c\7\21\2\2\u009c"+
		"\u009d\7&\2\2\u009d\u00a1\7\3\2\2\u009e\u00a0\5\n\6\2\u009f\u009e\3\2"+
		"\2\2\u00a0\u00a3\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2"+
		"\u00a4\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a4\u00a5\7\4\2\2\u00a5\u00a7\5\4"+
		"\3\2\u00a6\u00a8\5\32\16\2\u00a7\u00a6\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8"+
		"\u00a9\3\2\2\2\u00a9\u00aa\7\4\2\2\u00aa\u00bc\3\2\2\2\u00ab\u00ac\7\3"+
		"\2\2\u00ac\u00ad\7\22\2\2\u00ad\u00ae\7&\2\2\u00ae\u00b2\7\3\2\2\u00af"+
		"\u00b1\5\n\6\2\u00b0\u00af\3\2\2\2\u00b1\u00b4\3\2\2\2\u00b2\u00b0\3\2"+
		"\2\2\u00b2\u00b3\3\2\2\2\u00b3\u00b5\3\2\2\2\u00b4\u00b2\3\2\2\2\u00b5"+
		"\u00b7\7\4\2\2\u00b6\u00b8\5\32\16\2\u00b7\u00b6\3\2\2\2\u00b7\u00b8\3"+
		"\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\u00bc\7\4\2\2\u00ba\u00bc\5\22\n\2\u00bb"+
		"~\3\2\2\2\u00bb\u0081\3\2\2\2\u00bb\u0086\3\2\2\2\u00bb\u008c\3\2\2\2"+
		"\u00bb\u0093\3\2\2\2\u00bb\u009a\3\2\2\2\u00bb\u00ab\3\2\2\2\u00bb\u00ba"+
		"\3\2\2\2\u00bc\21\3\2\2\2\u00bd\u00be\7\3\2\2\u00be\u00bf\7\23\2\2\u00bf"+
		"\u00c0\7&\2\2\u00c0\u00c1\5\26\f\2\u00c1\u00c2\7\4\2\2\u00c2\u00fe\3\2"+
		"\2\2\u00c3\u00c4\7\3\2\2\u00c4\u00c5\7\24\2\2\u00c5\u00c6\7\3\2\2\u00c6"+
		"\u00c7\5\24\13\2\u00c7\u00c8\7\4\2\2\u00c8\u00ca\7\3\2\2\u00c9\u00cb\5"+
		"\26\f\2\u00ca\u00c9\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cc"+
		"\u00cd\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce\u00cf\7\4\2\2\u00cf\u00d0\7\4"+
		"\2\2\u00d0\u00fe\3\2\2\2\u00d1\u00d2\7\3\2\2\u00d2\u00d3\7\25\2\2\u00d3"+
		"\u00d4\7&\2\2\u00d4\u00d5\7 \2\2\u00d5\u00fe\7\4\2\2\u00d6\u00d7\7\3\2"+
		"\2\u00d7\u00d8\7\26\2\2\u00d8\u00d9\7&\2\2\u00d9\u00dd\7\3\2\2\u00da\u00dc"+
		"\5\n\6\2\u00db\u00da\3\2\2\2\u00dc\u00df\3\2\2\2\u00dd\u00db\3\2\2\2\u00dd"+
		"\u00de\3\2\2\2\u00de\u00e0\3\2\2\2\u00df\u00dd\3\2\2\2\u00e0\u00e1\7\4"+
		"\2\2\u00e1\u00e2\5\4\3\2\u00e2\u00e3\5\b\5\2\u00e3\u00e4\7\4\2\2\u00e4"+
		"\u00fe\3\2\2\2\u00e5\u00e6\7\3\2\2\u00e6\u00e7\7\27\2\2\u00e7\u00e8\7"+
		"&\2\2\u00e8\u00e9\5\4\3\2\u00e9\u00ea\7\4\2\2\u00ea\u00fe\3\2\2\2\u00eb"+
		"\u00ec\7\3\2\2\u00ec\u00ed\7\30\2\2\u00ed\u00ee\7\20\2\2\u00ee\u00ef\7"+
		"&\2\2\u00ef\u00f0\5$\23\2\u00f0\u00f1\7\4\2\2\u00f1\u00fe\3\2\2\2\u00f2"+
		"\u00f3\7\3\2\2\u00f3\u00f4\7\31\2\2\u00f4\u00f5\7&\2\2\u00f5\u00fe\7\4"+
		"\2\2\u00f6\u00f7\7\3\2\2\u00f7\u00f8\7\32\2\2\u00f8\u00f9\7\20\2\2\u00f9"+
		"\u00fa\7&\2\2\u00fa\u00fb\5$\23\2\u00fb\u00fc\7\4\2\2\u00fc\u00fe\3\2"+
		"\2\2\u00fd\u00bd\3\2\2\2\u00fd\u00c3\3\2\2\2\u00fd\u00d1\3\2\2\2\u00fd"+
		"\u00d6\3\2\2\2\u00fd\u00e5\3\2\2\2\u00fd\u00eb\3\2\2\2\u00fd\u00f2\3\2"+
		"\2\2\u00fd\u00f6\3\2\2\2\u00fe\23\3\2\2\2\u00ff\u0100\7\3\2\2\u0100\u0101"+
		"\7&\2\2\u0101\u0102\7 \2\2\u0102\u0103\7\4\2\2\u0103\25\3\2\2\2\u0104"+
		"\u0106\7\3\2\2\u0105\u0107\5\30\r\2\u0106\u0105\3\2\2\2\u0107\u0108\3"+
		"\2\2\2\u0108\u0106\3\2\2\2\u0108\u0109\3\2\2\2\u0109\u010a\3\2\2\2\u010a"+
		"\u010b\7\4\2\2\u010b\27\3\2\2\2\u010c\u010d\7\3\2\2\u010d\u0111\7&\2\2"+
		"\u010e\u0110\5\n\6\2\u010f\u010e\3\2\2\2\u0110\u0113\3\2\2\2\u0111\u010f"+
		"\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0114\3\2\2\2\u0113\u0111\3\2\2\2\u0114"+
		"\u0115\7\4\2\2\u0115\31\3\2\2\2\u0116\u0118\7\3\2\2\u0117\u0119\5\34\17"+
		"\2\u0118\u0117\3\2\2\2\u0119\u011a\3\2\2\2\u011a\u0118\3\2\2\2\u011a\u011b"+
		"\3\2\2\2\u011b\u011c\3\2\2\2\u011c\u011d\7\4\2\2\u011d\33\3\2\2\2\u011e"+
		"\u011f\7\3\2\2\u011f\u0120\7&\2\2\u0120\u0121\5\4\3\2\u0121\u0123\7\3"+
		"\2\2\u0122\u0124\5\36\20\2\u0123\u0122\3\2\2\2\u0124\u0125\3\2\2\2\u0125"+
		"\u0123\3\2\2\2\u0125\u0126\3\2\2\2\u0126\u0127\3\2\2\2\u0127\u0128\7\4"+
		"\2\2\u0128\u0129\7\4\2\2\u0129\35\3\2\2\2\u012a\u012b\7\3\2\2\u012b\u012c"+
		"\7\33\2\2\u012c\u012d\5\4\3\2\u012d\u012e\7\4\2\2\u012e\u0136\3\2\2\2"+
		"\u012f\u0130\7\3\2\2\u0130\u0131\7\34\2\2\u0131\u0132\5\4\3\2\u0132\u0133"+
		"\7\4\2\2\u0133\u0136\3\2\2\2\u0134\u0136\5\6\4\2\u0135\u012a\3\2\2\2\u0135"+
		"\u012f\3\2\2\2\u0135\u0134\3\2\2\2\u0136\37\3\2\2\2\u0137\u0143\7&\2\2"+
		"\u0138\u0139\7\3\2\2\u0139\u013a\7\35\2\2\u013a\u013c\7&\2\2\u013b\u013d"+
		"\5\"\22\2\u013c\u013b\3\2\2\2\u013d\u013e\3\2\2\2\u013e\u013c\3\2\2\2"+
		"\u013e\u013f\3\2\2\2\u013f\u0140\3\2\2\2\u0140\u0141\7\4\2\2\u0141\u0143"+
		"\3\2\2\2\u0142\u0137\3\2\2\2\u0142\u0138\3\2\2\2\u0143!\3\2\2\2\u0144"+
		"\u0145\t\3\2\2\u0145#\3\2\2\2\u0146\u0147\t\4\2\2\u0147%\3\2\2\2\33)\62"+
		"\66?CLU_jp\u00a1\u00a7\u00b2\u00b7\u00bb\u00cc\u00dd\u00fd\u0108\u0111"+
		"\u011a\u0125\u0135\u013e\u0142";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}