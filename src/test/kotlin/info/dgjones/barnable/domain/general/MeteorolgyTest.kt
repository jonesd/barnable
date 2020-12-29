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
import info.dgjones.barnable.grammar.ConjunctionConcept
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MeteorolgyTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class WeatherTest {
        @Test
        fun `Weather basic`() {
            val textProcessor = runTextProcess("Rain", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertEquals(MeteorologyConcept.Weather.name, weather.name)
            assertEquals(WeatherConcept.Rain.name, weather.valueName(CoreFields.Name))
        }

        @Test
        fun `Weather with severity`() {
            val textProcessor = runTextProcess("Thundery showers", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertEquals(MeteorologyConcept.Weather.name, weather.name)
            assertEquals(WeatherConcept.Shower.name, weather.valueName(CoreFields.Name))
            val characteristics = ConceptListAccessor(weather.value(MeteorologyFields.WeatherCharacteristics)!!)
            assertEquals(setOf(WeatherCharacteristics.Thundery.name), characteristics.valueNames().toSet())
        }

        @Test
        fun `Weather with multiple severity`() {
            val textProcessor = runTextProcess("squally wintry showers", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertEquals(MeteorologyConcept.Weather.name, weather.name)
            assertEquals(WeatherConcept.Shower.name, weather.valueName(CoreFields.Name))
            val characteristics = ConceptListAccessor(weather.value(MeteorologyFields.WeatherCharacteristics)!!)
            assertEquals(
                setOf(WeatherCharacteristics.Wintry.name, WeatherCharacteristics.Squally.name),
                characteristics.valueNames().toSet()
            )
        }
        @Test
        fun `Alternative weather`() {
            val textProcessor = runTextProcess("Rain or showers", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val groupWeather = GroupAccessor(textProcessor.workingMemory.concepts[0])
            assertEquals(MeteorologyConcept.Weather.name, groupWeather.elementType())
            assertEquals(listOf(withWeather(WeatherConcept.Rain), withWeather(WeatherConcept.Shower)), groupWeather.concepts())
            assertEquals(ConjunctionConcept.Or.name, groupWeather.conjunctionType())
        }
        private fun withWeather(weather: WeatherConcept): Concept {
            val root = Concept(MeteorologyConcept.Weather.name)
            root.value(MeteorologyFields.Weather, Concept(weather.name))
            return root
        }

        @Test
        fun `Weather specific to direction`() {
            val textProcessor = runTextProcess("Rain in northwest", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertEquals(MeteorologyConcept.Weather.name, weather.name)
            assertEquals(WeatherConcept.Rain.name, weather.valueName(CoreFields.Name))
            assertEquals(GeneralConcepts.Location.name, weather.valueName(CoreFields.Location))
            assertEquals(CardinalDirection.NorthWest.name, weather.value(CoreFields.Location)?.valueName(CoreFields.Name))
            assertEquals(CardinalDirectionConcept.CardinalDirection.name, weather.value(CoreFields.Location)?.valueName(CoreFields.Kind))
        }
    }
}