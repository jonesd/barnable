package info.dgjones.au.concept

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.Slot
import info.dgjones.au.concept.buildConceptPathAccessor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptResolutionTest {
    @Test
    fun `set concept value is resolved`() {
        assertTrue(isConceptResolved(Concept("someValue")))
    }
    @Test
    fun `null concept value is unresolved`() {
        assertFalse(isConceptResolved(null))
    }
    @Test
    fun `variable concept value is unresolved`() {
        assertFalse(isConceptResolved(Concept(Concept.VARIABLE_PREFIX+"1")))
    }
    @Test
    fun `blank concept value is unresolved`() {
        assertFalse(isConceptResolved(Concept("")))
    }

    @Test
    fun `set slott value is resolved`() {
        assertTrue(isConceptResolved(Concept("someValue")))
    }
}