package info.dgjones.au.concept

import info.dgjones.au.domain.general.Gender
import info.dgjones.au.domain.general.HumanFields
import info.dgjones.au.domain.general.buildHuman
import info.dgjones.au.episodic.EpisodicConcept
import info.dgjones.au.narrative.MopMealFields
import info.dgjones.au.parser.*

/* DSL to help build Concept as part of a word sense.
It also triggers demons to populate the concept slots.
 */

class LexicalRootBuilder(val wordContext: WordContext, val headName: String) {
    val root = LexicalConceptBuilder(this, headName)

    val demons = mutableListOf<Demon>()
    val variableSlots = mutableListOf<Slot>()
    val completedSlots = mutableListOf<Slot>()
    val completedConceptHolders = mutableListOf<ConceptHolder>()
    val disambiguations = mutableListOf<Demon>()
    var totalSuccessfulDisambiguations = 0

    fun build(): LexicalConcept {
        return LexicalConcept(wordContext, root.build(), demons, disambiguations)
    }

    // FIXME shouldn't associate this state that lives on beyond build() to builder
    // should create new holder instead
    fun createVariable(slotName: Fields, variableName: String? = null): Slot {
        return createVariable(slotName.fieldName, variableName)
    }
    fun createVariable(slotName: String, variableName: String? = null): Slot {
        val nameOrNextVariableNumber = variableName ?: wordContext.context.workingMemory.nextVariableIndex().toString()
        val conceptVariable = createConceptVariable(nameOrNextVariableNumber)
        val slot = Slot(slotName, conceptVariable)
        variableSlots.add(slot)
        return slot
    }
    fun completeVariable(variableSlot: Slot, value: Concept, wordContext: WordContext, episodicConcept: EpisodicConcept? = null) {
        val conceptHolder = wordContext.context.workingMemory.createDefHolder(value)
        completeVariable(variableSlot, conceptHolder, episodicConcept)
    }
    fun completeVariable(variableSlot: Slot, valueHolder: ConceptHolder, episodicConcept: EpisodicConcept? = null) {
        if (!variableSlot.isVariable()) return
        val variableName = variableSlot.value?.name ?: return
        completedConceptHolders.add(valueHolder)
        val completeVariableSlots = variableSlots.filter { it.value?.name == variableName }
        //FIXME can multiple demons update the same value here?
        completeVariableSlots.forEach {
            it.value = valueHolder.value
            // FIXME better way to triggerd updating - spawn demon instead
            episodicConcept?.let { episodicConcept ->
                // FIXME not sure about this qa hack?
                if (!wordContext.context.qaMode) {
                    wordContext.context.episodicMemory.episodicRoleCheck(episodicConcept, it)
                }
            }
        }
        completedSlots.addAll(completeVariableSlots)
        variableSlots.removeAll(completeVariableSlots)
        //if (variableSlots.isEmpty()) {
            completedConceptHolders.forEach { it.addFlag(ParserFlags.Inside)  }
        //}
    }
    fun disambiguationResult(result: Boolean) {
        this.totalSuccessfulDisambiguations += 1
    }
    fun addDemon(demon: Demon) {
        this.demons.add(demon)
    }
    fun addDisambiguationDemon(demon: Demon) {
        this.disambiguations.add(demon)
    }
}

/*class LexicalConceptDisambiguateBuilder(val root: LexicalRootBuilder) {
    fun disambiguateUsingWord(word: String, heads: List<String>, direction: SearchDirection = SearchDirection.After) {
        val demon = DisambiguateUsingWord(word, matchConceptByHead(heads), direction, root.wordContext) {
            root.disambiguationResult(true)
        }
        root.addDisambiguationDemon(demon)
    }
    fun disambiguate(head: String, direction: SearchDirection = SearchDirection.After) {
        disambiguate(listOf(head), direction)
    }
    fun disambiguate(heads: List<String>, direction: SearchDirection = SearchDirection.After) {
        val demon = DisambiguateUsingMatch(matchConceptByHead(heads), direction, root.wordContext) {
            root.disambiguationResult(true)

        }
        root.addDisambiguationDemon(demon)
    }
}*/

