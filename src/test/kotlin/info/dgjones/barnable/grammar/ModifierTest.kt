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

package info.dgjones.barnable.grammar

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ModifierTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Colour modifier`() {
        val textProcessor = runTextProcess("the red book", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(PhysicalObjectKind.PhysicalObject.name, concept.valueName(CoreFields.Kind))
        assertEquals("book", concept.valueName(CoreFields.Name))
        assertEquals("red", concept.valueName(ColourFields.Colour))
    }

    @Test
    fun `Multiple modifiers of different types can be applied`() {
        val textProcessor = runTextProcess("a thin old man", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(ModifierConcepts.GreaterThanNormal.name, concept.valueName(CoreFields.Age))
        assertEquals(ModifierConcepts.LessThanNormal.name, concept.valueName(CoreFields.Weight))
    }

    @Test
    fun `Synonym modifiers`() {
        val textProcessor = runTextProcess("an overweight man", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(ModifierConcepts.GreaterThanNormal.name, concept.valueName(CoreFields.Weight))
    }
}
