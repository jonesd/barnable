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
import info.dgjones.barnable.concept.ScaleConcepts
import info.dgjones.barnable.grammar.ConjunctionConcept
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NumberTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class NumberWords {
        @Test
        fun `Number one`() {
            val textProcessor = runTextProcess("one", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertEquals(NumberConcept.Number.name, number.name)
            assertEquals("1", number.valueName(NumberFields.Value))
        }
    }
    @Nested
    inner class ComposeNumberWords {
        @Test
        fun `Compose two number words as addition`() {
            val textProcessor = runTextProcess("twenty one", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertEquals(NumberConcept.Number.name, number.name)
            assertEquals("21", number.valueName(NumberFields.Value))
        }
        @Test
        fun `Compose with hundred multiplier`() {
            val textProcessor = runTextProcess("twenty one hundred", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertEquals(NumberConcept.Number.name, number.name)
            assertEquals("2100", number.valueName(NumberFields.Value))
        }
        @Test
        fun `Compose with dozen multiplier`() {
            val textProcessor = runTextProcess("two dozen", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertEquals(NumberConcept.Number.name, number.name)
            assertEquals("24", number.valueName(NumberFields.Value))
        }
        @Test
        fun `Compose with multiple multipliers`() {
            val textProcessor = runTextProcess("two hundred thousand", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertEquals(NumberConcept.Number.name, number.name)
            assertEquals("200000", number.valueName(NumberFields.Value))
        }
    }
}
