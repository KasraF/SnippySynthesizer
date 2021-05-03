package edu.ucsd.snippy

import edu.ucsd.snippy.ast._

object PostProcessor
{
	def clean(node: ASTNode): ASTNode = if (!node.usesVariables && node.values.toSet.size == 1) //second check is a tad redundant but just to be safe
		Types.typeof(node.values.head.get) match {
			case Types.String => StringLiteral(node.values.head.get.asInstanceOf[String], node.values.length)
			case Types.Bool => BoolLiteral(node.values.head.get.asInstanceOf[Boolean], node.values.length)
			case Types.Int => IntLiteral(node.values.head.get.asInstanceOf[Int], node.values.length)
			case _ => node
		}
	else node match {
		case add: IntAddition =>
			val lhs: IntNode = clean(add.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(add.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => IntLiteral(a.value + b.value, a.values.length)
				case (a, b: NegateInt) => IntSubtraction(a, b.arg)
				case (a, b: IntLiteral) if b.value < 0 => IntSubtraction(a, IntLiteral(-b.value, b.values.length))
				case _ => IntAddition(lhs, rhs)
			}
		case sub: IntSubtraction =>
			val lhs: IntNode = clean(sub.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(sub.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => IntLiteral(a.value - b.value, a.values.length)
				case (a: IntLiteral, b) => if (a.value == 0) {
					NegateInt(b)
				} else {
					IntSubtraction(lhs, rhs)
				}
				case _ => IntSubtraction(lhs, rhs)
			}
		case sub: IntDivision =>
			val lhs: IntNode = clean(sub.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(sub.rhs).asInstanceOf[IntNode]

			(lhs, rhs) match {
				case (a: IntLiteral, b: IntLiteral) => IntLiteral(a.value / b.value, a.values.length)
				case _ => IntDivision(lhs, rhs)
			}
		case concat: StringConcat =>
			val lhs: StringNode = clean(concat.lhs).asInstanceOf[StringNode]
			val rhs: StringNode = clean(concat.rhs).asInstanceOf[StringNode]
			(lhs, rhs) match {
				case (a: StringLiteral, b: StringLiteral) => StringLiteral(a.value + b.value, a.values.length)
				case _ => StringConcat(lhs, rhs)
			}
		case gt: GreaterThan =>
			val lhs: IntNode = clean(gt.lhs).asInstanceOf[IntNode]
			val rhs: IntNode = clean(gt.rhs).asInstanceOf[IntNode]
			if (lhs == rhs)
				BoolLiteral(false,gt.lhs.values.length)
			else (lhs,rhs) match {
				case (a: IntLiteral, b:IntLiteral) => BoolLiteral(a.value > b.value, a.values.length)
				case _ => GreaterThan(lhs,rhs)
			}
		case gt: GreaterThanDoubles =>
			val lhs: DoubleNode = clean(gt.lhs).asInstanceOf[DoubleNode]
			val rhs: DoubleNode = clean(gt.rhs).asInstanceOf[DoubleNode]
			if (lhs == rhs)
				BoolLiteral(false,gt.lhs.values.length)
			else (lhs,rhs) match {
				case (a: DoubleLiteral, b:DoubleLiteral) => BoolLiteral(a.value > b.value, a.values.length)
				case _ => GreaterThanDoubles(lhs,rhs)
			}
		case NegateBool(NegateBool(inner)) =>
			// Double negation!
			clean(inner)
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
		case map: MapCompNode[a, b] =>
			val list = clean(map.list)
			val key = clean(map.key)
			val value = clean(map.value)

			map.list.nodeType match {
				case Types.String =>
					map.value.nodeType match {
						case Types.String =>
							StringStringMapCompNode(
								list.asInstanceOf[StringNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[StringNode],
								map.varName)
						case Types.Int =>
							StringIntMapCompNode(
								list.asInstanceOf[StringNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[IntNode],
								map.varName)
					}
				case Types.StringList =>
					map.value.nodeType match {
						case Types.String =>
							StringListStringMapCompNode(
								list.asInstanceOf[StringListNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[StringNode],
								map.varName)
						case Types.Int =>
							StringListIntMapCompNode(
								list.asInstanceOf[StringListNode],
								key.asInstanceOf[StringNode],
								value.asInstanceOf[IntNode],
								map.varName)
					}
				case Types.IntList =>
					map.value.nodeType match {
						case Types.String =>
							IntStringMapCompNode(
								list.asInstanceOf[IntListNode],
								key.asInstanceOf[IntNode],
								value.asInstanceOf[StringNode],
								map.varName)
						case Types.Int =>
							IntIntMapCompNode(
								list.asInstanceOf[IntListNode],
								key.asInstanceOf[IntNode],
								value.asInstanceOf[IntNode],
								map.varName)
					}
			}
		case map: FilteredMapNode[a, b] =>
			val mapNode: MapNode[a, b] = clean(map.map).asInstanceOf[MapNode[a, b]]
			val filter: BoolNode = clean(map.filter).asInstanceOf[BoolNode]
			map.make(mapNode, filter, map.keyName)
		case n => n
	}
}
