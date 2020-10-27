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
        assertEquals("red", concept.valueName(ColourFields.Colour))
    }

    @Test
    fun `Multiple modifiers of different types can be applied`() {
        val textProcessor = runTextProcess("a thin old man", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(ModifierConcepts.GreaterThanNormal.name, concept.valueName(CoreFields.Age))
        assertEquals(ModifierConcepts.LessThanNormal.name, concept.valueName(CoreFields.Weight))
    }

    @Test
    fun `Synonym modifiers`() {
        val textProcessor = runTextProcess("an overweight man", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(ModifierConcepts.GreaterThanNormal.name, concept.valueName(CoreFields.Weight))
    }
}
