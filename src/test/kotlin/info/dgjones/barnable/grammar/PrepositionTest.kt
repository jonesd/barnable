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
import info.dgjones.barnable.narrative.MopMeal
import info.dgjones.barnable.narrative.MopMealFields
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PrepositionTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Builds preposition with word sense`() {
        val concept = buildPrep(Preposition.With)

        assertEquals(PrepositionConcept.Preposition.name, concept.name)
        assertEquals(Preposition.With.name, concept.valueName(CoreFields.Is))
    }

    @Test
    fun `With preposition matches human`() {
        val textProcessor = runTextProcess("Fred had lunch with George.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val lunch = textProcessor.workingMemory.concepts[0]
        assertEquals(MopMeal.MopMeal.name, lunch.name)

        assertEquals("Fred", lunch.value(MopMealFields.EATER_A)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("George", lunch.value(MopMealFields.EATER_B)?.valueName(HumanFields.FIRST_NAME))
    }

    @Test
    fun `With preposition does not match non-human`() {
        val textProcessor = runTextProcess("Fred had lunch with a book.", lexicon)

        assertEquals(2, textProcessor.workingMemory.concepts.size)
        val lunch = textProcessor.workingMemory.concepts[0]
        assertEquals(MopMeal.MopMeal.name, lunch.name)

        assertEquals("Fred", lunch.value(MopMealFields.EATER_A)?.valueName(HumanFields.FIRST_NAME))
        assertNull(lunch.value(MopMealFields.EATER_B))

        val book = textProcessor.workingMemory.concepts[1]
        assertEquals("book", book.valueName(CoreFields.Name))
    }
}
