package info.dgjones.au.domain.general

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FoodTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Food Word Sense`() {
        val textProcessor = runTextProcess("Lobster", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val lobster = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(PhysicalObjectKind.PhysicalObject.name, lobster.name)
        Assertions.assertEquals(PhysicalObjectKind.Food.name, lobster.valueName(CoreFields.Kind))
        Assertions.assertEquals("lobster", lobster.valueName(CoreFields.Name))
    }
}