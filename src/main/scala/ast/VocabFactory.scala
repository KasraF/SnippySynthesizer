package ast

import execution.Types.{Types,Any}

trait VocabMaker {
  val arity: Int
  val childTypes: List[Types]
  val returnType: Types
  def canMake(children: List[ASTNode]): Boolean = children.length == arity && children.zip(childTypes).forall(pair => execution.Types.subclassEq(pair._1.nodeType,pair._2))
  def apply(children: List[ASTNode]): ASTNode
}

object VocabMaker {
  def apply(termDesc: String): VocabMaker = {
    val line = termDesc.split('|').map(_.trim)
    assert(line.length >= 3)
    val retType = if (line.length > 3) execution.Types.withName(line(3)) else Any
    val _arity = line(1).toInt
    val childrenTypes = line.drop(4).map(t => execution.Types.withName(t)).padTo(_arity,Any).toList
    line(0) match {
      case "Literal" => {
        assert(_arity == 0)
        new VocabMaker {
          override def apply(children: List[ASTNode]): ASTNode = {
            assert(children.isEmpty)
            new Literal(line(2),returnType)
          }
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
          override val returnType: Types = retType
        }
      }
      case "Variable" => {
        assert(_arity == 0)
        new VocabMaker {
          override def apply(children: List[ASTNode]): ASTNode = {
            assert(children.isEmpty)
            new Variable(line(2),returnType)
          }
          override val arity: Int = 0
          override val childTypes: List[Types] = Nil
          override val returnType: Types = retType
        }
      }
      case "BinOperator" => {
        assert(_arity == 2)
        new VocabMaker {
          override def apply(children: List[ASTNode]): ASTNode = {
            assert(canMake(children))
            new BinOperator(line(2),children(0),children(1),returnType)
          }
          override val arity: Int = 2
          override val childTypes: List[Types] = childrenTypes
          override val returnType: Types = retType
        }
      }
      case "FunctionCall" => new VocabMaker {
        val arity = _arity
        override def apply(children: List[ASTNode]): ASTNode = {
          assert(canMake(children))
          new FunctionCall(line(2), arity,children,returnType)
        }
        override val childTypes: List[Types] = childrenTypes
        override val returnType: Types = retType
      }
      case "MethodCall" => new VocabMaker {
        assert(_arity > 0)
        override val arity: Int = _arity
        override val childTypes: List[Types] = childrenTypes
        override val returnType: Types = retType

        override def apply(children: List[ASTNode]): ASTNode = {
          assert(canMake(children))
          new MethodCall(line(2),arity,children(0),children.tail,returnType)
        }
      }
      case "RandomAccess" => new VocabMaker {
        assert(_arity == 2)
        override val arity: Int = _arity
        override val childTypes: List[Types] = childrenTypes
        override val returnType: Types = retType

        override def apply(children: List[ASTNode]): ASTNode = {
          assert(canMake(children))
          new RandomAccess(children(0),children(1),returnType)
        }
      }
      case "Slicing" => new VocabMaker {
        assert(_arity == 3)
        override val arity: Int = 3
        override val childTypes: List[Types] = childrenTypes
        override val returnType: Types = retType

        override def apply(children: List[ASTNode]): ASTNode = {
          assert(canMake(children))
          new Slicing(children(0),children(1),children(2),returnType)
        }
      }
    }
  }
}

class VocabFactory(val leavesMakers: List[VocabMaker], val nodeMakers: List[VocabMaker]) {
  def leaves(): Iterator[VocabMaker] = leavesMakers.iterator
  def nonLeaves(): Iterator[VocabMaker] = nodeMakers.iterator
}

object VocabFactory{
  def apply(vocabString: String): VocabFactory = this(vocabString.lines.map(l => VocabMaker(l)).toList)
  def apply(vocabMakers: Seq[VocabMaker]): VocabFactory = {
    val (leavesMakers, nodeMakers) = vocabMakers.toList.partition(m => m.arity == 0)
    new VocabFactory(leavesMakers,nodeMakers)
  }
}
