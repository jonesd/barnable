package info.dgjones.au.narrative

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.domain.general.Gender
import info.dgjones.au.domain.general.Human
import info.dgjones.au.domain.general.RoleThemeFields
import info.dgjones.au.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RelationshipsTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `John and his wife Ann`() {
        val textProcessor = runTextProcess("John and his wife Ann.", lexicon)

        // FIXME implement
        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val john = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, john.name)
        assertEquals("John", john.valueName(Human.FIRST_NAME))

        val wife = textProcessor.workingMemory.concepts[1]
        assertEquals(Human.CONCEPT.fieldName, wife.name)
        val marriage = wife.value(Relationships.Name)
        assertNotNull(marriage)
        assertEquals(Gender.Male.name, marriage?.value(Marriage.Husband)?.valueName(Human.GENDER))
        assertEquals("Ann", marriage?.value(Marriage.Wife)?.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Female.name, marriage?.value(Marriage.Wife)?.valueName(Human.GENDER))
    }

    @Test
    fun `Ann and her husband John`() {
        val textProcessor = runTextProcess("Ann and her husband John.", lexicon)

        // FIXME implement
        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val ann = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, ann.name)
        assertEquals("Ann", ann.valueName(Human.FIRST_NAME))

        val husband = textProcessor.workingMemory.concepts[1]
        assertEquals(Human.CONCEPT.fieldName, husband.name)
        val marriage = husband.value(Relationships.Name)
        assertNotNull(marriage)
        assertEquals(Gender.Female.name, marriage?.value(Marriage.Wife)?.valueName(Human.GENDER))
        assertEquals("John", marriage?.value(Marriage.Husband)?.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Male.name, marriage?.value(Marriage.Husband)?.valueName(Human.GENDER))
    }
}