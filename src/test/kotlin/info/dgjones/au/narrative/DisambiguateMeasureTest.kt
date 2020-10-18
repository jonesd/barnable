package info.dgjones.au.narrative

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.domain.general.*
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DisambiguateMeasureTest {
    private val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Two measures of sugar`() {
        val textProcessor = runTextProcess("Two measures of sugar", lexicon)

        // FIXME think this should be Food sugar with amount, rather than Quantity of sugar
        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val quantity = textProcessor.workingMemory.concepts[0]
        assertEquals(QuantityConcept.Quantity.name, quantity.name)
        assertEquals("2", quantity.value(QuantityFields.Amount)?.valueName(NumberFields.Value))
        assertEquals("sugar", quantity.value(QuantityFields.Of)?.valueName(CoreFields.Name))
    }

    @Test
    fun `John measures the tree`() {
        val textProcessor = runTextProcess("John measures the tree", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val toMeasure = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.ATRANS.name, toMeasure.name)
        assertEquals("John", toMeasure.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("tree", toMeasure.value(ActFields.Thing)?.valueName(CoreFields.Name))
    }
}