package edu.ucsd.snippy.vocab

import edu.ucsd.snippy.ast.{ASTNode, BoolLiteral, BoolVariable, IntLiteral, IntVariable, StringLiteral, StringVariable}
import edu.ucsd.snippy.ast.Types.Types
import edu.ucsd.snippy.enumeration.ProbUpdate

import scala.collection.mutable

class VocabFactory(
  val leavesMakers: List[VocabMaker],
  val nodeMakers  : List[VocabMaker])
{
	def leaves(): Iterator[VocabMaker] = leavesMakers.iterator

	def nonLeaves(): Iterator[VocabMaker] = nodeMakers.iterator
}

object VocabFactory
{
	def apply(vocabMakers: Seq[VocabMaker]): VocabFactory =
	{
		val (leavesMakers, nodeMakers) = vocabMakers.toList.partition(m => m.arity == 0)
		new VocabFactory(leavesMakers, nodeMakers)
	}
}


trait VocabMaker
{
	val arity: Int
	val childTypes: List[Types]
	val nodeType: Class[_ <: ASTNode]
	val head: String
	val returnType: Types

	def apply(children: List[ASTNode], contexts: List[Map[String,Any]]): ASTNode
	def canMake(children: List[ASTNode]): Boolean = children.length == arity && children.zip(childTypes).forall(pair => pair._1.nodeType == pair._2)

	def rootCost: Int =
		if (nodeType == classOf[IntLiteral] ||
			nodeType == classOf[StringLiteral] ||
			nodeType == classOf[BoolLiteral] ||
			nodeType == classOf[StringVariable] ||
			nodeType == classOf[BoolVariable] ||
			nodeType == classOf[IntVariable])
		ProbUpdate.priors(nodeType, Some(head)) else ProbUpdate.priors(nodeType, None)

	def init(programs: List[ASTNode], contexts : List[Map[String, Any]], vocabFactory: VocabFactory, height: Int) : Iterator[ASTNode]
	def probe_init(vocabFactory: VocabFactory,
	               costLevel: Int, contexts: List[Map[String,Any]],
	               bank: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]],
	               nested: Boolean,
	               miniBank: mutable.Map[(Class[_], ASTNode), mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]],
	               mini: mutable.Map[Int, mutable.ArrayBuffer[ASTNode]]) : Iterator[ASTNode]
}