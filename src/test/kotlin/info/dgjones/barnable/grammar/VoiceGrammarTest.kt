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

import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.domain.general.Acts
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShouldMapPassiveAndActiveToSameConceptsTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Active Voice `() {
        val textProcessor = runTextProcess("Fred kicked the ball.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val propel = textProcessor.workingMemory.concepts[0]
        verifyFredKickedBall(propel)
    }

    @Test
    fun `Passive Voice - The ball was kicked by Fred`() {
        val textProcessor = runTextProcess("The ball was kicked by Fred.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val propel = textProcessor.workingMemory.concepts[0]
        verifyFredKickedBall(propel)
    }

    private fun verifyFredKickedBall(propel: Concept) {
        assertEquals(Acts.PROPEL.name, propel.name)
        assertEquals("Fred", propel.value("actor")?.valueName(HumanFields.FirstName))
        assertEquals("ball", propel.value("thing")?.valueName("name"))
    }
}