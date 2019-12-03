grammar SyGuS;

syGuS : cmd*
      ;

sort : identifier | '(' identifier sort+ ')'
    ;

bfTerm : identifier
    | literal
    | '(' identifier bfTerm+ ')'
    ;

term: identifier
    | literal
    | '(' identifier term+ ')'
    | '(' 'exists' sortedVar+ term ')'
    | '(' 'forall' sortedVar+ term ')'
    | '(' 'let' '(' varBinding+ ')' term ')'
    ;

sortedVar : '(' Symbol sort ')'
    ;

varBinding : '(' Symbol term ')'
    ;

feature : 'grammars' | 'fwd-decls' | 'recursion'
    ;

cmd : '(' 'check-synth' ')'
    | '(' 'constraint' term ')'
    | '(' 'declare-var' Symbol sort ')'
    | '(' 'inv-constraint' Symbol Symbol Symbol Symbol ')'
    | '(' 'set-feature' ':' feature BoolConst ')'
    | '(' 'synth-fun' Symbol '(' sortedVar* ')' sort grammarDef? ')'
    | '(' 'synth-inv' Symbol '(' sortedVar* ')' grammarDef? ')'
    | smtCmd
    ;

smtCmd :  '(' 'declare-datatype' Symbol dTDec ')'
       |  '(' 'declare-datatypes' '(' sortDecl ')' '(' dTDec+ ')' ')'  // n+1 for each
       |  '(' 'declare-sort' Symbol Numeral ')'
       |  '(' 'define-fun' Symbol '(' sortedVar* ')' sort term ')'
       |  '(' 'define-sort' Symbol sort ')'
       |  '(' 'set-info' ':' Symbol literal ')'
       |  '(' 'set-logic' Symbol ')'
       |  '(' 'set-option' ':' Symbol literal ')'
       ;

sortDecl : '(' Symbol Numeral ')'
         ;

dTDec : '(' dtConsDec+ ')'
      ;

dtConsDec : '(' Symbol sortedVar* ')'
          ;

grammarDef : '(' groupedRuleList+ ')' //'(' sortedVar+ ')' '(' groupedRuleList+ ')'  // n+1
           ;

groupedRuleList : '(' Symbol sort '(' gTerm+ ')' ')'
                ;

gTerm : '(' 'Constant' sort ')'
      | '(' 'Variable' sort ')'
      | bfTerm
      ;

identifier : Symbol | '(' '_' Symbol index+ ')'
    ;

index : Numeral | Symbol
    ;

literal : Numeral | Decimal | BoolConst
    | HexConst | BinConst | StringConst
    ;

LineComment : ';' ~[\n]* -> skip ;
WS : [ \t\r\n]+ -> skip;

Numeral :  ([1-9][0-9]* | [0]) ;

Decimal : Numeral '.' '0'* Numeral ;  // FIX: disallow whitespace
BoolConst : 'true' | 'false' ;
HexConst : '#x' [0-9a-fA-F]+ ;
BinConst : '#b' [0-1]+ ;
StringConst : '"' ( ~('"') )* '"' ; // FIX: double quotes. No escape should be allowed

Symbol : [a-zA-Z_+*&|!~<>=/%?.$^-][a-zA-Z0-9_+*&|!~<>=/%?.$^-]* ;
