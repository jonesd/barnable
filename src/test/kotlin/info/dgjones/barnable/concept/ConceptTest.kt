/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.concept

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

    @Nested
    inner class Replace {
        @Test
        fun `replace multiple matches`() {
            val root = Concept("test")
                .with(Slot("a", createConceptVariable("someVariable")))
                .with(Slot("b", createConceptVariable("otherVariable")))
                .with(Slot("c", Concept("valueC")))

            root.replaceSlotValues(matchUnresolvedVariables(), null)
            assertEquals(null, root.valueName("a"))
            assertEquals(null, root.valueName("b"))
            assertEquals("valueC", root.valueName("c"))
        }
    }

    @Nested
    inner class KeyValue {
        @Test
        fun `select key value of concept based on field names`() {
            val root = Concept("root")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b", Concept("valueB")))

            val keyValue = root.selectKeyValue(listOf("b", "a"))
            assertEquals("valueB", keyValue)
        }
        @Test
        fun `select key value skips missing values`() {
            val root = Concept("root")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b"))

            val keyValue = root.selectKeyValue(listOf("b", "a"))
            assertEquals("valueA", keyValue)
        }
        @Test
        fun `select key value skips blank values`() {
            val root = Concept("root")
                .with(Slot("a", Concept("valueA")))
                .with(Slot("b", Concept("")))

            val keyValue = root.selectKeyValue(listOf("b", "a"))
            assertEquals("valueA", keyValue)
        }
        @Test
        fun `select key value falls back to head value`() {
            val root = Concept("root")
                .with(Slot("a"))
                .with(Slot("b"))

            val keyValue = root.selectKeyValue(listOf("b", "a"))
            assertEquals("root", keyValue)
        }
    }

    @Nested
    inner class ShareStateFrom {
        @Test
        fun `no-op when sharing from same concept`() {
            var root = Concept("root")
            val originalRoot = root
            root.value("a", Concept("childA"))
            root.value("b", Concept("childB"))

            root.shareStateFrom(root)

            assertSame(root, originalRoot)
        }

        @Test
        fun `replace root name and childen with aliased values from another concept`() {
            val root = Concept("root")
                .value("a", Concept("valueA"))
                .value("b", Concept("valueB"))
            root.value("a")?.value("aa", Concept("valueAA"))

            var replace = Concept("otherRoot")
                .value("z", Concept("valueZ"))

            val originalRoot = root

            // test
            root.shareStateFrom(replace)

            // root node content replaced from otherRoot
            assertSame(originalRoot, root)
            assertNotSame(replace, root)
            assertEquals("otherRoot", root.name)
            // content matches replace
            assertEquals("otherRoot", root.name)
            assertEquals("valueZ", root.valueName("z"))
            // original roots slots should be removed
            assertEquals(null, root.valueName("a"))
        }
        @Test
        fun `ensure original root concept is duplicated rather than shared when already present in newRoot`() {
            val root = Concept("root")
                .value("a", Concept("valueA"))
                .value("b", Concept("valueB"))
            root.value("a")?.value("aa", Concept("valueAA"))

            val replace = Concept("enclosingRoot")
                .value("childRoot", root)
                .value("childRoot2", Concept("childRoot2Concept"))

            // test
            root.shareStateFrom(replace)

            assertNotSame(replace, root)
            assertEquals("enclosingRoot", root.name)
            // child root should be duplicated - to present loops
            val childRoot = root.value("childRoot")
            assertEquals("root", childRoot?.name)
            assertNotSame(root, childRoot)
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
        val slot = Slot(CoreFields.Instance, Concept("someValue"))
        assertEquals("instance", slot.name)
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
}
