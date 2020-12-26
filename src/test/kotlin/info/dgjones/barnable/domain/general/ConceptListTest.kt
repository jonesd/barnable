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

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.CoreFields
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConceptListTest {
    var concept0 = Concept("test0")
    var concept1 = Concept("test1")
    @BeforeEach
    fun setup() {
        concept0 = Concept("test0")
        concept1 = Concept("test1")
    }
    @Test
    fun `Create List with initial concepts`() {
        // test
        val list = buildConceptList(listOf(concept0, concept1))

        val listAccessor = ConceptListAccessor(list)
        assertEquals(ListConcept.List.name, list.name)
        assertEquals(2, listAccessor.size)
        assertEquals("test0", listAccessor[0]?.name)
        assertEquals("test1", listAccessor[1]?.name)
    }

    @Test
    fun `Get list item`() {
        val list = buildConceptList(listOf(concept0, concept1))

        // test
        val listAccessor = ConceptListAccessor(list)

        assertEquals(concept1, listAccessor[1])
    }

    @Test
    fun `Get list item outside of range returns null`() {
        val list = buildConceptList(listOf(concept0, concept1))
        val listAccessor = ConceptListAccessor(list)

        assertEquals(null, listAccessor[2])
    }

    @Test
    fun `Get all list item names`() {
        val list = buildConceptList(listOf(concept0, concept1))
        val listAccessor = ConceptListAccessor(list)

        assertEquals(listOf("test0", "test1"), listAccessor.valueNames())
    }

    @Test
    fun `Get all list item concepts`() {
        val list = buildConceptList(listOf(concept0, concept1))
        val listAccessor = ConceptListAccessor(list)

        assertEquals(listOf(concept0, concept1), listAccessor.concepts())
    }
}
