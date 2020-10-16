package info.dgjones.au.episodic

import info.dgjones.au.concept.*
import info.dgjones.au.domain.general.Human
import info.dgjones.au.domain.general.RoleThemeFields
import info.dgjones.au.domain.general.characterMatcher
import info.dgjones.au.domain.general.humanKeyValue
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.narrative.Marriage
import info.dgjones.au.narrative.MopMeal
import info.dgjones.au.narrative.MopMealFields

typealias EpisodicInstance = String

typealias EpisodicConcept = Concept

typealias EpisodicConceptMap = MutableMap<EpisodicInstance, EpisodicConcept>

class IndexedNameGenerator {
    val nextIndexes = mutableMapOf<String, Int>()

    fun episodicId(name: String): String {
        val index = nextIndexes.getOrDefault(name, 0)
        nextIndexes[name] = index + 1
        return name + index
    }

    fun episodicId(concept: Concept): String = episodicId(concept.name)
}


class EpisodicMemory {
    val indexGenerator = IndexedNameGenerator()

    val relationships = mutableMapOf<EpisodicInstance, EpisodicConcept>()
    val characters = mutableMapOf<EpisodicInstance, EpisodicConcept>()
    val mops = mutableMapOf<EpisodicInstance, EpisodicConcept>()
    val events = mutableMapOf<EpisodicInstance, EpisodicConcept>()
    val roleThemes = mutableMapOf<EpisodicInstance, EpisodicConcept>()

    val scenarioMap = ScenarioMap()

    val concepts = mutableListOf<EpisodicConcept>()

    fun addConcept(c: EpisodicConcept) {
        recentUseOf(c)
    }

    fun search(matcher: ConceptMatcher): EpisodicConcept? {
        val found = concepts.firstOrNull { matcher(it) }
        if (found != null) {
            recentUseOf(found)
        }
        return found
    }

    private fun recentUseOf(c: EpisodicConcept) {
        concepts.remove(c)
        concepts.add(0, c)
    }

    fun concepts(): List<EpisodicConcept> {
        return concepts.toList()
    }

    override fun toString(): String {
        return "EpisodicMemory $concepts"
    }

    // FIXME other kind of elements
    // FIXME Recency for access - should move used element to head of list
    // FIXME use a linked list

    fun initializeCharacterSlotFrom(concept: Concept, field: Fields, episodicConcept: EpisodicConcept) {
        val conceptValue = concept.value(field)
        val episodicValue = if (isConceptResolved(conceptValue)) checkOrCreateCharacter(conceptValue) else null
        println("EP Character ${field.fieldName} <== $episodicValue")
        episodicConcept.with(Slot(field, episodicValue))
    }

    fun initializeEventSlotFrom(concept: Concept, field: Fields, episodicConcept: EpisodicConcept) {
        val conceptValue = concept.value(field)
        // FIXME should we be creating event instances? val episodicValue =  checkOrCreateEvent(if (isConceptResolved(conceptValue)) conceptValue else null)
        val episodicValue =  if (isConceptResolved(conceptValue)) conceptValue else null
        println("EP Event ${field.fieldName} <== $episodicValue")
        episodicConcept.with(Slot(field, episodicValue))
    }

    // FIXME support multiple Mops of the same type!
    fun findExistingEpisodicMop(concept: Concept?): EpisodicConcept? {
        concept?.name?.let { conceptHead ->
            return mops.values.firstOrNull(matchConceptByHead(conceptHead))
        }
        return null
    }

    fun createBareEpisodicConceptFrom(concept: Concept, map: EpisodicConceptMap): EpisodicConcept {
        val episodicConcept = EpisodicConcept(concept.name)
        val episodicId = indexGenerator.episodicId(concept)
        episodicConcept.with(Slot(CoreFields.INSTANCE, EpisodicConcept(episodicId)))
        map[episodicId] = episodicConcept
        return episodicConcept
    }

    fun checkOrCreateRelationship(concept: Concept): EpisodicInstance {
        // FIXME always assume it is new
        // FIXME should be general - not marriage...
        return checkOrCreateMarriage(concept)
    }

