package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.vocab.VocabFactory

trait Enumerator extends Iterator[ASTNode]
{
	val vocab: VocabFactory
	val oeManager: OEValuesManager
	val contexts: List[Map[String, Any]]
}