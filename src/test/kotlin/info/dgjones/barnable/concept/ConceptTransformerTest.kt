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

import info.dgjones.barnable.domain.general.HumanConcept
import info.dgjones.barnable.domain.general.buildGroup
import info.dgjones.barnable.domain.general.buildHuman
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptTransformerTest {
    @Test
    fun `Can match by head name`() {
        val source = Concept("Test")
        source.value("child", Concept("childValue"))

        // test
        val head = extractConceptHead.transform(source)

        assertEquals("Test", head?.name)
        assertTrue(head?.children()?.isEmpty() ?: false)
    }
    @Test
    fun `Can match by Name value`() {
        val source = Concept("Test")
        source.value(CoreFields.Name, Concept("ChildValue"))

        // test
        assertEquals(Concept("ChildValue"), extractConceptName.transform(source))
    }
    @Test
    fun `Can match by Name value returns null when not present`() {
        val source = Concept("Test")
        source.value("other", Concept("ChildValue"))

        // test
        assertNull(extractConceptName.transform(source))
    }
}
