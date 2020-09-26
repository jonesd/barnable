package info.dgjones.au.grammar

import info.dgjones.au.concept.Fields
import info.dgjones.au.parser.*

enum class GrammarFields(override val fieldName: String): Fields {
    Aspect("aspect"),
    Case("case"),
    Voice("voice")
}

enum class ParserKinds {
    Conjunction
}

enum class Case {
    Subjective,
    Objective,
    Possessive,
    Vocative
}

enum class Aspect {
    Progressive
}

enum class Voice {
    // https://en.wiktionary.org/wiki/active_voice#English
    // Fred kicked the ball.
    Active,

    // https://en.wiktionary.org/wiki/passive_voice#English
    // The ball was kicked by Fred
    Passive
}

/* Prepositions */

/* Conjunctions */


/* Word senses */

fun buildEnglishGrammarLexicon(lexicon: Lexicon) {
    buildGrammarConjunctionLexicon(lexicon)
    buildGrammarPropositionLexicon(lexicon)

    lexicon.addMapping(WordIt())
    lexicon.addMapping(WordHave())
    lexicon.addMapping(WordIgnore(EntryWord("a").and("an")))
    lexicon.addMapping(WordIgnore(EntryWord("the")))
}

class WordIt: WordHandler(EntryWord("it")) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(FindObjectReferenceDemon(wordContext))
    }
}

class WordHave: WordHandler(EntryWord("have")) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("FIXME implement have")
        return super.build(wordContext)
    }
    //FIXME InDepth pp303 - having?
}
