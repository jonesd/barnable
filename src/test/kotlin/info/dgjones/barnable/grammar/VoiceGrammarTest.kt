package info.dgjones.barnable.grammar

import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.domain.general.Acts
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.parser.runTextProcess
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