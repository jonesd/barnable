package info.dgjones.au.concept

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ConceptAccessorTest {
    @Test
    fun `create accessor for root`() {
        val c = Concept("test")
        c.with(Slot("child", Concept("childValue")))

        val accessor = buildConceptPathAccessor(c, "")
        assertSame(c, accessor?.invoke())
    }

    @Test
    fun `create accessor for child`() {
        val c = Concept("test")
        val childValue1 = Concept("childValue1")
        c.with(Slot("child1", childValue1))
        val childValue2 = Concept("childValue2")
        c.with(Slot("child2", childValue2))

        assertSame(childValue1, buildConceptPathAccessor(c, "child1")?.invoke())
        assertSame(childValue2, buildConceptPathAccessor(c, "child2")?.invoke())
    }
}