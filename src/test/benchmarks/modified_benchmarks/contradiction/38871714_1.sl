; https=//stackoverflow.com/questions/38871714/excel-vba-how-to-remove-substrings-from-a-cell
(set-logic SLIA)
(synth-fun f ((_arg_0 String)) String 
 ( (Start String (ntString)) 
 (ntString String (
	_arg_0
	"" " " "<" ">"
	(str.++ ntString ntString) 
	(str.replace ntString ntString ntString) 
	(str.at ntString ntInt)
	(int.to.str ntInt)
	(ite ntBool ntString ntString)
	(str.substr ntString ntInt ntInt)
)) 
 (ntInt Int (
	
	1 0 -1
	(+ ntInt ntInt)
	(- ntInt ntInt)
	(str.len ntString)
	(str.to.int ntString)
	(ite ntBool ntInt ntInt)
	(str.indexof ntString ntString ntInt)
)) 
 (ntBool Bool (
	
	true false
	(= ntInt ntInt)
	(str.prefixof ntString ntString)
	(str.suffixof ntString ntString)
	(str.contains ntString ntString)
)) ))
(constraint (= (f "This is a <string>, It should be <changed> to <a> number.") "This is a string, It should be changed to a number."))
(constraint (= (f "a < 4 and a > 0") "a  4 and a  0"))
(constraint (= (f "a < 4 and a > 0 and a < 12") "a 4 and a  0 and a 12"))
(check-synth)
