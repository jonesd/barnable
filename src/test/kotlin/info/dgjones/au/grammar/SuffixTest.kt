package info.dgjones.au.grammar

import info.dgjones.au.domain.general.*
import info.dgjones.au.narrative.Acts
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Ed Suffix indicates past tense of action`() {
        val textProcessor = runTextProcess("Fred kicked the ball.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val kicked = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.PROPEL.name, kicked.name)

        assertEquals(TimeConcepts.Past.name, kicked.valueName(TimeFields.TIME))
    }

    @Test
    fun `S Suffix indicates plural`() {
        val textProcessor = runTextProcess("two measures of sugar.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val quantity = textProcessor.workingMemory.concepts[0]
        assertEquals(QuantityConcept.Quantity.name, quantity.name)

        assertEquals(GroupConcept.`*multiple*`.name, quantity.valueName(GroupFields.GroupInstances))
    }

    //FIXME ING test
}