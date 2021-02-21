/*
 * Copyright  2021 David G Jones
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

package info.dgjones.barnable.domain.general.numeric

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

fun assertNumberEquals(actual: Concept, expectedValue: Int) {
    assertContainsRooted(actual, concept(NumberConcept.Number.name) {
        slot(NumberFields.Value, expectedValue.toString())
    })
}

class NumberTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class NumberValuesTest {
        @Test
        fun `split name into separate words`() {
            assertEquals(NumberValues.LongHundred.words, listOf("long", "hundred"))
        }
    }

    @Nested
    inner class NumberWords {
        @Test
        fun `Number one`() {
            val textProcessor = runTextProcess("one", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 1)
        }
    }
    @Nested
    inner class NumberDigits {
        @Test
        fun `Number of single digit`() {
            val textProcessor = runTextProcess("1", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 1)
        }
        @Test
        fun `Number with multiple digits`() {
            val textProcessor = runTextProcess("1234", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 1234)
        }
        @Test
        fun `Negative number`() {
            val textProcessor = runTextProcess("-1234", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, -1234)
        }
        @Test
        fun `Mixing words and digits`() {
            val textProcessor = runTextProcess("100 million", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 100000000)
        }
        @Test
        fun `Number with comma separated digits`() {
            val textProcessor = runTextProcess("1,234", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 1234)
        }
    }
    @Nested
    inner class ComposeNumberWords {
        @Test
        fun `Compose two number words as addition`() {
            val textProcessor = runTextProcess("twenty one", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 21)
        }
        @Test
        fun `Compose with hundred multiplier`() {
            val textProcessor = runTextProcess("twenty one hundred", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 2100)
        }
        @Test
        fun `Compose with dozen multiplier`() {
            val textProcessor = runTextProcess("two dozen", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 24)
        }
        @Test
        fun `Compose with multiple multipliers`() {
            val textProcessor = runTextProcess("two hundred thousand", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 200000)
        }
    }
    @Nested
    inner class ComposeNumbersWithAnd {
        @Test
        fun `Compose two number words as addition`() {
            val textProcessor = runTextProcess("one hundred and twenty", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 120)
        }
    }

    @Nested
    inner class Score {
        @Test
        fun `multiple score`() {
            val textProcessor = runTextProcess("six score", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 120)
        }
    }

    @Nested
    inner class LongHundred {
        @Test
        fun `long hundred`() {
            val textProcessor = runTextProcess("long hundred", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 120)
        }
        @Test
        fun `long thousand`() {
            val textProcessor = runTextProcess("long thousand", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 1200)
        }
        @Test
        fun `short hundred`() {
            val textProcessor = runTextProcess("short hundred", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 100)
        }
        @Test
        fun `can use long hundred as part of numeric expression`() {
            val textProcessor = runTextProcess("two long hundreds and five", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val number = textProcessor.workingMemory.concepts[0]
            assertNumberEquals(number, 245)
        }
    }

    @Nested
    inner class NumbersAsQuantityOfThings {
        @Test
        fun `Number is applied as quantity of objects`() {
            val textProcessor = runTextProcess("twenty books", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val books = textProcessor.workingMemory.concepts[0]
            assertEquals(GeneralConcepts.PhysicalObject.name, books.name)
            assertEquals("book", books.valueName(CoreFields.Name))
            assertEquals("20", books.valueName(QuantityFields.Amount))
        }
        @Test
        fun `Composed Number is applied as quantity of objects`() {
            val textProcessor = runTextProcess("twenty one books", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val books = textProcessor.workingMemory.concepts[0]
            assertEquals(GeneralConcepts.PhysicalObject.name, books.name)
            assertEquals("book", books.valueName(CoreFields.Name))
            assertEquals("21", books.valueName(QuantityFields.Amount))
        }
        @Test
        fun `Support multiple quantities in same sentence`() {
            val textProcessor = runTextProcess("twenty one books and ninety two balls", lexicon)

            assertEquals(2, textProcessor.workingMemory.concepts.size)

            val books = textProcessor.workingMemory.concepts[0]
            assertEquals(GeneralConcepts.PhysicalObject.name, books.name)
            assertEquals("book", books.valueName(CoreFields.Name))
            assertEquals("21", books.valueName(QuantityFields.Amount))

            val balls = textProcessor.workingMemory.concepts[1]
            assertEquals(GeneralConcepts.PhysicalObject.name, balls.name)
            assertEquals("ball", balls.valueName(CoreFields.Name))
            assertEquals("92", balls.valueName(QuantityFields.Amount))
        }
    }
}
