// Generated from SyGuS.g4 by ANTLR 4.7.2
package sygus;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SyGuSLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
			"T__25", "T__26", "LineComment", "WS", "Numeral", "Decimal", "BoolConst", 
			"HexConst", "BinConst", "StringConst", "Symbol"
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


	public SyGuSLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SyGuS.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2&\u019a\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3"+
		"\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\34\3\34\3\35\3\35\7\35\u014e\n\35\f\35\16\35\u0151"+
		"\13\35\3\35\3\35\3\36\6\36\u0156\n\36\r\36\16\36\u0157\3\36\3\36\3\37"+
		"\3\37\7\37\u015e\n\37\f\37\16\37\u0161\13\37\3\37\5\37\u0164\n\37\3 \3"+
		" \3 \7 \u0169\n \f \16 \u016c\13 \3 \3 \3!\3!\3!\3!\3!\3!\3!\3!\3!\5!"+
		"\u0179\n!\3\"\3\"\3\"\3\"\6\"\u017f\n\"\r\"\16\"\u0180\3#\3#\3#\3#\6#"+
		"\u0187\n#\r#\16#\u0188\3$\3$\7$\u018d\n$\f$\16$\u0190\13$\3$\3$\3%\3%"+
		"\7%\u0196\n%\f%\16%\u0199\13%\2\2&\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n"+
		"\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30"+
		"/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I&\3\2\f\3\2\f\f\5\2"+
		"\13\f\17\17\"\"\3\2\63;\3\2\62;\3\2\62\62\5\2\62;CHch\3\2\62\63\3\2$$"+
		"\f\2##&(,-/\61>AC\\`ac|~~\u0080\u0080\f\2##&(,-/;>AC\\`ac|~~\u0080\u0080"+
		"\2\u01a3\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2"+
		"\2G\3\2\2\2\2I\3\2\2\2\3K\3\2\2\2\5M\3\2\2\2\7O\3\2\2\2\tV\3\2\2\2\13"+
		"]\3\2\2\2\ra\3\2\2\2\17j\3\2\2\2\21t\3\2\2\2\23~\3\2\2\2\25\u008a\3\2"+
		"\2\2\27\u0095\3\2\2\2\31\u00a1\3\2\2\2\33\u00b0\3\2\2\2\35\u00bc\3\2\2"+
		"\2\37\u00be\3\2\2\2!\u00c8\3\2\2\2#\u00d2\3\2\2\2%\u00e3\3\2\2\2\'\u00f5"+
		"\3\2\2\2)\u0102\3\2\2\2+\u010d\3\2\2\2-\u0119\3\2\2\2/\u0122\3\2\2\2\61"+
		"\u012c\3\2\2\2\63\u0137\3\2\2\2\65\u0140\3\2\2\2\67\u0149\3\2\2\29\u014b"+
		"\3\2\2\2;\u0155\3\2\2\2=\u0163\3\2\2\2?\u0165\3\2\2\2A\u0178\3\2\2\2C"+
		"\u017a\3\2\2\2E\u0182\3\2\2\2G\u018a\3\2\2\2I\u0193\3\2\2\2KL\7*\2\2L"+
		"\4\3\2\2\2MN\7+\2\2N\6\3\2\2\2OP\7g\2\2PQ\7z\2\2QR\7k\2\2RS\7u\2\2ST\7"+
		"v\2\2TU\7u\2\2U\b\3\2\2\2VW\7h\2\2WX\7q\2\2XY\7t\2\2YZ\7c\2\2Z[\7n\2\2"+
		"[\\\7n\2\2\\\n\3\2\2\2]^\7n\2\2^_\7g\2\2_`\7v\2\2`\f\3\2\2\2ab\7i\2\2"+
		"bc\7t\2\2cd\7c\2\2de\7o\2\2ef\7o\2\2fg\7c\2\2gh\7t\2\2hi\7u\2\2i\16\3"+
		"\2\2\2jk\7h\2\2kl\7y\2\2lm\7f\2\2mn\7/\2\2no\7f\2\2op\7g\2\2pq\7e\2\2"+
		"qr\7n\2\2rs\7u\2\2s\20\3\2\2\2tu\7t\2\2uv\7g\2\2vw\7e\2\2wx\7w\2\2xy\7"+
		"t\2\2yz\7u\2\2z{\7k\2\2{|\7q\2\2|}\7p\2\2}\22\3\2\2\2~\177\7e\2\2\177"+
		"\u0080\7j\2\2\u0080\u0081\7g\2\2\u0081\u0082\7e\2\2\u0082\u0083\7m\2\2"+
		"\u0083\u0084\7/\2\2\u0084\u0085\7u\2\2\u0085\u0086\7{\2\2\u0086\u0087"+
		"\7p\2\2\u0087\u0088\7v\2\2\u0088\u0089\7j\2\2\u0089\24\3\2\2\2\u008a\u008b"+
		"\7e\2\2\u008b\u008c\7q\2\2\u008c\u008d\7p\2\2\u008d\u008e\7u\2\2\u008e"+
		"\u008f\7v\2\2\u008f\u0090\7t\2\2\u0090\u0091\7c\2\2\u0091\u0092\7k\2\2"+
		"\u0092\u0093\7p\2\2\u0093\u0094\7v\2\2\u0094\26\3\2\2\2\u0095\u0096\7"+
		"f\2\2\u0096\u0097\7g\2\2\u0097\u0098\7e\2\2\u0098\u0099\7n\2\2\u0099\u009a"+
		"\7c\2\2\u009a\u009b\7t\2\2\u009b\u009c\7g\2\2\u009c\u009d\7/\2\2\u009d"+
		"\u009e\7x\2\2\u009e\u009f\7c\2\2\u009f\u00a0\7t\2\2\u00a0\30\3\2\2\2\u00a1"+
		"\u00a2\7k\2\2\u00a2\u00a3\7p\2\2\u00a3\u00a4\7x\2\2\u00a4\u00a5\7/\2\2"+
		"\u00a5\u00a6\7e\2\2\u00a6\u00a7\7q\2\2\u00a7\u00a8\7p\2\2\u00a8\u00a9"+
		"\7u\2\2\u00a9\u00aa\7v\2\2\u00aa\u00ab\7t\2\2\u00ab\u00ac\7c\2\2\u00ac"+
		"\u00ad\7k\2\2\u00ad\u00ae\7p\2\2\u00ae\u00af\7v\2\2\u00af\32\3\2\2\2\u00b0"+
		"\u00b1\7u\2\2\u00b1\u00b2\7g\2\2\u00b2\u00b3\7v\2\2\u00b3\u00b4\7/\2\2"+
		"\u00b4\u00b5\7h\2\2\u00b5\u00b6\7g\2\2\u00b6\u00b7\7c\2\2\u00b7\u00b8"+
		"\7v\2\2\u00b8\u00b9\7w\2\2\u00b9\u00ba\7t\2\2\u00ba\u00bb\7g\2\2\u00bb"+
		"\34\3\2\2\2\u00bc\u00bd\7<\2\2\u00bd\36\3\2\2\2\u00be\u00bf\7u\2\2\u00bf"+
		"\u00c0\7{\2\2\u00c0\u00c1\7p\2\2\u00c1\u00c2\7v\2\2\u00c2\u00c3\7j\2\2"+
		"\u00c3\u00c4\7/\2\2\u00c4\u00c5\7h\2\2\u00c5\u00c6\7w\2\2\u00c6\u00c7"+
		"\7p\2\2\u00c7 \3\2\2\2\u00c8\u00c9\7u\2\2\u00c9\u00ca\7{\2\2\u00ca\u00cb"+
		"\7p\2\2\u00cb\u00cc\7v\2\2\u00cc\u00cd\7j\2\2\u00cd\u00ce\7/\2\2\u00ce"+
		"\u00cf\7k\2\2\u00cf\u00d0\7p\2\2\u00d0\u00d1\7x\2\2\u00d1\"\3\2\2\2\u00d2"+
		"\u00d3\7f\2\2\u00d3\u00d4\7g\2\2\u00d4\u00d5\7e\2\2\u00d5\u00d6\7n\2\2"+
		"\u00d6\u00d7\7c\2\2\u00d7\u00d8\7t\2\2\u00d8\u00d9\7g\2\2\u00d9\u00da"+
		"\7/\2\2\u00da\u00db\7f\2\2\u00db\u00dc\7c\2\2\u00dc\u00dd\7v\2\2\u00dd"+
		"\u00de\7c\2\2\u00de\u00df\7v\2\2\u00df\u00e0\7{\2\2\u00e0\u00e1\7r\2\2"+
		"\u00e1\u00e2\7g\2\2\u00e2$\3\2\2\2\u00e3\u00e4\7f\2\2\u00e4\u00e5\7g\2"+
		"\2\u00e5\u00e6\7e\2\2\u00e6\u00e7\7n\2\2\u00e7\u00e8\7c\2\2\u00e8\u00e9"+
		"\7t\2\2\u00e9\u00ea\7g\2\2\u00ea\u00eb\7/\2\2\u00eb\u00ec\7f\2\2\u00ec"+
		"\u00ed\7c\2\2\u00ed\u00ee\7v\2\2\u00ee\u00ef\7c\2\2\u00ef\u00f0\7v\2\2"+
		"\u00f0\u00f1\7{\2\2\u00f1\u00f2\7r\2\2\u00f2\u00f3\7g\2\2\u00f3\u00f4"+
		"\7u\2\2\u00f4&\3\2\2\2\u00f5\u00f6\7f\2\2\u00f6\u00f7\7g\2\2\u00f7\u00f8"+
		"\7e\2\2\u00f8\u00f9\7n\2\2\u00f9\u00fa\7c\2\2\u00fa\u00fb\7t\2\2\u00fb"+
		"\u00fc\7g\2\2\u00fc\u00fd\7/\2\2\u00fd\u00fe\7u\2\2\u00fe\u00ff\7q\2\2"+
		"\u00ff\u0100\7t\2\2\u0100\u0101\7v\2\2\u0101(\3\2\2\2\u0102\u0103\7f\2"+
		"\2\u0103\u0104\7g\2\2\u0104\u0105\7h\2\2\u0105\u0106\7k\2\2\u0106\u0107"+
		"\7p\2\2\u0107\u0108\7g\2\2\u0108\u0109\7/\2\2\u0109\u010a\7h\2\2\u010a"+
		"\u010b\7w\2\2\u010b\u010c\7p\2\2\u010c*\3\2\2\2\u010d\u010e\7f\2\2\u010e"+
		"\u010f\7g\2\2\u010f\u0110\7h\2\2\u0110\u0111\7k\2\2\u0111\u0112\7p\2\2"+
		"\u0112\u0113\7g\2\2\u0113\u0114\7/\2\2\u0114\u0115\7u\2\2\u0115\u0116"+
		"\7q\2\2\u0116\u0117\7t\2\2\u0117\u0118\7v\2\2\u0118,\3\2\2\2\u0119\u011a"+
		"\7u\2\2\u011a\u011b\7g\2\2\u011b\u011c\7v\2\2\u011c\u011d\7/\2\2\u011d"+
		"\u011e\7k\2\2\u011e\u011f\7p\2\2\u011f\u0120\7h\2\2\u0120\u0121\7q\2\2"+
		"\u0121.\3\2\2\2\u0122\u0123\7u\2\2\u0123\u0124\7g\2\2\u0124\u0125\7v\2"+
		"\2\u0125\u0126\7/\2\2\u0126\u0127\7n\2\2\u0127\u0128\7q\2\2\u0128\u0129"+
		"\7i\2\2\u0129\u012a\7k\2\2\u012a\u012b\7e\2\2\u012b\60\3\2\2\2\u012c\u012d"+
		"\7u\2\2\u012d\u012e\7g\2\2\u012e\u012f\7v\2\2\u012f\u0130\7/\2\2\u0130"+
		"\u0131\7q\2\2\u0131\u0132\7r\2\2\u0132\u0133\7v\2\2\u0133\u0134\7k\2\2"+
		"\u0134\u0135\7q\2\2\u0135\u0136\7p\2\2\u0136\62\3\2\2\2\u0137\u0138\7"+
		"E\2\2\u0138\u0139\7q\2\2\u0139\u013a\7p\2\2\u013a\u013b\7u\2\2\u013b\u013c"+
		"\7v\2\2\u013c\u013d\7c\2\2\u013d\u013e\7p\2\2\u013e\u013f\7v\2\2\u013f"+
		"\64\3\2\2\2\u0140\u0141\7X\2\2\u0141\u0142\7c\2\2\u0142\u0143\7t\2\2\u0143"+
		"\u0144\7k\2\2\u0144\u0145\7c\2\2\u0145\u0146\7d\2\2\u0146\u0147\7n\2\2"+
		"\u0147\u0148\7g\2\2\u0148\66\3\2\2\2\u0149\u014a\7a\2\2\u014a8\3\2\2\2"+
		"\u014b\u014f\7=\2\2\u014c\u014e\n\2\2\2\u014d\u014c\3\2\2\2\u014e\u0151"+
		"\3\2\2\2\u014f\u014d\3\2\2\2\u014f\u0150\3\2\2\2\u0150\u0152\3\2\2\2\u0151"+
		"\u014f\3\2\2\2\u0152\u0153\b\35\2\2\u0153:\3\2\2\2\u0154\u0156\t\3\2\2"+
		"\u0155\u0154\3\2\2\2\u0156\u0157\3\2\2\2\u0157\u0155\3\2\2\2\u0157\u0158"+
		"\3\2\2\2\u0158\u0159\3\2\2\2\u0159\u015a\b\36\2\2\u015a<\3\2\2\2\u015b"+
		"\u015f\t\4\2\2\u015c\u015e\t\5\2\2\u015d\u015c\3\2\2\2\u015e\u0161\3\2"+
		"\2\2\u015f\u015d\3\2\2\2\u015f\u0160\3\2\2\2\u0160\u0164\3\2\2\2\u0161"+
		"\u015f\3\2\2\2\u0162\u0164\t\6\2\2\u0163\u015b\3\2\2\2\u0163\u0162\3\2"+
		"\2\2\u0164>\3\2\2\2\u0165\u0166\5=\37\2\u0166\u016a\7\60\2\2\u0167\u0169"+
		"\7\62\2\2\u0168\u0167\3\2\2\2\u0169\u016c\3\2\2\2\u016a\u0168\3\2\2\2"+
		"\u016a\u016b\3\2\2\2\u016b\u016d\3\2\2\2\u016c\u016a\3\2\2\2\u016d\u016e"+
		"\5=\37\2\u016e@\3\2\2\2\u016f\u0170\7v\2\2\u0170\u0171\7t\2\2\u0171\u0172"+
		"\7w\2\2\u0172\u0179\7g\2\2\u0173\u0174\7h\2\2\u0174\u0175\7c\2\2\u0175"+
		"\u0176\7n\2\2\u0176\u0177\7u\2\2\u0177\u0179\7g\2\2\u0178\u016f\3\2\2"+
		"\2\u0178\u0173\3\2\2\2\u0179B\3\2\2\2\u017a\u017b\7%\2\2\u017b\u017c\7"+
		"z\2\2\u017c\u017e\3\2\2\2\u017d\u017f\t\7\2\2\u017e\u017d\3\2\2\2\u017f"+
		"\u0180\3\2\2\2\u0180\u017e\3\2\2\2\u0180\u0181\3\2\2\2\u0181D\3\2\2\2"+
		"\u0182\u0183\7%\2\2\u0183\u0184\7d\2\2\u0184\u0186\3\2\2\2\u0185\u0187"+
		"\t\b\2\2\u0186\u0185\3\2\2\2\u0187\u0188\3\2\2\2\u0188\u0186\3\2\2\2\u0188"+
		"\u0189\3\2\2\2\u0189F\3\2\2\2\u018a\u018e\7$\2\2\u018b\u018d\n\t\2\2\u018c"+
		"\u018b\3\2\2\2\u018d\u0190\3\2\2\2\u018e\u018c\3\2\2\2\u018e\u018f\3\2"+
		"\2\2\u018f\u0191\3\2\2\2\u0190\u018e\3\2\2\2\u0191\u0192\7$\2\2\u0192"+
		"H\3\2\2\2\u0193\u0197\t\n\2\2\u0194\u0196\t\13\2\2\u0195\u0194\3\2\2\2"+
		"\u0196\u0199\3\2\2\2\u0197\u0195\3\2\2\2\u0197\u0198\3\2\2\2\u0198J\3"+
		"\2\2\2\u0199\u0197\3\2\2\2\r\2\u014f\u0157\u015f\u0163\u016a\u0178\u0180"+
		"\u0188\u018e\u0197\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}