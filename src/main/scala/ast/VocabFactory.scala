package ast

trait VocabMaker {
  val arity: Int
  def apply(children: List[ASTNode]): ASTNode
}

object VocabMaker {
  def apply(termDesc: String): VocabMaker = {
    val line = termDesc.trim.split('|')
    assert(line.length == 3)
    line(0) match {
      case "Literal" => {
        assert(line(1) == "0")
        new VocabMaker {
          override def apply(children: List[ASTNode]): ASTNode = {
            assert(children.isEmpty)
            new Literal(line(2))
          }
          override val arity: Int = 0
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
        }
      }
      case "FunctionCall" => new VocabMaker {
        val arity = line(1).toInt
        override def apply(children: List[ASTNode]): ASTNode = {
          assert(arity == children.length)
          new FunctionCall(line(2), arity,children)
        }
      }
    }
  }
}

class VocabFactory(vocabString: String) {
  val leavesMakers = vocabString.lines.map(l => VocabMaker(l)).filter(m => m.arity == 0).toList

  def leaves(): Iterator[VocabMaker] = leavesMakers.iterator
}
