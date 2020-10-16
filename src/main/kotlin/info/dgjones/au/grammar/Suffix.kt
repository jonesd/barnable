package info.dgjones.au.grammar

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.Slot
import info.dgjones.au.domain.general.*
import info.dgjones.au.parser.*

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
        return "Suffix ED marks word sense as in the past"
    }
}

class SuffixSDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null) {
            if (def.value(GroupFields.GroupInstances) == null) {
                def.value(GroupFields.GroupInstances, Concept(GroupConcept.`*multiple*`.name))
                active = false
            }
        }
    }
    override fun description(): String {
        return "Suffix S marks word sense as being multiple"
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
        return "Suffix ING mark words sense as progressive when following be"
    }
}

// FIXME find a more useful location fo formsOfBe
private val formsOfBe = setOf("be", "am", "are", "is", "was", "were", "being", "been")

