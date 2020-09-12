package org.dgjones.au.parser

import org.dgjones.au.narrative.HumanAccessor
import org.dgjones.au.narrative.InDepthUnderstandingConcepts

class ExpectDemon(val matcher: ConceptMatcher, val direction: SearchDirection, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
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
            println("Found concept=$foundConcept for match=$matcher")
            active = false
        }
    }

    private fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing != null) {
            return existing
        }
        val defHolder = wordContext.context.defHolderAtWordIndex(index)
        val value = defHolder.value
        if (isConjunction(value)) {
            return null
        }
        if (matcher(value)) {
            return defHolder
        }
        return null
    }

    private fun isConjunction(concept: Concept?): Boolean {
        return matchConceptByHead(ParserKinds.Conjunction.name)(concept)
    }

    override fun description(): String {
        return "ExpectDemon $direction $action"
    }
}

/* Find matching character in episodic memory. Only runs once */
class CheckCharacterDemon(val human: Concept, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val matchedCharacter = wordContext.context.episodicMemory.checkOrCreateCharacter(human)
        action(matchedCharacter.value(CoreFields.INSTANCE))
        active = false
    }
    override fun description(): String {
        return "CheckCharacter from episodic with ${human.valueName(Human.FIRST_NAME)} ${human.valueName(Human.GENDER)}"
    }
}

// Search working memory for tentative character based on gender
// InDepth p182
class FindCharacterDemon(val gender: String?, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        if (gender != null) {
            val matchedCharacter = wordContext.context.workingMemory.findCharacterByGender(gender)
            action(matchedCharacter)
            active = false
        }
    }

    override fun description(): String {
        return "FindCharacter from working memory gender=$gender"
    }
}

class FindObjectReferenceDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        if (!wordContext.isDefSet()) {
            wordContext.defHolder.value = wordContext.context.mostRecentObject
            println("updated word=${wordContext.word} to def=${wordContext.def()}")
        }
        if (wordContext.isDefSet()) {
            active = false
        }
    }
}

class SaveCharacterDemon(wordContext: WordContext): Demon(wordContext){
    override fun run() {
        val character = wordContext.def()
        if (character != null && HumanAccessor(character).isCompatible()) {
            wordContext.context.workingMemory.markAsRecentCharacter(character)
//            wordContext.context.mostRecentCharacter = character
//            if (wordContext.context.localCharacter != null) {
//                wordContext.context.localCharacter = character
//            }
            active = false
        } else {
            println("SaveCharacter failed as def = $character")
        }
    }

    override fun description(): String {
        return "SaveCharacter ${wordContext.def()}"
    }
}

class SaveObjectDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val o = wordContext.def()
        if (o != null) {
            wordContext.context.mostRecentObject = o
            active = false
        }
    }

    override fun description(): String {
        return "SaveObject def=${wordContext.def()}"
    }
}

class PossessiveReference(val gender: Gender, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext( matchCase(Case.Possessive), direction = SearchDirection.Before, wordContext = wordContext) {
            val instan = it.value?.value("instan")
            action(instan)
            if (instan != null) {
                active = false
            }
        }
    }

    override fun description(): String {
        return "Poss-Ref for $gender"
    }
}

/* Assign the Instance slot value of the resolved concept */
class InnerInstanceDemon(val slotName: String, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    var conceptAccessor: ConceptSlotAccessor? = null
    override fun run() {
        wordContext.defHolder.value?.let { rootConcept ->
            conceptPathResolvedValue(rootConcept, slotName)?.let { parentConcept ->
                val instanceConcept = parentConcept.value(CoreFields.INSTANCE)
                if (isConceptResolved(instanceConcept)) {
                    active = false
                    action(instanceConcept)
                }
            }
        }
    }

    override fun description(): String {
        return "When the inner ${CoreFields.INSTANCE} of $slotName has been bound\nThen use it to bind the demon's role"
    }
}

class CheckRelationshipDemon(var parent: Concept, var dependentSlotNames: List<String>, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val rootConcept =  wordContext.defHolder.value
        if (rootConcept != null) {
            if (isDependentSlotsComplete(parent, dependentSlotNames)) {
                var instance = checkEpisodicRelationship(parent, wordContext.context.episodicMemory)
                active = false
                action(Concept(instance))
            }
        }
    }
    override fun description(): String {
        return "CheckRelationship waitingFor $dependentSlotNames"
    }
}

fun isDependentSlotsComplete(parent: Concept, childrenSlotNames: List<String>): Boolean {
    // FIXME PERFORMACE try and cache the conceptAccessors
    return childrenSlotNames.all { conceptPathResolvedValue(parent, it) != null }
}

fun conceptPathResolvedValue(parent: Concept?, slotName: String): Concept? {
    parent?.let {
        buildConceptPathAccessor(parent, slotName)?.invoke()?.let {concept ->
            if (isConceptResolved(concept)) {
                return concept
            }
        }
    }
    return null
}

//FIXME implement more....
// InDepth p185
fun checkEpisodicRelationship(parent: Concept, episodicMemory: EpisodicMemory): String {
    val episodicInstance = episodicMemory.checkOrCreateRelationship(parent)
    // FIXME also assume new relationship
    return episodicInstance
}

class InsertAfterDemon(val matcher: ConceptMatcher, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
            if (it.value != null) {
                active = false
                action(it)
            }
        }
    }

    override fun description(): String {
        return "InsertAfter $matcher"
    }
}

class NextCharacterDemon(wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        val matcher = matchConceptByHead(InDepthUnderstandingConcepts.Human.name)
        searchContext(matcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
            if (it.value != null) {
                active = false
                action(it)
            }
            //FIXME also look for unattached Human preceedes
        }
    }

    override fun description(): String {
        return "NextCharacter"
    }
}