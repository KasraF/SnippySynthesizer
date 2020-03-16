import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite
import sygus.InputParser

class ParserTests extends JUnitSuite
{
	val parser = new InputParser

	// Basic types
	@Test def parseInt(): Unit = assertEquals(Some(123), parser.parse("123"))
	@Test def parseTrue(): Unit = assertEquals(Some(true), parser.parse("True"))
	@Test def parseFalse(): Unit = assertEquals(Some(false), parser.parse("False"))
	@Test def parseString(): Unit = assertEquals(Some("abc"), parser.parse("'abc'"))
	@Test def parseEmptyString(): Unit = assertEquals(Some(""), parser.parse("''"))
	@Test def parseStringDoubleQuotes(): Unit = assertEquals(Some("abc"), parser.parse("\"abc\""))
	@Test def parseStrings(): Unit = assertEquals(Some("abcdef"), parser.parse("'abc' 'def'"))

	// Lists
	@Test def parseIntList(): Unit = assertEquals(Some(List(123)), parser.parse("[123]"))
	@Test def parseStrList(): Unit = assertEquals(Some(List("abc")), parser.parse("['abc']"))
	@Test def parseBoolList(): Unit = assertEquals(Some(List(false)), parser.parse("[False]"))
	@Test def parseIntMultiList(): Unit = assertEquals(Some(List(1, 2, -3)), parser.parse("[1, 2, -3]"))
	@Test def parseStrMultiList(): Unit = assertEquals(Some(List("a", "b", "c")), parser.parse("['a', 'b', 'c']"))
	@Test def parseBoolMultiList(): Unit = assertEquals(Some(List(false, true)), parser.parse("[False, True]"))
	@Test def parseEmptyList(): Unit = assertEquals(Some(List()), parser.parse("[]"))
	@Test def parseIntBoolList(): Unit = assertEquals(None, parser.parse("[1, True]"))
	@Test def parseIntStrList(): Unit = assertEquals(None, parser.parse("[2, 'asd']"))

	// Maps
	@Test def parseStrStrMap(): Unit = assertEquals(Some(List("a" -> "a", "b" -> "b")), parser.parse("{'a': 'a', 'b': 'b'}"))
	@Test def parseStrIntMap(): Unit = assertEquals(Some(List("a" -> 0, "b" -> 1)), parser.parse("{'a': 0, 'b': 1}"))
	@Test def parseIntStrMap(): Unit = assertEquals(Some(List(0 -> "a", 1 -> "b")), parser.parse("{0: 'a', 1: 'b'}"))
	@Test def parseIntIntMap(): Unit = assertEquals(Some(List(0 -> 1, 1 -> 2)), parser.parse("{0: 1, 1: 2}"))
	@Test def parseExtraSpacesMap0(): Unit = assertEquals(Some(List(0 -> 1, 1 -> 2)), parser.parse(" {0: 1, 1: 2} "))
	@Test def parseExtraSpacesMap1(): Unit = assertEquals(Some(List(0 -> 1, 1 -> 2)), parser.parse("{0 : 1, 1 : 2}"))
	@Test def parseExtraSpacesMap2(): Unit = assertEquals(Some(List(0 -> 1, 1 -> 2)), parser.parse("{ 0: 1, 1: 2 }"))
	@Test def parseExtraSpacesMap3(): Unit = assertEquals(Some(List(0 -> 1, 1 -> 2)), parser.parse("{ 0 : 1 , 1 : 2 }"))
	@Test def parseExtraSpacesMap4(): Unit = assertEquals(Some(List(0 -> 1, 1 -> 2)), parser.parse("{0:1,1:2}"))
	@Test def parseEmptyMap(): Unit = assertEquals(Some(List()), parser.parse("{}"))
	@Test def parseBadValTypeMap(): Unit = assertEquals(None, parser.parse("{0: 'a', 1: 2}"))
	@Test def parseBadKeyTypeMap(): Unit = assertEquals(None, parser.parse("{0: 'a', 'a': 'b'}"))

	// TODO Sets
}
