package ast

import execution.Types.{Types,Any}

trait VocabMaker {
  val arity: Int
  val childTypes: List[Types]
  def apply(children: List[ASTNode]): ASTNode
}

object VocabMaker {
  def apply(termDesc: String): VocabMaker = {
    val line = termDesc.trim.split('|')
    assert(line.length >= 3)
    line(0) match {
      case "Literal" => {
        assert(line(1) == "0")
        new VocabMaker {
          override def apply(children: List[ASTNode]): ASTNode = {
            assert(children.isEmpty)
            new Literal(line(2))
          }
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
        }
      }
      case "Variable" => {
        assert(line(1) == "0")
        new VocabMaker {
          override def apply(children: List[ASTNode]): ASTNode = {
            assert(children.isEmpty)
            new Variable(line(2))
          }
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
        }
      }
      case "BinOperator" => {
        assert(line(1) == "2")
        new VocabMaker {
          override def apply(children: List[ASTNode]): ASTNode = {
            assert(children.length == 2)
            new BinOperator(line(2),children(0),children(1))
          }
          override val arity: Int = 2
          override val childTypes: List[Types] = List(Any,Any)
        }
      }
      case "FunctionCall" => new VocabMaker {
        val arity = line(1).toInt
        override def apply(children: List[ASTNode]): ASTNode = {
          assert(arity == children.length)
          new FunctionCall(line(2), arity,children)
        }
        override val childTypes: List[Types] = List.fill(arity)(Any)
      }
    }
  }
}

class VocabFactory(val leavesMakers: List[VocabMaker], val nodeMakers: List[VocabMaker]) {
  def leaves(): Iterator[VocabMaker] = leavesMakers.iterator
  def nonLeaves(): Iterator[VocabMaker] = nodeMakers.iterator
}

object VocabFactory{
  def apply(vocabString: String): VocabFactory = {
    val (leavesMakers, nodeMakers) = vocabString.lines.map(l => VocabMaker(l)).toList.partition(m => m.arity == 0)
    new VocabFactory(leavesMakers,nodeMakers)
  }
}
