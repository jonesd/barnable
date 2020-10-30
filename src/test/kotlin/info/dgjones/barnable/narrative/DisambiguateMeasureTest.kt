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

package info.dgjones.barnable.narrative

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DisambiguateMeasureTest {
    private val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Two measures of sugar`() {
        val textProcessor = runTextProcess("Two measures of sugar", lexicon)

        // FIXME think this should be Food sugar with amount, rather than Quantity of sugar
        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val quantity = textProcessor.workingMemory.concepts[0]
        assertEquals(QuantityConcept.Quantity.name, quantity.name)
        assertEquals("2", quantity.value(QuantityFields.Amount)?.valueName(NumberFields.Value))
        assertEquals("sugar", quantity.value(QuantityFields.Of)?.valueName(CoreFields.Name))
    }

    @Test
    fun `John measures the tree`() {
        val textProcessor = runTextProcess("John measures the tree", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val toMeasure = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.ATRANS.name, toMeasure.name)
        assertEquals("John", toMeasure.value(ActFields.Actor)?.valueName(HumanFields.FirstName))
        assertEquals("tree", toMeasure.value(ActFields.Thing)?.valueName(CoreFields.Name))
    }
}