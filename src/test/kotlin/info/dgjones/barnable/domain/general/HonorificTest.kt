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

import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HonorificTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class CommonTitles {
        @Test
        fun `Mr Snicklefritz`() {
            val textProcessor = runTextProcess("Mr Snicklefritz.", lexicon)

            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FirstName))
            Assertions.assertEquals(Gender.Male.name, human.valueName(HumanFields.Gender))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LastName))
        }

        @Test
        fun `Mrs Snicklefritz`() {
            val textProcessor = runTextProcess("Mrs Snicklefritz.", lexicon)

            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FirstName))
            Assertions.assertEquals(Gender.Female.name, human.valueName(HumanFields.Gender))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LastName))
        }

        @Test
        fun `Miss Snicklefritz`() {
            val textProcessor = runTextProcess("Miss Snicklefritz.", lexicon)

            // FIXME implement
            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FirstName))
            Assertions.assertEquals(Gender.Female.name, human.valueName(HumanFields.Gender))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LastName))
        }

        @Test
        fun `Ms Snicklefritz`() {
            val textProcessor = runTextProcess("Ms Snicklefritz.", lexicon)

            // FIXME implement
            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FirstName))
            Assertions.assertEquals(Gender.Female.name, human.valueName(HumanFields.Gender))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LastName))
        }

        @Test
        fun `Mr John Snicklefritz`() {
            val textProcessor = runTextProcess("Mr John Snicklefritz.", lexicon)

            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("John", human.valueName(HumanFields.FirstName))
            Assertions.assertEquals(Gender.Male.name, human.valueName(HumanFields.Gender))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LastName))
        }
    }
}