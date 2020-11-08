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

import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.domain.general.Acts
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuffixTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Ed Suffix indicates past tense of action`() {
        val textProcessor = runTextProcess("Fred kicked the ball.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val kicked = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.PROPEL.name, kicked.name)

        assertEquals(TimeConcepts.Past.name, kicked.valueName(TimeFields.TIME))
    }

    @Test
    fun `S Suffix indicates plural`() {
        val textProcessor = runTextProcess("two measures of sugar.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val quantity = textProcessor.workingMemory.concepts[0]
        assertEquals(QuantityConcept.Quantity.name, quantity.name)

        assertEquals(GroupConcept.MultipleGroup.name, quantity.valueName(GroupFields.GroupInstances))
    }

    //FIXME ING test
}