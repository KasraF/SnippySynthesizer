import java.io.File

import edu.ucsd.snippy.{PostProcessor, Snippy}
import edu.ucsd.snippy.ast._
import edu.ucsd.snippy.utils.{ConditionalAssignment, MultilineMultivariableAssignment, SingleAssignment}
import org.junit.Assert._
import org.junit.Test
import org.scalatestplus.junit.JUnitSuite


class PostprocessorTests extends JUnitSuite
{
	@Test def constantFoldIntOperation: Unit =
	{
		val expr = IntAddition(
			IntSubtraction(
				IntDivision(IntLiteral(2, 1), IntLiteral(3, 1)),
				IntLiteral(-1, 1)), IntLiteral(8, 1)
			)
		val postProcessed = PostProcessor.clean(expr)
		assertEquals("9", postProcessed.code)
	}

	@Test def constantFoldIntOperationOneVar1: Unit =
	{
		val x = IntVariable("x", Map("x" -> 2) :: Nil)
		val expr = IntAddition(
			IntSubtraction(
				IntDivision(x, IntLiteral(3, 1)),
				IntLiteral(-1, 1)
				),
			IntLiteral(8, 1)
			)
		val postProcessed = PostProcessor.clean(expr)
		assertEquals("x // 3 - -1 + 8", postProcessed.code)
	}

	@Test def constantFoldIntOperationOneVar2: Unit =
	{
		val x = IntVariable("x", Map("x" -> 2) :: Nil)
		val expr = IntAddition(
			IntSubtraction(
				IntDivision(IntLiteral(2, 1), x),
				IntLiteral(-1, 1)
				),
			IntLiteral(8, 1)
			)
		val postProcessed = PostProcessor.clean(expr)
		assertEquals("2 // x - -1 + 8", postProcessed.code)
	}

	@Test def constantFoldIntOperationOneVar3: Unit =
	{
		val x = IntVariable("x", Map("x" -> 2) :: Nil)
		val expr = IntAddition(
			IntSubtraction(
				IntDivision(IntLiteral(2, 1), IntLiteral(3, 1)),
				x
				), IntLiteral(8, 1)
			)
		val postProcessed = PostProcessor.clean(expr)
		assertEquals("-x + 8", postProcessed.code)
	}

	@Test def constantFoldIntOperationOneVar4: Unit =
	{
		val x = IntVariable("x", Map("x" -> 2) :: Nil)
		val expr = IntAddition(
			IntSubtraction(
				IntDivision(IntLiteral(2, 1), IntLiteral(3, 1)),
				IntLiteral(-1, 1)
				), x
			)
		val postProcessed = PostProcessor.clean(expr)
		assertEquals("1 + x", postProcessed.code)
	}

	@Test def constantFoldStringToInt: Unit =
	{
		val expr = IntAddition(
			IntSubtraction(
				IntDivision(IntLiteral(2, 1), IntLiteral(3, 1)),
				IntLiteral(-1, 1)
				),
			StringToInt(StringLiteral("8", 1))
			)
		val postProcessed = PostProcessor.clean(expr)
		assertEquals("9", postProcessed.code)
	}

	@Test def constantFoldingStrings1: Unit =
	{
		val expr = IntToString(IntLiteral(-8, 1))
		assertEquals("\"-8\"", PostProcessor.clean(expr).code)
	}

	@Test def constantFoldingStrings1WithVar: Unit =
	{
		val x = IntVariable("x", Map("x" -> -8) :: Nil)
		val expr = IntToString(x)
		assertEquals("str(x)", PostProcessor.clean(expr).code)
	}

	@Test def constantFoldingStrings2: Unit =
	{
		val expr = Find(StringLiteral("", 1), StringLiteral(" ", 1))
		assertEquals("-1", PostProcessor.clean(expr).code)
	}

	@Test def constantFoldingStrings2Var: Unit =
	{
		val x = StringVariable("x", Map("x" -> "") :: Nil)
		val expr = Find(x, StringLiteral(" ", 1))
		assertEquals("x.find(\" \")", PostProcessor.clean(expr).code)
		val expr2 = Find(StringLiteral("", 1), x)
		assertEquals("\"\".find(x)", PostProcessor.clean(expr2).code)
	}

