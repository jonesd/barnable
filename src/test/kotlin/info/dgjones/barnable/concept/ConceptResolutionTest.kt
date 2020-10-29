package info.dgjones.barnable.concept

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
    fun `set slot value is resolved`() {
        assertTrue(isConceptResolved(Concept("someValue")))
    }
}