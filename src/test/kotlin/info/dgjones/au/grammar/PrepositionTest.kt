package info.dgjones.au.grammar

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.domain.general.*
import info.dgjones.au.narrative.MopMeal
import info.dgjones.au.narrative.MopMealFields
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PrepositionTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Builds preposition with word sense`() {
        val concept = buildPrep(Preposition.With)

        assertEquals(PrepositionConcept.Preposition.name, concept.name)
        assertEquals(Preposition.With.name, concept.valueName(CoreFields.Is))
    }

    @Test
    fun `With preposition matches human`() {
        val textProcessor = runTextProcess("Fred had lunch with George.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val lunch = textProcessor.workingMemory.concepts[0]
        assertEquals(MopMeal.MopMeal.name, lunch.name)

        assertEquals("Fred", lunch.value(MopMealFields.EATER_A)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("George", lunch.value(MopMealFields.EATER_B)?.valueName(HumanFields.FIRST_NAME))
    }

    @Test
    fun `With preposition does not match non-human`() {
        val textProcessor = runTextProcess("Fred had lunch with a book.", lexicon)

        assertEquals(2, textProcessor.workingMemory.concepts.size)
        val lunch = textProcessor.workingMemory.concepts[0]
        assertEquals(MopMeal.MopMeal.name, lunch.name)

        assertEquals("Fred", lunch.value(MopMealFields.EATER_A)?.valueName(HumanFields.FIRST_NAME))
        assertNull(lunch.value(MopMealFields.EATER_B))

        val book = textProcessor.workingMemory.concepts[1]
        assertEquals("book", book.valueName(CoreFields.Name))
    }
}