	@Test def constantFoldingStrings3: Unit =
	{
		val expr = StringConcat(
			BinarySubstring(
				StringLiteral("abc", 1),
				IntLiteral(1, 1)
				),
			TernarySubstring(
				StringLiteral("abcde", 1),
				IntLiteral(1, 1),
				IntSubtraction(Length(StringLiteral("abcde", 1)), IntLiteral(2, 1))
				)
			)
		assertEquals("\"bbc\"", PostProcessor.clean(expr).code)
	}

	@Test def constantFoldingBoolean1: Unit =
	{
		val expr = LessThanEq(IntLiteral(1, 1), IntLiteral(2, 1))
		assertEquals("True", PostProcessor.clean(expr).code)
		val expr2 = GreaterThan(IntLiteral(1, 1), IntLiteral(2, 1))
		assertEquals("False", PostProcessor.clean(expr2).code)
	}

	@Test def constantFoldingBoolean1WithVar: Unit =
	{
		val n = IntVariable("n", Map("n" -> 2) :: Nil)
		val expr = LessThanEq(n, IntLiteral(2, 1))
		assertEquals("n <= 2", PostProcessor.clean(expr).code)
		val expr2 = GreaterThan(IntLiteral(1, 1), n)
		assertEquals("1 > n", PostProcessor.clean(expr2).code)
	}

	@Test def constantFoldingBoolean2: Unit =
	{
		val expr = Contains(StringLiteral("abc", 1), StringLiteral("", 1))
		assertEquals("False", PostProcessor.clean(expr).code)
	}

	@Test def multivarSamePrefixBug: Unit = {
		//Snippy.synthesize(new File("C:\\Users\\peleg\\code\\PythonSynthesizer\\src\\test\\resources\\user_study_candidate_benchmarks\\increment_3.examples.json"),Int.MaxValue)
		//if d > digits[carry]:
		//			v = carry + d
		//			rs += [0]
		//		else:
		//			v = carry + d
		//			carry = carry * 0
		//			rs = [v] + rs
		val ctx = Map("d" -> 0, "rs" -> Nil, "carry" -> 0, "digits" -> List(0,3)) ::
			Map("d" -> 0, "rs" -> Nil, "carry" -> 0, "digits" -> List(-1,3)) :: Nil
		val d = VariableNode.nodeFromType("d",Types.Int,ctx).get.asInstanceOf[IntNode]
		val carry = VariableNode.nodeFromType("carry",Types.Int,ctx).get.asInstanceOf[IntNode]
		val rs = VariableNode.nodeFromType("rs", Types.IntList, ctx).get.asInstanceOf[ListVariable[Int]]
		val v = VariableNode.nodeFromType("v",Types.Int,ctx).get.asInstanceOf[IntNode]
		val conditionalAssignment = ConditionalAssignment(
			NegateBool(GreaterThan(d,
			IntListLookup(VariableNode.nodeFromType("digits",Types.IntList,ctx).get.asInstanceOf[ListVariable[Int]],
				carry))),
			MultilineMultivariableAssignment(
				SingleAssignment("v", IntAddition(carry,d))
				:: SingleAssignment("carry", IntMultiply(carry, IntLiteral(0, ctx.length)))
				:: SingleAssignment("rs", ListPrepend(v,rs))
				:: Nil
			),
			MultilineMultivariableAssignment(
				SingleAssignment("carry", carry)
				:: SingleAssignment("v", IntAddition(carry,d))
				:: SingleAssignment("rs", ListAppend(rs,IntLiteral(0,ctx.length)))
				:: Nil
			)
		)
		assertEquals("""v = carry + d
					   |if d > digits[carry]:
					   |	rs += [0]
					   |else:
					   |	carry = carry * 0
					   |	rs = [v] + rs""".stripMargin.replace("\r",""), conditionalAssignment.code())
	}
}
