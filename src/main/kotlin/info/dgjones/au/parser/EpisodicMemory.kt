package info.dgjones.au.parser

import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.narrative.Marriage

typealias EpisodicInstance = String

typealias EpisodicConcept = Concept

class IndexedNameGenerator {
    val nextIndexes = mutableMapOf<String, Int>()

    fun episodicId(name: String): String {
        val index = nextIndexes.getOrDefault(name, 0)
        nextIndexes[name] = index + 1
        return name + index
    }

    fun episodicId(names: List<String?>): String {
        val name = names.firstOrNull { it != null && it.isNotEmpty() }
        return episodicId(name ?: "noname")
    }

    fun episodicId(concept: Concept): String = episodicId(concept.name)
}

class EpisodicMemory {
    val indexGenerator = IndexedNameGenerator()

    val relationships = mutableMapOf<EpisodicInstance, EpisodicConcept>()
    val characters = mutableMapOf<EpisodicInstance, Concept>()

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

    fun checkOrCreateCharacter(human: Concept?): EpisodicConcept {
        if (human != null) {
            val episodicCharacter = findEpisodicCharacter(human)
            if (episodicCharacter != null) {
                return episodicCharacter
            }
        }
        val character = EpisodicConcept(InDepthUnderstandingConcepts.Human.name)
        val episodicInstance = indexGenerator.episodicId(
            listOf(human?.valueName(Human.FIRST_NAME) , human?.valueName(Human.LAST_NAME), Human.CONCEPT.fieldName))
        if (human != null) {
            character.with(Slot(Human.FIRST_NAME, Concept(human.valueName(Human.FIRST_NAME, ""))))
            character.with(Slot(Human.LAST_NAME, Concept(human.valueName(Human.LAST_NAME, ""))))
            character.with(Slot(Human.GENDER, Concept(human.valueName(Human.GENDER, ""))))
            character.with(Slot(CoreFields.INSTANCE, Concept(episodicInstance)))
        }
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

    fun dumpMemory() {
        println("Episodic Characters = ${characters.values}")
        println("Episodic Relationships = ${relationships.values}")
    }
}

data class EpisodicConceptHolder(val instance: EpisodicInstance, val concept: Concept)

