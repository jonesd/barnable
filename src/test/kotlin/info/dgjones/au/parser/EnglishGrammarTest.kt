package info.dgjones.au.parser

import info.dgjones.au.narrative.Acts
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShouldMapPassiveAndActiveToSameConcepts {
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
        assertEquals("Fred", propel.value("actor")?.valueName(Human.FIRST_NAME))
        assertEquals("ball", propel.value("thing")?.valueName("name"))
    }
}