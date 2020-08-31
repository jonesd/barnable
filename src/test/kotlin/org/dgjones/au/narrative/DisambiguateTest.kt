package org.dgjones.au.narrative

import org.dgjones.au.nlp.NaiveTextModelBuilder
import org.dgjones.au.parser.TextProcessor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DisambiguateTest {
    private val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Two measures of sugar`() {
        val textModel = NaiveTextModelBuilder("Two measures of sugar").buildModel()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        assertEquals(2, textProcessor.workingMemory.concepts.size)
    }

}