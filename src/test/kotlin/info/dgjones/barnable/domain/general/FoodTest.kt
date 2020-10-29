package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
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