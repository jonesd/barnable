package info.dgjones.au.parser

import info.dgjones.au.domain.general.TimeConcepts
import info.dgjones.au.domain.general.TimeFields
import info.dgjones.au.grammar.buildGrammarPropositionLexicon
import info.dgjones.au.narrative.InDepthUnderstandingConcepts

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

/* Common Words */

fun buildEnglishGrammarLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordAnd())
    lexicon.addMapping(WordIt())
    lexicon.addMapping(WordHave())
    lexicon.addMapping(WordIgnore(EntryWord("a").and("an")))
    lexicon.addMapping(WordIgnore(EntryWord("the")))
    buildGrammarPropositionLexicon(lexicon)
}

class WordAnd: WordHandler(EntryWord("and")) {
    override fun build(wordContext: WordContext): List<Demon> {
        // FIXME should be expanded upon
        wordContext.defHolder.value = buildConjunction(Conjunction.And.name)
        return listOf(IgnoreDemon(wordContext))
    }
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

/* Suffix Daemons */

fun buildSuffixDemon(suffix: String, wordContext: WordContext): Demon? {
    return when (suffix) {
        "ed" -> SuffixEdDemon(wordContext)
        "s" -> SuffixSDemon(wordContext)
        "ing" -> SuffixIngDemon(wordContext)
        //FIXME implement other suffixes....
        else -> null
    }
}

class SuffixEdDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null) {
            if (def.value(TimeFields.TIME) == null) {
                def.value(TimeFields.TIME.fieldName, Concept(TimeConcepts.Past.name))
                active = false
            }
        }
    }

    override fun description(): String {
        return "Suffix ED"
    }
}

class SuffixSDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null) {
            if (def.value("group-instances") == null) {
                def.value("group-instances", Concept("*multiple*"))
                active = false
            }
        }
    }
    override fun description(): String {
        return "Suffix S"
    }
}

class SuffixIngDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        // Progressive Detection
        val previousWord = wordContext.previousWord()
        if (previousWord != null && formsOfBe.contains(previousWord.toLowerCase())) {
            def?.let {
                it.with(Slot(GrammarFields.Aspect, Concept(Aspect.Progressive.name)) )
                active = false
            }
        } else {
            active = false
        }
    }
    override fun description(): String {
        return "Suffix ING"
    }
}

private val formsOfBe = setOf("be", "am", "are", "is", "was", "were", "being", "been")
