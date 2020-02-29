package sygus

import ast.{ASTNode, BinaryOpNode, IntAddition, IntLiteral, IntNode, IntSubtraction}

object PostProcessor
{
 def clean(node: ASTNode): ASTNode = node match {
		case add: IntAddition =>
			val lhs: IntNode = clean(add.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(add.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => new IntLiteral(a.value + b.value, 1)
				case _ => new IntAddition(lhs, rhs)
			}
		case sub: IntSubtraction =>
			val lhs: IntNode = clean(sub.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(sub.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => new IntLiteral(a.value - b.value, 1)
				case _ => new IntAddition(lhs, rhs)
			}
		case bin: BinaryOpNode[Int] =>
			val lhs: ASTNode = clean(bin.lhs)
			val rhs: ASTNode = clean(bin.rhs)
			bin.
			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => new IntLiteral(a.value - b.value, 1)
				case _ => new IntAddition(lhs, rhs)
			}
	}
}
