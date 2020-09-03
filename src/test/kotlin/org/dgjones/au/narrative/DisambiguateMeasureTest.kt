package org.dgjones.au.narrative

import org.dgjones.au.nlp.NaiveTextModelBuilder
import org.dgjones.au.parser.TextProcessor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DisambiguateMeasureTest {
    private val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Two measures of sugar`() {
        val textModel = NaiveTextModelBuilder("Two measures of sugar").buildModel()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        // FIXME think this should be Food sugar with amount, rather than Quantity of sugar
        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val quantity = textProcessor.workingMemory.concepts[0]
        assertEquals("Quantity", quantity.name)
        assertEquals("2", quantity.value("amount")?.valueName("value"))
        assertEquals("Food", quantity.valueName("of"))
        assertEquals("Sugar", quantity.value("of")?.valueName("kind"))
    }

    @Test
    fun `John measures the tree`() {
        val textModel = NaiveTextModelBuilder("John measures the tree").buildModel()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val toMeasure = textProcessor.workingMemory.concepts[0]
        assertEquals("ATRANS", toMeasure.name)
        assertEquals("John", toMeasure.value("actor")?.valueName("firstName"))
        assertEquals("tree", toMeasure.value("thing")?.valueName("name"))
    }
}