package info.dgjones.barnable.grammar

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.ConceptMatcher
import info.dgjones.barnable.concept.Slot
import info.dgjones.barnable.concept.matchConceptByHead
import info.dgjones.barnable.parser.*


enum class Conjunction {
    And
}

fun withConjunctionObj(concept: Concept, conjunction: Concept) {
    concept.with(Slot("conjObj", conjunction))
}

fun buildConjunction(conjunction: String): Concept {
    return Concept("conjunction")
        .with(Slot("is", Concept(conjunction)))
}

fun matchConjunction(): ConceptMatcher {
    return matchConceptByHead(ParserKinds.Conjunction.name)
}

// Word Senses

fun buildGrammarConjunctionLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordAnd())
}

class WordAnd: WordHandler(EntryWord("and")) {
    override fun build(wordContext: WordContext): List<Demon> {
        // FIXME should be expanded upon
        wordContext.defHolder.value = buildConjunction(Conjunction.And.name)
        return listOf(IgnoreDemon(wordContext))
    }
}
