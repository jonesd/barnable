package info.dgjones.au.grammar

import info.dgjones.au.domain.general.HumanFields
import info.dgjones.au.domain.general.Acts
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.concept.Concept
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShouldMapPassiveAndActiveToSameConceptsTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Active Voice `() {
        val textProcessor = runTextProcess("Fred kicked the ball.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val propel = textProcessor.workingMemory.concepts[0]
        verifyFredKickedBall(propel)
    }

    @Test
    fun `Passive Voice - The ball was kicked by Fred`() {
        val textProcessor = runTextProcess("The ball was kicked by Fred.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val propel = textProcessor.workingMemory.concepts[0]
        verifyFredKickedBall(propel)
    }

    private fun verifyFredKickedBall(propel: Concept) {
        assertEquals(Acts.PROPEL.name, propel.name)
        assertEquals("Fred", propel.value("actor")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("ball", propel.value("thing")?.valueName("name"))
    }
}