class LexicalConceptBuilder(val root: LexicalRootBuilder, conceptName: String) {
    val concept = Concept(conceptName)
    var episodicConcept: EpisodicConcept? = null

    fun ignoreHolder() {
        root.wordContext.defHolder.addFlag(ParserFlags.Ignore)
    }

    fun slot(slotName: Fields, slotValue: String) {
        slot(slotName.fieldName, slotValue)
    }
    fun slot(slotName: String, slotValue: String) {
        concept.with(Slot(slotName, Concept(slotValue)))
    }
    fun slot(slotName: Fields, slotValue: String, initializer: LexicalConceptBuilder.() -> Unit) {
        slot(slotName.fieldName, slotValue, initializer)
    }
    fun slot(slotName: String, slotValue: String, initializer: LexicalConceptBuilder.() -> Unit) {
        val child = LexicalConceptBuilder(root, slotValue)
        child.apply(initializer)
        val c = child.build()
        concept.with(Slot(slotName, c))
    }
    fun build(): Concept {
        return concept
    }
    fun expectHead(slotName: String, variableName: String? = null, headValue: String, direction: SearchDirection = SearchDirection.After) {
        expectHead(slotName, variableName, listOf(headValue), direction)
    }
    fun expectHead(slotName: String, variableName: String? = null, headValues: List<String>, direction: SearchDirection = SearchDirection.After) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = ExpectDemon(matchConceptByHead(headValues), direction, root.wordContext) {
            root.completeVariable(variableSlot, it, this.episodicConcept)
        }
        root.addDemon(demon)
    }

    /**
     * General find a concept from the concept sentence and extract out the specified concept value
     */
    fun expectConcept(slotName: String, variableName: String? = null, conceptField: String? = null, matcher: ConceptMatcher, direction: SearchDirection = SearchDirection.After) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = ExpectDemon(matcher, direction, root.wordContext) {
            if (conceptField != null) {
                // FIXME support sub-concept matching
                val value = it.value?.value(conceptField)
                // FIXME will never complete if value initially empty...
                if (value != null) {
                    root.completeVariable(variableSlot, value, wordContext = root.wordContext, this.episodicConcept)
                    it.addFlag(ParserFlags.Inside)
                }
            } else {
                root.completeVariable(variableSlot, it, this.episodicConcept)
            }
        }
        root.addDemon(demon)
    }
    fun expectKind(slotName: String, variableName: String? = null, kinds: List<String>, direction: SearchDirection = SearchDirection.After) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = ExpectDemon(matchConceptByKind(kinds), direction, root.wordContext) {
            root.completeVariable(variableSlot, it, this.episodicConcept)
        }
        root.addDemon(demon)
    }
    fun expectActor(slotName: String, variableName: String? = null) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = ExpectActor(root.wordContext) {
            root.completeVariable(variableSlot, it, this.episodicConcept)
        }
        root.addDemon(demon)
    }

    fun expectThing(slotName: String, variableName: String? = null, headValues: List<String>) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = ExpectThing(headValues,root.wordContext) {
            root.completeVariable(variableSlot, it, this.episodicConcept)
        }
        root.addDemon(demon)
    }

    fun saveAsObject() {
        root.addDemon(SaveObjectDemon(root.wordContext))
    }

    // Find matching human in episodic memory, and associate concept
    // InDepth p185
    fun checkCharacter(slotName: String, variableName: String? = null) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val checkCharacterDemon = CheckCharacterDemon(concept, root.wordContext) { episodicCharacter ->
            if (episodicCharacter != null) {
                val episodicInstance = episodicCharacter.value(CoreFields.INSTANCE)
                root.completeVariable(variableSlot, root.wordContext.context.workingMemory.createDefHolder(episodicInstance))
                this.episodicConcept = episodicCharacter
                copyCompletedSlot(HumanFields.FIRST_NAME, episodicCharacter, concept)
                copyCompletedSlot(HumanFields.LAST_NAME, episodicCharacter, concept)
                copyCompletedSlot(HumanFields.GENDER, episodicCharacter, concept)
                root.wordContext.context.workingMemory.markAsRecentCharacter(concept)
            } else {
                println("Creating character ${concept.valueName(HumanFields.FIRST_NAME)} in EP memory")
                val human = buildHuman(concept.valueName(HumanFields.FIRST_NAME), concept.valueName(HumanFields.LAST_NAME), concept.valueName(HumanFields.GENDER))
                // val saveCharacterDemon = SaveCharacterDemon(root.wordContext)
                root.wordContext.context.workingMemory.markAsRecentCharacter(concept)
                //root.wordContext.context.episodicMemory.addConcept(human)
            }
        }
        root.addDemon(checkCharacterDemon)
    }

    fun checkMop(slotName: String, variableName: String? = null) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val checkMopDemon = CheckMopDemon(concept, root.wordContext) { episodicMop ->
            if (episodicMop != null) {
                val episodicInstance = episodicMop.value(CoreFields.INSTANCE)
                root.completeVariable(variableSlot, root.wordContext.context.workingMemory.createDefHolder(episodicInstance))
                this.episodicConcept = episodicMop
                copyCompletedSlot(MopMealFields.EATER_A, episodicMop, concept)
                copyCompletedSlot(MopMealFields.EATER_B, episodicMop, concept)
                copyCompletedSlot(CoreFields.Event, episodicMop, concept)
//            } else {
//                println("Creating mop ${concept.name} in EP memory")
//                val human = buildHuman(concept.valueName("firstName"), concept.valueName("lastName"), concept.valueName("gender"))
//                // val saveCharacterDemon = SaveCharacterDemon(root.wordContext)
//                root.wordContext.context.episodicMemory.addConcept(human)
            }
        }
        root.addDemon(checkMopDemon)
        // checkOrCreateMop
    }

    // Find recent character with matching gender in Working Memory
    // May be replaced by better value using knowledge layer
    // InDepth p185
    fun findCharacter(slotName: String, variableName: String? = null) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = FindCharacterDemon(concept.valueName(HumanFields.GENDER), root.wordContext) {
            if (it != null) {
                root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
            }
        }
        root.addDemon(demon)
    }

    fun possessiveRef(slotName: Fields, variableName: String? = null, gender: Gender) {
        val variableSlot = root.createVariable(slotName.fieldName, variableName)
        concept.with(variableSlot)
        val demon = PossessiveReference(gender, root.wordContext) {
            if (it != null) {
                root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
            }
        }
        root.addDemon(demon)
    }

    fun nextChar(slotName: String, variableName: String? = null, relRole: String? = null) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = NextCharacterDemon(root.wordContext) {
            if (it != null) {
                root.completeVariable(variableSlot, it, this.episodicConcept)
            }
        }
        root.addDemon(demon)
    }

    fun innerInstan(slotName: String, variableName: String? = null, observeSlot: String) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = InnerInstanceDemon(observeSlot, root.wordContext) {
            if (it != null) {
                root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
            }
        }
        root.addDemon(demon)
    }

    // InDepth p185
    fun checkRelationship(slotName: Fields, variableName: String? = null, waitForSlots: List<String>) {
        val variableSlot = root.createVariable(slotName.fieldName, variableName)
        concept.with(variableSlot)
        val demon = CheckRelationshipDemon(concept, waitForSlots, root.wordContext) {
            if (it != null) {
                root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
            }
        }
        root.addDemon(demon)
    }

    fun varReference(slotName: String, variableName: String) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
    }
}

fun lexicalConcept(wordContext: WordContext, headName: String, initializer: LexicalConceptBuilder.() -> Unit): LexicalConcept {
    val builder = LexicalRootBuilder(wordContext, headName)
    builder.root.apply(initializer)
    return builder.build()
}

class LexicalRoot(val wordContext: WordContext, val head: Concept)

class LexicalConcept(val wordContext: WordContext, val head: Concept, val demons: List<Demon>, val disambiguateDemons: List<Demon>) {
    init {
        wordContext.defHolder.value = head
    }
}
