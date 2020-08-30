package org.dgjones.au.parser

import org.dgjones.au.parser.Concept
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptTest {
    @Test
    fun `create concept`() {
        val c = Concept("test")
        assertEquals("test", c.name)
    }

    @Test
    fun `can add a slot`() {
        val c = Concept("test")
        c.with(Slot("slot0", Concept("value0")))

        assertEquals("value0", c.value("slot0")?.name)
    }

    @Test
    fun `can change slot value`() {
        val c = Concept("test")
        c.with(Slot("slot0", Concept("value0")))
        c.value("slot0", Concept("updatedValue"))

        assertEquals("updatedValue", c.value("slot0")?.name)
    }
}