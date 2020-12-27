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
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class CardinalDirectionTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Recognize cardinal directions`() {
        val textProcessor = runTextProcess("East", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val direction = textProcessor.workingMemory.concepts[0]
        assertEquals(CardinalDirectionConcept.CardinalDirection.name, direction.name)
        assertEquals("East", direction.valueName(CoreFields.Name))
        assertEquals("90.0", direction.valueName(CardinalFields.Degrees))
    }
    @Test
    fun `Recognize intercardinal direction as one word`() {
        val textProcessor = runTextProcess("NorthEast", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val direction = textProcessor.workingMemory.concepts[0]
        assertEquals(CardinalDirectionConcept.CardinalDirection.name, direction.name)
        assertEquals("NorthEast", direction.valueName(CoreFields.Name))
        assertEquals("45.0", direction.valueName(CardinalFields.Degrees))
    }
    @Test
    fun `Recognize intercardinal direction as one word without secondary capitalization`() {
        val textProcessor = runTextProcess("Northeast", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val direction = textProcessor.workingMemory.concepts[0]
        assertEquals(CardinalDirectionConcept.CardinalDirection.name, direction.name)
        assertEquals("NorthEast", direction.valueName(CoreFields.Name))
        assertEquals("45.0", direction.valueName(CardinalFields.Degrees))
    }
    @Test
    fun `Recognize intercardinal direction with hyphen separators`() {
        val textProcessor = runTextProcess("North-East", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val direction = textProcessor.workingMemory.concepts[0]
        assertEquals(CardinalDirectionConcept.CardinalDirection.name, direction.name)
        assertEquals("NorthEast", direction.valueName(CoreFields.Name))
        assertEquals("45.0", direction.valueName(CardinalFields.Degrees))
    }
    @Test
    fun `Recognize intercardinal direction by initials`() {
        val textProcessor = runTextProcess("NE", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val direction = textProcessor.workingMemory.concepts[0]
        assertEquals(CardinalDirectionConcept.CardinalDirection.name, direction.name)
        assertEquals("NorthEast", direction.valueName(CoreFields.Name))
        assertEquals("45.0", direction.valueName(CardinalFields.Degrees))
    }

    @Test
    fun `Recognize secondary intercardinal direction as one word`() {
        val textProcessor = runTextProcess("NorthNorthEast", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val direction = textProcessor.workingMemory.concepts[0]
        assertEquals(CardinalDirectionConcept.CardinalDirection.name, direction.name)
        assertEquals("NorthNorthEast", direction.valueName(CoreFields.Name))
        assertEquals("22.5", direction.valueName(CardinalFields.Degrees))
    }
}