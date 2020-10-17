package info.dgjones.au.narrative

import info.dgjones.au.domain.general.Gender
import info.dgjones.au.domain.general.HumanConcept
import info.dgjones.au.domain.general.HumanFields
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class RelationshipsTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `John and his wife Ann`() {
        val textProcessor = runTextProcess("John and his wife Ann.", lexicon)

        // FIXME implement
        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val john = textProcessor.workingMemory.concepts[0]
        assertEquals(HumanConcept.Human.name, john.name)
        assertEquals("John", john.valueName(HumanFields.FIRST_NAME))

        val wife = textProcessor.workingMemory.concepts[1]
        assertEquals(HumanConcept.Human.name, wife.name)
        val marriage = wife.value(Relationships.Name)
        assertNotNull(marriage)
        assertEquals(Gender.Male.name, marriage?.value(Marriage.Husband)?.valueName(HumanFields.GENDER))
        assertEquals("Ann", marriage?.value(Marriage.Wife)?.valueName(HumanFields.FIRST_NAME))
        assertEquals(Gender.Female.name, marriage?.value(Marriage.Wife)?.valueName(HumanFields.GENDER))
    }

    @Test
    fun `Ann and her husband John`() {
        val textProcessor = runTextProcess("Ann and her husband John.", lexicon)

        // FIXME implement
        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val ann = textProcessor.workingMemory.concepts[0]
        assertEquals(HumanConcept.Human.name, ann.name)
        assertEquals("Ann", ann.valueName(HumanFields.FIRST_NAME))

        val husband = textProcessor.workingMemory.concepts[1]
        assertEquals(HumanConcept.Human.name, husband.name)
        val marriage = husband.value(Relationships.Name)
        assertNotNull(marriage)
        assertEquals(Gender.Female.name, marriage?.value(Marriage.Wife)?.valueName(HumanFields.GENDER))
        assertEquals("John", marriage?.value(Marriage.Husband)?.valueName(HumanFields.FIRST_NAME))
        assertEquals(Gender.Male.name, marriage?.value(Marriage.Husband)?.valueName(HumanFields.GENDER))
    }
}