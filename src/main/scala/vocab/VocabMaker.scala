package vocab

import ast.Types.Types
import ast.{ASTNode, IntVariable, StringVariable, Types}
import enumeration._

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
	def init(progs: List[ASTNode], contexts : List[Map[String, Any]], vocabFactory: VocabFactory, height: Int) : Iterator[ASTNode]
}