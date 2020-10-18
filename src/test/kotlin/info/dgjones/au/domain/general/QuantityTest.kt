package info.dgjones.au.domain.general

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class QuantityTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Measure Word Sense - Two measures of sugar`() {
        val textProcessor = runTextProcess("Two measures of sugar", lexicon)

        // FIXME should this be Food sugar with amount, rather than Quantity of sugar?
        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val quantity = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(QuantityConcept.Quantity.name, quantity.name)
        Assertions.assertEquals("2", quantity.value(QuantityFields.Amount)?.valueName(NumberFields.Value))
        Assertions.assertEquals("Measure", quantity.valueName(QuantityFields.Unit))
        Assertions.assertEquals("sugar", quantity.value(QuantityFields.Of)?.valueName(CoreFields.Name))
    }
}