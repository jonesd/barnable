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

package info.dgjones.barnable.domain.shippingforecast

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.domain.general.GeneralConcepts
import info.dgjones.barnable.grammar.buildEnglishGrammarLexicon
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.Lexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShippingForecastTest {
    val lexicon = buildLexicon()

    private fun buildLexicon(): Lexicon {
        val lexicon = Lexicon()
        buildEnglishGrammarLexicon(lexicon)
        buildGeneralDomainLexicon(lexicon)
        ShippingForecastDomain().buildGeneralDomainLexicon(lexicon)
        return lexicon
    }

    @Test
    fun `Region title should split camel case enum name`() {
        assertEquals("North Utsire", ShippingForecastEnum.NorthUtsire.title())
    }

    @Nested
    inner class Regions {
        @Test
        fun `Interprets shipping forecast region`() {
            val textProcessor = runTextProcess("North Utsire", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val region = textProcessor.workingMemory.concepts.first()
            assertEquals(GeneralConcepts.PhysicalObject.name, region.name)
            assertEquals("NorthUtsire", region.valueName(CoreFields.Name))
            assertEquals(ShippingForecastConcepts.ShippingForecast_Region.name, region.valueName(CoreFields.Kind))
        }
        @Test
        fun `Group multiple regions`() {
            val textProcessor = runTextProcess("Rockall, Malin, Hebrides", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val regions = GroupAccessor(textProcessor.workingMemory.concepts.first())
            assertEquals(3, regions.size)
            assertEquals("Rockall", regions[0]?.valueName(CoreFields.Name))
            assertEquals("Malin", regions[1]?.valueName(CoreFields.Name))
            assertEquals("Hebrides", regions[2]?.valueName(CoreFields.Name))
        }
    }

    @Nested
    inner class WeatherSegment {
        @Test
        fun `Basic weather`() {
            val textProcessor = runTextProcess("Rain", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts.first()
            assertEquals(GeneralConcepts.Weather.name, weather.name)
            assertEquals(WeatherConcept.Rain.name, weather.valueName(CoreFields.Name))
        }
    }
}
