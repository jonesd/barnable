package org.dgjones.au.parser

enum class ParserKinds {
    Conjunction
}

enum class SL {
    PrepObject
}

/* Prepositions */

enum class Preposition {
    In,
    Into,
    On,
    To,
    With
}

fun withPrepObj(concept: Concept, prep: Concept) {
    concept.with(Slot(SL.PrepObject.name, prep))
}
fun buildPrep(preposition: String): Concept {
    return Concept("prep")
        .with(Slot("is", Concept(preposition)))
}

fun matchPrepIn(preps: Collection<String>): ConceptMatcher {
    return { c -> preps.contains(c?.value(SL.PrepObject)?.valueName("is")) }
}

fun LexicalConceptBuilder.expectPrep(slotName: String, variableName: String? = null, preps: Collection<Preposition>, matcher: ConceptMatcher, direction: SearchDirection = SearchDirection.After) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val matchers = matchAll(listOf(
        matchPrepIn(preps.map { it.name }),
        matcher
    ))
    val demon = PrepDemon(matchers, SearchDirection.After, root.wordContext) {
        root.completeVariable(variableSlot, it)
    }
    root.addDemon(demon)
}

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
        var defHolder = wordContext.context.defHolderAtWordIndex(index)
        var value = defHolder.value
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
    lexicon.addMapping(WordIgnore(EntryWord("a").and("an")))
    lexicon.addMapping(WordIgnore(EntryWord("the")))
}
class WordAnd(): WordHandler(EntryWord("and")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildConjunction(Conjunction.And.name)
        return listOf(IgnoreDemon(wordContext))
    }
}

class WordIt(): WordHandler(EntryWord("it")) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(FindObjectReferenceDemon(wordContext))
    }
}

/* Suffix Daemons */

fun buildSuffixDemon(suffix: String, wordContext: WordContext): Demon? {
    when (suffix) {
        "ed" -> return SuffixEdDemon(wordContext)
        "s" -> return SuffixSDemon(wordContext)
        else -> return null
    }
}

class SuffixEdDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null) {
            if (def.value("time") == null) {
                def.value("time", Concept("past"))
                active = false
            }
        }
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
}