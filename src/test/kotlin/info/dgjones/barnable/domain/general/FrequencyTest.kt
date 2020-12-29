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

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.ScaleConcepts
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FrequencyTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Less frequent`() {
        val textProcessor = runTextProcess("Occasional showers", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val weather = textProcessor.workingMemory.concepts[0]
        assertEquals(WeatherConcept.Shower.name, weather.valueName(CoreFields.Name))
        assertEquals(ScaleConcepts.LessThanNormal.name, weather.valueName(FrequencyFields.Frequency))
    }

    @Test
    fun `More frequent`() {
        val textProcessor = runTextProcess("Frequent showers", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val weather = textProcessor.workingMemory.concepts[0]
        assertEquals(WeatherConcept.Shower.name, weather.valueName(CoreFields.Name))
        assertEquals(ScaleConcepts.GreaterThanNormal.name, weather.valueName(FrequencyFields.Frequency))
    }
}