package info.dgjones.au.grammar

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.domain.general.*
import info.dgjones.au.narrative.MopMeal
import info.dgjones.au.narrative.MopMealFields
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ModifierTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Colour modifier`() {
        val textProcessor = runTextProcess("the red book", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(PhysicalObjectKind.PhysicalObject.name, concept.valueName(CoreFields.Kind))
        assertEquals("book", concept.valueName(CoreFields.Name))
        assertEquals("red", concept.valueName("colour"))
    }

    @Test
    fun `Age-Weight modifiers`() {
        val textProcessor = runTextProcess("a thin old man", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals("GT-NORM", concept.valueName("age"))
        assertEquals("LT-NORM", concept.valueName("weight"))
    }
}
