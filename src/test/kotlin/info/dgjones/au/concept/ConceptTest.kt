package info.dgjones.au.concept

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.Slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
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
    @Nested
    inner class DuplicateResolved {
        @Test
        fun `Duplicates simple concept`() {
            val original = Concept("test")

            val duplicated = original.duplicateResolvedValue()

            assertEquals("test", duplicated?.name)
            assertNotSame(duplicated, original)
        }
        @Test
        fun `Duplicates tree concept`() {
            val original = Concept("test")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b", Concept("valueB")))

            val duplicated = original.duplicateResolvedValue()

            assertEquals("test", duplicated?.name)
            assertEquals("valueA", duplicated?.valueName("a"))
            assertEquals("valueB", duplicated?.valueName("b"))
        }
        @Test
        fun `Does not duplicate root variable`() {
            val original = createConceptVariable("someVariable")

            val duplicated = original.duplicateResolvedValue()

            assertNull(duplicated)
        }
        @Test
        fun `Does not duplicate child variable`() {
            val original = Concept("test")
                .with(Slot("a", createConceptVariable("someVariable")))
                .with(Slot("b", Concept("valueB")))

            val duplicated = original.duplicateResolvedValue()

            assertEquals("test", duplicated?.name)
            assertNull(duplicated?.valueName("a"))
            assertEquals("valueB", duplicated?.valueName("b"))
        }
    }
    @Nested
    inner class Find {
        @Test
        fun `can match head with concept matcher`() {
            val root = Concept("test")
                .with(Slot("a", createConceptVariable("someVariable")))
                .with(Slot("b", Concept("valueB")))

            val matches = root.find(matchConceptByHead("test"))
            assertEquals(listOf(root), matches)
        }
        @Test
        fun `fails to find any match`() {
            val root = Concept("test")
                .with(Slot("a", createConceptVariable("someVariable")))
                .with(Slot("b", Concept("valueB")))

            val matches = root.find(matchConceptByHead("not-present"))
            assertEquals(listOf<Concept>(), matches)
        }
        @Test
        fun `can search child concepts`() {
            val root = Concept("test")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b", Concept("valueB")))

            val matches = root.find(matchConceptByHead("valueB"))
            assertEquals(1, matches.size)
            assertEquals("valueB", matches[0].name)
        }
        @Test
        fun `can find multiple child matches`() {
            val root = Concept("root")
                .with(Slot("a", Concept("test")
                    .with(Slot("child", Concept("aChild")))))
                .with(Slot("b", Concept("test")
                    .with(Slot("child", Concept("bChild")))))

            val matches = root.find(matchConceptByHead("test"))
            assertEquals(2, matches.size)
            assertEquals("aChild", matches[0].valueName("child"))
            assertEquals("bChild", matches[1].valueName("child"))
        }
        @Test
        fun `does not search within a match`() {
            val root = Concept("test")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b", Concept("valueB")
                    .with(Slot("c", Concept("valueB")))))

            val matches = root.find(matchConceptByHead("valueB"))
            assertEquals(1, matches.size)
            assertEquals("valueB", matches[0].name)
            assertNotNull(matches[0].slot("c"))
        }
    }
}

class SlotTest {
    @Test
    fun `create with field string and value`() {
        val slot = Slot("someName", Concept("someValue"))
        assertEquals("someName", slot.name)
        assertEquals("someValue", slot.value?.name)
    }
    @Test
    fun `create with fieldName and value`() {
        val slot = Slot(CoreFields.INSTANCE, Concept("someValue"))
        assertEquals("instan", slot.name)
        assertEquals("someValue", slot.value?.name)
    }
    @Nested
    inner class CopySlotToAnotherConcept {
        @Test
        fun `copy slot to another concept as new slot`() {
            val slot = Slot("someName", Concept("someValue"))
            val bareConcept = Concept("bare")

            // test
            slot.copyValue(bareConcept)

            assertEquals("someValue", bareConcept.valueName("someName"))
        }

        @Test
        fun `overwrite slot value in another concept`() {
            val slot = Slot("someName", Concept("someValue"))
            val bareConcept = Concept("bare")
                .value("someName", Concept("originalValue"))

            // test
            slot.copyValue(bareConcept)

            assertEquals("someValue", bareConcept.valueName("someName"))
        }
    }
    @Nested
    inner class DuplicateResolved {
        @Test
        fun `Duplicate plain slot`() {
            val source = Slot("someName", Concept("someValue"))

            val duplicated = source.duplicateResolvedValue()

            assertEquals("someName", duplicated.name)
            assertEquals("someValue", duplicated.value?.name)
            assertNotSame(duplicated.value, source.value)
        }

        @Test
        fun `Duplicate preserves null concepts`() {
            val source = Slot("someName", null)

            val duplicated = source.duplicateResolvedValue()

            assertEquals("someName", duplicated.name)
            assertNull(duplicated.value)
        }
    }

    @Nested
    inner class KeyValue {
        @Test
        fun `select key value of concept based on field names`() {
            val root = Concept("root")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b", Concept("valueB")))

            val keyValue = selectKeyValue(root, listOf("b", "a"))
            assertEquals("valueB", keyValue)
        }
        @Test
        fun `select key value skips missing values`() {
            val root = Concept("root")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b"))

            val keyValue = selectKeyValue(root, listOf("b", "a"))
            assertEquals("valueA", keyValue)
        }
        @Test
        fun `select key value skips blank values`() {
            val root = Concept("root")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b", Concept("")))

            val keyValue = selectKeyValue(root, listOf("b", "a"))
            assertEquals("valueA", keyValue)
        }
        @Test
        fun `select key value falls back to head value`() {
            val root = Concept("root")
                .with(Slot("a"))
                .with(Slot("b"))

            val keyValue = selectKeyValue(root, listOf("b", "a"))
            assertEquals("root", keyValue)
        }
    }
}