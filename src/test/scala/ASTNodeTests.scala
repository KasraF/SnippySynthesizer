import ast._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite
import org.junit.Assert._

class ASTNodeTests extends JUnitSuite
{
	@Test def stringLiteralNode(): Unit =
	{
		val stringLiteral: StringNode = new StringLiteral("abc", 1)
		assertEquals(1, stringLiteral.values.length)
		assertEquals("abc", stringLiteral.values(0))
		assertEquals(Types.String, stringLiteral.nodeType)
		assertEquals("\"abc\"", stringLiteral.code)
		assertEquals(0, stringLiteral.height)
		assertEquals(1, stringLiteral.terms)
		assertTrue(stringLiteral.children.isEmpty)
	}
}