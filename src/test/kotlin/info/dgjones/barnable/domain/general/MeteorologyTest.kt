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

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.grammar.ConjunctionConcept
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError

class MeteorologyTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class WeatherTest {
        @Test
        fun `Weather basic`() {
            val textProcessor = runTextProcess("Rain", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(weather, concept(MeteorologyConcept.Weather.name) {
                slot(CoreFields.Name, WeatherConcept.Rain.name)
            })
        }

        @Test
        fun `Weather with severity`() {
            val textProcessor = runTextProcess("Thundery showers", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(weather, concept(MeteorologyConcept.Weather.name) {
                slot(CoreFields.Name, WeatherConcept.Shower.name)
                slot(MeteorologyFields.WeatherCharacteristics, listOf(WeatherCharacteristics.Thundery.name))
            })
        }

        @Test
        fun `Weather with multiple severity`() {
            val textProcessor = runTextProcess("squally wintry showers", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(weather, concept(MeteorologyConcept.Weather.name) {
                slot(CoreFields.Name, WeatherConcept.Shower.name)
                slot(MeteorologyFields.WeatherCharacteristics, listOf(WeatherCharacteristics.Wintry.name, WeatherCharacteristics.Squally.name))
            })
        }
        @Test
        fun `Alternative weather`() {
            val textProcessor = runTextProcess("Rain or showers", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val groupWeather = GroupAccessor(textProcessor.workingMemory.concepts[0])
            assertEquals(MeteorologyConcept.Weather.name, groupWeather.elementType())
            assertContainsRootedList(groupWeather.concepts(), listOf(withWeather(WeatherConcept.Rain), withWeather(WeatherConcept.Shower)))
            assertEquals(ConjunctionConcept.Or.name, groupWeather.conjunctionType())
        }
        @Test
        fun `Progression using later`() {
            val textProcessor = runTextProcess("Mainly fair, occasional drizzle later", lexicon)

            assertEquals(2, textProcessor.workingMemory.concepts.size)
            val fair = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(fair, withWeather(WeatherConcept.Fair))
            val drizzle = textProcessor.workingMemory.concepts[1]
            assertContainsRooted(drizzle, withTiming(withWeather(WeatherConcept.Drizzle), "Later"))
//            assertEquals(MeteorologyConcept.Weather.name, groupWeather.elementType())
//            assertContainsRootedList(groupWeather.concepts(),
//                listOf(
//                    withWeather(WeatherConcept.Fair),
//                    withTiming(withWeather(WeatherConcept.Drizzle), "Later")
//            ))
//            assertEquals(ConjunctionConcept.And.name, groupWeather.conjunctionType())
        }
        private fun withWeather(weather: WeatherConcept): Concept {
            val root = Concept(MeteorologyConcept.Weather.name)
            root.value(CoreFields.Name, Concept(weather.name))
            return root
        }
        private fun withTiming(weather: Concept, timing: String): Concept {
            weather.value(TimeFields.TIME, Concept(timing))
            return weather
        }

        @Test
        fun `Weather specific to direction`() {
            val textProcessor = runTextProcess("Rain in northwest", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(weather, concept(MeteorologyConcept.Weather.name) {
                slot(CoreFields.Name, WeatherConcept.Rain.name)
                slot(CoreFields.Location, GeneralConcepts.Location.name) {
                    slot(CoreFields.Name, CardinalDirection.NorthWest.name)
                    slot(CoreFields.Kind, CardinalDirectionConcept.CardinalDirection.name)
                }
            })
        }
        @Test
        fun `Weather specific to direction passive`() {
            val textProcessor = runTextProcess("In northwest, rain", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(weather, concept(MeteorologyConcept.Weather.name) {
                slot(CoreFields.Name, WeatherConcept.Rain.name)
                slot(CoreFields.Location, GeneralConcepts.Location.name) {
                    slot(CoreFields.Name, CardinalDirection.NorthWest.name)
                    slot(CoreFields.Kind, CardinalDirectionConcept.CardinalDirection.name)
                }
            })
        }
        @Test
        fun `Weather with frequency before`() {
            val textProcessor = runTextProcess("Occasional rain", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val weather = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(weather, concept(MeteorologyConcept.Weather.name) {
                slot(CoreFields.Name, WeatherConcept.Rain.name)
                slot(FrequencyFields.Frequency, ScaleConcepts.LessThanNormal.name)
            })
        }
    }

    @Nested
    inner class WindTest {
        @Test
        fun `Wind basic`() {
            val textProcessor = runTextProcess("Gale", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val wind = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(wind, concept(MeteorologyConcept.Wind.name) {
                slot(CoreFields.Name, WindConcepts.Gale.name)
            })
        }
        @Test
        fun `Wind scale modifier`() {
            val textProcessor = runTextProcess("Severe gale", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val wind = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(wind, concept(MeteorologyConcept.Wind.name) {
                slot(CoreFields.Name, WindConcepts.Gale.name)
                slot(CoreFields.Scale, ScaleConcepts.GreaterThanNormal.name)
            })
        }
        @Test
        fun `Direction of wind`() {
            val textProcessor = runTextProcess("Northerly gale", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val wind = textProcessor.workingMemory.concepts[0]
            assertContainsRooted(wind, concept(MeteorologyConcept.Wind.name) {
                slot(CoreFields.Name, WindConcepts.Gale.name)
                slot(CardinalFields.Direction, CardinalDirection.North.name)
            })
        }
    }
}