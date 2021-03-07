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

import info.dgjones.barnable.domain.general.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConceptMatcherTest {
    @Nested
    inner class MatchConceptByHeadTest {
        @Test
        fun `Can match by head name`() {
            val matchConceptByHead = matchConceptByHead("testHead")
            assertTrue(matchConceptByHead.matches(Concept("testHead")))
        }

        @Test
        fun `Should print matcher`() {
            val matchConceptByHead = matchConceptByHead("testHead")
            assertEquals("(c.name == testHead)", matchConceptByHead.toString())
        }

        @Test
        fun `Match by head needs full match`() {
            assertFalse(matchConceptByHead("testHead").matches(Concept("otherHead")))
        }

        @Test
        fun `Only matches on head`() {
            assertFalse(
                matchConceptByHead("testHead").matches(
                    Concept("otherHead").value(
                        "testHead",
                        Concept("testHead")
                    )
                )
            )
        }

        @Test
        fun `Fails for missing head`() {
            assertFalse(matchConceptByHead("testHead").matches(null))
        }
    }

    @Nested
    inner class MatchConceptByHeadCollectionTest {
        @Test
        fun `Can match by head name`() {
            val matchConceptByHead = matchConceptByHead(listOf("test1Head", "test2Head"))
            assertTrue(matchConceptByHead.matches(Concept("test2Head")))
        }
        @Test
        fun `Should print matcher`() {
            val matchConceptByHead = matchConceptByHead(listOf("test1Head", "test2Head"))
            assertEquals("(c.name anyOf [test1Head, test2Head])", matchConceptByHead.toString())
        }

        @Test
        fun `Match by head needs full match`() {
            assertFalse(matchConceptByHead(listOf("test1Head", "test2Head")).matches(Concept("otherHead")))
        }

        @Test
        fun `Fails for missing head`() {
            assertFalse(matchConceptByHead(listOf("test1Head", "test2Head")).matches(null))
        }

        @Test
        fun `Fails for no specified heads`() {
            assertFalse(matchConceptByHead(listOf()).matches(Concept("testHead")))
        }
    }

    @Nested
    inner class MatchConceptByKindTest {
        @Test
        fun `Can match by kind name`() {
            val matchConceptByKind = matchConceptByKind("testKind")
            assertTrue(matchConceptByKind.matches(Concept("head").value("kind", Concept("testKind"))))
        }
        @Test
        fun `Should print matcher`() {
            val matchConceptByKind = matchConceptByKind("testKind")
            assertEquals("(c.kind == testKind)", matchConceptByKind.toString())
        }

        @Test
        fun `Match by kind needs full match`() {
            assertFalse(matchConceptByHead("testKind").matches(Concept("head").value("kind", Concept("otherKind"))))
        }

        @Test
        fun `Only matches on kind`() {
            assertFalse(matchConceptByKind("testKind").matches(Concept("head").value("other", Concept("testKind"))))
        }

        @Test
        fun `Fails for missing head`() {
            assertFalse(matchConceptByKind("testHead").matches(null))
        }
    }

    @Nested
    inner class MatchConceptByKindCollectionTest {
        @Test
        fun `Can match by kind name`() {
            val matchConceptByKind = matchConceptByKind(listOf("testKind"))
            assertTrue(
                matchConceptByKind.matches(
                    Concept("head").value(
                        "kind",
                        Concept("testKind")
                    )
                )
            )
        }
        @Test
        fun `Should print matcher`() {
            val matchConceptByKind = matchConceptByKind(listOf("testKind"))
            assertEquals("(c.kind anyOf [testKind])", matchConceptByKind.toString())
        }

        @Test
        fun `Match by kind needs full match`() {
            assertFalse(
                matchConceptByHead(listOf("testKind")).matches(
                    Concept("head").value(
                        "kind",
                        Concept("otherKind")
                    )
                )
            )
        }

        @Test
        fun `Only matches on kind`() {
            assertFalse(
                matchConceptByKind(listOf("testKind")).matches(
                    Concept("head").value(
                        "other",
                        Concept("testKind")
                    )
                )
            )
        }

        @Test
        fun `Fails for missing head`() {
            assertFalse(matchConceptByKind(listOf("testHead")).matches(null))
        }
    }

    @Nested
    inner class MatchConceptByValueNameTest {
        @Test
        fun `Can match by child value name`() {
            val matchConceptValueName = matchConceptValueName("child", "childValue")
            assertTrue(
                matchConceptValueName.matches(
                    Concept("head").value(
                        "child",
                        Concept("childValue")
                    )
                )
            )
        }
        @Test
        fun `Should print matcher`() {
            val matchConceptValueName = matchConceptValueName("child", "childValue")
            assertEquals("(c.child == childValue)", matchConceptValueName.toString())
        }

        @Test
        fun `Match by kind needs full match`() {
            assertFalse(
                matchConceptValueName("child", "childValue").matches(
                    Concept("head").value(
                        "child",
                        Concept("otherValue")
                    )
                )
            )
        }

        @Test
        fun `Only matches on child`() {
            assertFalse(
                matchConceptValueName("child", "childValue").matches(
                    Concept("head").value(
                        "other",
                        Concept("childValue")
                    )
                )
            )
        }

        @Test
        fun `Fails for missing head`() {
            assertFalse(matchConceptValueName("child", "childValue").matches(null))
        }
    }

    @Nested
    inner class MatchAnyTest {
        @Test
        fun `Matches if all match`() {
            val matchAny = matchAny(listOf(matchAlways(), matchAlways()))
            assertTrue(matchAny.matches(Concept("anything")))
        }
        @Test
        fun `Should print matcher`() {
            val matchAny = matchAny(listOf(matchAlways(), matchAlways()))
            assertEquals("(anyOf true|true)", matchAny.toString())
        }

        @Test
        fun `Matches if at least one matches`() {
            assertTrue(matchAny(listOf(matchAlways(), matchNever())).matches(Concept("anything")))
        }

        @Test
        fun `Fails if none match`() {
            assertFalse(matchAny(listOf(matchNever(), matchNever())).matches(Concept("anything")))
        }
    }

    @Nested
    inner class MatchAllTest {
        @Test
        fun `Matches if all match`() {
            val matchAll = matchAll(listOf(matchAlways(), matchAlways()))
            assertTrue(matchAll.matches(Concept("anything")))
        }
        @Test
        fun `Should print matcher`() {
            val matchAll = matchAll(listOf(matchAlways(), matchAlways()))
            assertEquals("(allOf true&true)", matchAll.toString())
        }

        @Test
        fun `Fails if at least one matches`() {
            assertFalse(matchAll(listOf(matchAlways(), matchNever())).matches(Concept("anything")))
        }

        @Test
        fun `Fails if none match`() {
            assertFalse(matchAll(listOf(matchNever(), matchNever())).matches(Concept("anything")))
        }
    }

    @Nested
    inner class MatchAlwaysTest {
        @Test
        fun `Should always match`() {
            val matchAlways = matchAlways()
            assertTrue(matchAlways.matches(Concept("anything")))
        }
        @Test
        fun `Should print matcher`() {
            val matchAlways = matchAlways()
            assertEquals("true", matchAlways.toString())
        }

        @Test
        fun `Matches even for null`() {
            assertTrue(matchAlways().matches(null))
        }
    }

    @Nested
    inner class MatchNeverTest {
        @Test
        fun `Never matches`() {
            val matchNever = matchNever()
            assertFalse(matchNever.matches(Concept("anything")))
        }
        @Test
        fun `Should print matcher`() {
            val matchNever = matchNever()
            assertEquals("false", matchNever.toString())
        }

        @Test
        fun `Not even for null`() {
            assertFalse(matchNever().matches(null))
        }
    }

    @Nested
    inner class MatchConceptByHeadOrGroup {
        @Test
        fun `Matches Group instance head`() {
            val humans = buildGroup(listOf(buildHuman("george"), buildHuman("fred")))
            val matchConceptByHeadOrGroup = matchConceptByHeadOrGroup(HumanConcept.Human.name)
            assertTrue(matchConceptByHeadOrGroup.matches(humans))
        }

        @Test
        fun `Should print matcher`() {
            val matchConceptByHeadOrGroup = matchConceptByHeadOrGroup(HumanConcept.Human.name)
            assertEquals("(anyOf (c.name == Human)|(c.elementsType == Human))", matchConceptByHeadOrGroup.toString())
        }

        @Test
        fun `MisMatches Group instance head`() {
            val humans = buildGroup(listOf(buildHuman("george"), buildHuman("fred")))
            assertFalse(matchConceptByHeadOrGroup(PhysicalObjectKind.PhysicalObject.name).matches(humans))
        }

        @Test
        fun `Can match by head name`() {
            assertTrue(matchConceptByHeadOrGroup("testHead").matches(Concept("testHead")))
        }

        @Test
        fun `Match by head needs full match`() {
            assertFalse(matchConceptByHeadOrGroup("testHead").matches(Concept("otherHead")))
        }
    }

    @Nested
    inner class MatchConceptByHasField {

        @Test
        fun `Matches when root has field with value`() {
            val c = Concept("root")
            c.value(CoreFields.Name, Concept("hasValue"))

            assertTrue(matchConceptHasSlotName(CoreFields.Name).matches(c))
        }

        @Test
        fun `Should print matcher`() {
            assertEquals("(c.name != null)", matchConceptHasSlotName(CoreFields.Name).toString())
        }

        @Test
        fun `Does not match when root has field without value`() {
            val c = Concept("root")
            c.value(CoreFields.Name, null)

            assertFalse(matchConceptHasSlotName(CoreFields.Name).matches(c))
        }

        @Test
        fun `Matches when root has field with other name`() {
            val c = Concept("root")
            c.value(CoreFields.Event, Concept("hasValue"))

            assertFalse(matchConceptHasSlotName(CoreFields.Name).matches(c))
        }
    }

    @Nested
    inner class MatchConceptNot {
        @Test
        fun `Should negate matcher result`() {
            assertFalse(matchNot(matchAlways()).matches(Concept("anything")))
        }

        @Test
        fun `Should print matcher`() {
            assertEquals("(!true)", matchNot(matchAlways()).toString())
        }
    }
}