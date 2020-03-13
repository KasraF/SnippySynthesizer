package sygus

import ast._

object PostProcessor
{
 def clean(node: ASTNode): ASTNode = node match {
		case add: IntAddition =>
			val lhs: IntNode = clean(add.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(add.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => new IntLiteral(a.value + b.value, a.values.length)
				case _ => new IntAddition(lhs, rhs)
			}
		case sub: IntSubtraction =>
			val lhs: IntNode = clean(sub.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(sub.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => new IntLiteral(a.value - b.value, a.values.length)
				case _ => new IntSubtraction(lhs, rhs)
			}
		case sub: IntDivision =>
			val lhs: IntNode = clean(sub.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(sub.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => new IntLiteral(a.value / b.value, a.values.length)
				case _ => new IntDivision(lhs, rhs)
			}
		case concat: StringConcat =>
			val lhs: StringNode = clean(concat.lhs).asInstanceOf[StringNode]
			val rhs: StringNode = clean(concat.rhs).asInstanceOf[StringNode]
			(lhs, rhs) match {
				case (a: StringLiteral, b: StringLiteral) => new StringLiteral(a.value + b.value, a.values.length)
				case _ => new StringConcat(lhs, rhs)
			}
		case uni: UnaryOpNode[_] =>
			val arg = clean(uni.arg)
			uni.make(arg)
		case bin: BinaryOpNode[_] =>
			val lhs: ASTNode = clean(bin.lhs)
			val rhs: ASTNode = clean(bin.rhs)
			bin.make(lhs, rhs)
		case ter: TernaryOpNode[_] =>
			val arg0: ASTNode = clean(ter.arg0)
			val arg1: ASTNode = clean(ter.arg1)
			val arg2: ASTNode = clean(ter.arg2)
			ter.make(arg0, arg1, arg2)
		case qua: QuaternaryOpNode[_] =>
			val arg0: ASTNode = clean(qua.arg0)
			val arg1: ASTNode = clean(qua.arg1)
			val arg2: ASTNode = clean(qua.arg2)
			val arg3: ASTNode = clean(qua.arg3)
			qua.make(arg0, arg1, arg2, arg3)
		case map: MapCompNode[a,b] =>
			// TODO Fix the types
			val list: StringNode = clean(map.list).asInstanceOf[StringNode]
			val key: StringNode = clean(map.key).asInstanceOf[StringNode]
			val value: IntNode = clean(map.value).asInstanceOf[IntNode]
			new StringIntMapCompNode(list, key, value, map.varName)
		case map: FilteredMapNode[a,b] =>
			// TODO Fix the types
			val mapNode: MapNode[a,b] = clean(map.map).asInstanceOf[MapNode[a,b]]
			val filter: BoolNode = clean(map.filter).asInstanceOf[BoolNode]
			map.make(mapNode, filter, map.keyName)
		case n => n
	}
}