    private fun checkOrCreateMarriage(concept: Concept): EpisodicInstance {
        // FIXME always assume it is new
        val relationshipType = concept.name
        val relationship = EpisodicConcept(relationshipType)
        val episodicId = indexGenerator.episodicId(concept)
        relationship.with(Slot(Marriage.Wife, checkOrCreateCharacter(concept.value(Marriage.Wife))))
        relationship.with(Slot(Marriage.Husband, checkOrCreateCharacter(concept.value(Marriage.Husband))))
        relationship.with(Slot(CoreFields.INSTANCE, EpisodicConcept(episodicId)))
        relationships[episodicId] = relationship
        return episodicId
    }

    fun episodicRoleCheck(episodicConcept: EpisodicConcept, slotUpdate: Slot) {
        when (episodicConcept.name) {
            MopMeal.MopMeal.name -> updateMopMeal(episodicConcept, slotUpdate)
            InDepthUnderstandingConcepts.Human.name -> updateCharacter(episodicConcept, slotUpdate)
            else -> {
                println("EP WARNING! Unhandled episodicConcept update ${episodicConcept.name}")
            }
        }
    }

    fun updateMopMeal(episodicConcept: EpisodicConcept, slotUpdate: Slot) {
        //FIXME may need to use deepcopy...
        println("EP UpdateMopMeal ${episodicConcept.name} $slotUpdate")
        when (slotUpdate.name) {
            CoreFields.Event.fieldName -> slotUpdate.copyValue(episodicConcept)
            MopMealFields.EATER_A.fieldName -> episodicConcept.value(MopMealFields.EATER_A, checkOrCreateCharacter(slotUpdate.value))
            MopMealFields.EATER_B.fieldName -> episodicConcept.value(MopMealFields.EATER_B, checkOrCreateCharacter(slotUpdate.value))
            else -> println("EP UpdateMopMeal - no slot match for update $slotUpdate")
        }
    }

    fun updateCharacter(episodicConcept: EpisodicConcept, slotUpdate: Slot) {
        //FIXME may need to use deepcopy...
        when (slotUpdate.name) {
            Human.FIRST_NAME.fieldName -> slotUpdate.copyValue(episodicConcept)
            Human.LAST_NAME.fieldName -> slotUpdate.copyValue(episodicConcept)
            RoleThemeFields.RoleTheme.fieldName -> slotUpdate.copyValue(episodicConcept)
            Human.GENDER.fieldName -> slotUpdate.copyValue(episodicConcept)
            else -> println("EP UpdateCharacter - no slot match for update $slotUpdate")
        }
        }
    fun checkOrCreateCharacter(human: Concept?): EpisodicConcept {
        if (human != null) {
            val episodicCharacter = findEpisodicCharacter(human)
            if (episodicCharacter != null) {
                return episodicCharacter
            }
        }
        val character = EpisodicConcept(InDepthUnderstandingConcepts.Human.name)
        if (human != null) {
            character.with(human.duplicateResolvedSlot(Human.FIRST_NAME))
            character.with(human.duplicateResolvedSlot(Human.LAST_NAME))
            character.with(human.duplicateResolvedSlot(Human.GENDER))
            character.with(human.duplicateResolvedSlot(RoleThemeFields.RoleTheme))
        }
        val episodicInstance = indexGenerator.episodicId(humanKeyValue(character))
        character.with(Slot(CoreFields.INSTANCE, Concept(episodicInstance)))
        characters[episodicInstance] = character
        return character
    }

    private fun findEpisodicCharacter(human: Concept): EpisodicConcept? {
        val matcher = characterMatcher(human)
        return characters.values.firstOrNull {matcher(it)}
    }

    private fun nextEpisodicId(conceptType: String): String {
        return indexGenerator.episodicId(conceptType)
    }

    fun setCurrentEvent(event: Concept, mainEvent: Boolean = false) {
        scenarioMap.setCurrentEvent(event, mainEvent)
    }

    fun dumpMemory() {
        println("Episodic Characters = ${characters.values}")
        println("Episodic Relationships = ${relationships.values}")
        println("Episodic MOPs = ${mops.values}")
    }
}

data class EpisodicConceptHolder(val instance: EpisodicInstance, val concept: Concept)

