package sygus

import ast._

object PostProcessor
{
 def clean(node: ASTNode): ASTNode = if (!node.usesVariables && node.values.toSet.size == 1) //second check is a tad redundant but just to be safe
		Types.typeof(node.values(0)) match {
				case Types.String => new StringLiteral(node.values(0).asInstanceOf[String],node.values.length)
				case Types.Bool => new BoolLiteral(node.values(0).asInstanceOf[Boolean],node.values.length)
				case Types.Int => new IntLiteral(node.values(0).asInstanceOf[Int],node.values.length)
				case _ => node
		}
 	else node match {
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
			val list = clean(map.list)
			val key = clean(map.key)
			val value = clean(map.value)

			map.list.nodeType match {
				case Types.String =>
					map.value.nodeType match {
						case Types.String =>
							new StringStringMapCompNode(
								list.asInstanceOf[StringNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[StringNode],
								map.varName)
						case Types.Int =>
							new StringIntMapCompNode(
								list.asInstanceOf[StringNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[IntNode],
								map.varName)
					}
				case Types.StringList =>
					map.value.nodeType match {
						case Types.String =>
							new StringListStringMapCompNode(
								list.asInstanceOf[StringListNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[StringNode],
								map.varName)
						case Types.Int =>
							new StringListIntMapCompNode(
								list.asInstanceOf[StringListNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[IntNode],
								map.varName)
					}
				case Types.IntList =>
					map.value.nodeType match {
						case Types.String =>
							new IntStringMapCompNode(
								list.asInstanceOf[IntListNode],
								key.asInstanceOf[IntNode],
								value.asInstanceOf[StringNode],
								map.varName)
						case Types.Int =>
							new IntIntMapCompNode(
								list.asInstanceOf[IntListNode],
								key.asInstanceOf[IntNode],
								value.asInstanceOf[IntNode],
								map.varName)
					}
			}
		case map: FilteredMapNode[a,b] =>
			val mapNode: MapNode[a,b] = clean(map.map).asInstanceOf[MapNode[a,b]]
			val filter: BoolNode = clean(map.filter).asInstanceOf[BoolNode]
			map.make(mapNode, filter, map.keyName)
		case n => n
	}
}
