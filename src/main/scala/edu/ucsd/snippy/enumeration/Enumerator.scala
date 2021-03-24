package edu.ucsd.snippy.enumeration

import edu.ucsd.snippy.ast.ASTNode
import edu.ucsd.snippy.predicates.Predicate
import edu.ucsd.snippy.utils.Assignment
import edu.ucsd.snippy.vocab.VocabFactory

/**
 * This is the slightly-redesigned enumerator that is now interconnected
 * with the synthesis task predicate. While it still enumerates per-program,
 * it only returns *solutions*, hence the new iterator return type of
 * Option[String]. This allows any caller to run code between enumeration steps,
 * but they only have access to any full solutions rather than every program it
 * enumerates.
 */
trait Enumerator extends Iterator[ASTNode]
{
	val vocab: VocabFactory
	val oeManager: OEValuesManager
	val contexts: List[Map[String, Any]]
	def programsSeen: Int
}