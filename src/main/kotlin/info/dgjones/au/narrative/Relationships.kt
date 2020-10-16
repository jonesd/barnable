package info.dgjones.au.narrative

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.concept.Fields
import info.dgjones.au.concept.lexicalConcept
import info.dgjones.au.domain.general.Gender
import info.dgjones.au.domain.general.Human
import info.dgjones.au.parser.*

enum class Relationships(override val fieldName: String): Fields {
    Name("Relationship")
}

enum class Marriage(override val fieldName: String): Fields {
    Concept("R-Marriage"),
    Wife("wife"),
    Husband("husband")
}

// Word Sense

fun buildNarrativeRelationshipLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordHusband())
    lexicon.addMapping(WordWife())
}

class WordWife: WordHandler(EntryWord("wife")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(Human.GENDER, Gender.Female.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Husband, gender = Gender.Male)
                nextChar("wife", relRole = "Wife")
                checkRelationship(CoreFields.INSTANCE, waitForSlots = listOf("husband", "wife"))
            }
            innerInstan("instan", observeSlot = "wife")
        }
        return lexicalConcept.demons
    }
}

class WordHusband: WordHandler(EntryWord("husband")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(Human.GENDER, Gender.Male.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Wife, gender = Gender.Female)
                nextChar("husband", relRole = "Husband")
                checkRelationship(CoreFields.INSTANCE, waitForSlots = listOf("husband", "wife"))
            }
            innerInstan("instan", observeSlot = "husband")
        }
        return lexicalConcept.demons
    }
}