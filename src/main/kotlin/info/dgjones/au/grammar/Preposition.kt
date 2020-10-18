package info.dgjones.au.grammar

import info.dgjones.au.concept.*
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

/*
"A word, normally non-inflecting, that is typically employed to connect a following noun or pronoun, in an adjectival or
adverbial sense, with some other word. Examples of prepositions in English are in, from and during."
https://en.wiktionary.org/wiki/Appendix:Glossary#preposition
 */
enum class PrepositionConcept {
    Preposition
}

enum class Preposition {
    By,
    In,
    Into,
    On,
    To,
    With
}

fun buildPrep(preposition: Preposition): Concept {
    val concept = Concept(PrepositionConcept.Preposition.name)
    withPreposition(concept, preposition)
    return concept
}

fun withPreposition(concept: Concept, preposition: Preposition) {
    return withPreposition(concept, preposition.name)
}

fun withPreposition(concept: Concept, preposition: String) {
    concept.with(Slot("is", Concept(preposition)))
}

fun matchPrepIn(preps: Collection<String>): ConceptMatcher {
    return { c -> preps.contains(c?.valueName("is")) }
}

fun LexicalConceptBuilder.expectPrep(slotName: String, variableName: String? = null, preps: Collection<Preposition>, matcher: ConceptMatcher, direction: SearchDirection = SearchDirection.After) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val matchers = matchAll(
        listOf(
        matchPrepIn(preps.map { it.name }),
        matcher
    )
    )
    val demon = PrepDemon(matchers, SearchDirection.After, root.wordContext) {
        root.completeVariable(variableSlot, it, this.episodicConcept)
    }
    root.addDemon(demon)
}

// Word Senses

fun buildGrammarPropositionLexicon(lexicon: Lexicon) {
    // FIXME InDepth p304 "with" also needs to "determine social activity"
    lexicon.addMapping(PrepositionWord(Preposition.With, setOf(InDepthUnderstandingConcepts.Human.name)))
    lexicon.addMapping(PrepositionWord(Preposition.In, setOf(InDepthUnderstandingConcepts.PhysicalObject.name, InDepthUnderstandingConcepts.Setting.name)))
    lexicon.addMapping(PrepositionWord(Preposition.By, setOf(InDepthUnderstandingConcepts.Setting.name)))
}

/*
A preposition word will mark a matched target word as being connected to the preposition. This can then be matched
This target can then be matched by another word searching for a preposition.
For example: John had lunch with George
 - With word will mark George as being related to the With preposition
 - Lunch looks for a following Human related to a With preposition
 */
class PrepositionWord(val preposition: Preposition, val matchConcepts: Set<String>): WordHandler(EntryWord(preposition.name.toLowerCase())) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildPrep(preposition)
        wordContext.defHolder.addFlag(ParserFlags.Ignore)

        val matcher = matchConceptByHead(matchConcepts)
        val addPrepObj = InsertAfterDemon(matcher, wordContext) {
            if (wordContext.isDefSet()) {
                val itValue = it.value
                val holderValue = wordContext.defHolder.value
                if (itValue != null && holderValue != null) {
                    withPreposition(itValue, preposition)
                    wordContext.defHolder.addFlag(ParserFlags.Inside)
                    println("Updated with preposition=$preposition concept=${it}")
                }
            }
        }
        return listOf(addPrepObj)
    }
}

// Demons

class PrepDemon(val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    var found: ConceptHolder? = null

    override fun run() {
        if (direction == SearchDirection.Before) {
            (wordContext.wordIndex - 1 downTo 0).forEach {
                found = updateFrom(found, wordContext, it)
            }
        } else {
            (wordContext.wordIndex + 1 until wordContext.context.wordContexts.size).forEach {
                found = updateFrom(found, wordContext, it)
            }
        }
        val foundConcept = found
        if (foundConcept?.value != null) {
            action(foundConcept)
            println("Prep found concept=$foundConcept for match=$matcher")
            active = false
        }
    }

    private fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing != null) {
            return existing
        }
        val defHolder = wordContext.context.defHolderAtWordIndex(index)
        val value = defHolder.value
        // if (isConjunction(value)) {
        //    return null
        //}
        if (matcher(value)) {
            return defHolder
        }
        return null
    }

    // private fun isConjunction(concept: Concept?): Boolean {
    //    return matchConceptByKind(ParserKinds.Conjunction.name)(concept)
    //}

    override fun description(): String {
        return "PrepDemon $matcher"
    }
}