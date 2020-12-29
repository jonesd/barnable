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
import info.dgjones.barnable.grammar.MultipleModifierWord
import info.dgjones.barnable.grammar.Preposition
import info.dgjones.barnable.grammar.expectPrep
import info.dgjones.barnable.parser.*

/**
 * Model basic Meteorological domain.
 */
enum class MeteorologyConcept {
    Weather,
    WeatherCharacteristic
}

enum class MeteorologyFields(override val fieldName: String): Fields {
    Weather("weather"),
    WeatherCharacteristics("characteristics")
}

fun buildGeneralMeteorologyLexicon(lexicon: Lexicon) {
    buildWeatherConcepts(lexicon)
    buildWeatherCharacteristics(lexicon)
    buildWeatherInLocation(lexicon)
}

enum class WeatherCharacteristics {
    Snow,
    Squally,
    Thundery,
    Wintry;

    fun word() = this.name.toLowerCase()
}

/* Implement characteristic word elements as modifiers for the Weather */
fun buildWeatherCharacteristics(lexicon: Lexicon) {
    val weatherMatcher = matchConceptByHead(MeteorologyConcept.Weather.name)
    WeatherCharacteristics.values().forEach {
        lexicon.addMapping(MultipleModifierWord(it.word(), MeteorologyFields.WeatherCharacteristics, it.name, weatherMatcher ))
    }
}

private fun buildWeatherConcepts(lexicon: Lexicon) {
    WeatherConcept.values().forEach {
        lexicon.addMapping(WeatherHandler(it))
    }
}

// FIXME defined in a more general
private fun buildWeatherInLocation(lexicon: Lexicon) {
    val weatherMatcher = matchConceptByHead(MeteorologyConcept.Weather.name)

}

enum class WeatherConcept {
    Rain,
    Shower;

    fun word() = this.name.toLowerCase()
}

class WeatherHandler(private val weather: WeatherConcept): WordHandler(EntryWord(weather.word())) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, MeteorologyConcept.Weather.name) {
            slot(CoreFields.Name, weather.name)
            slot(CoreFields.Location, GeneralConcepts.Location.name) {
                expectPrep(CardinalFields.Direction.fieldName, variableName = "cardinalDirection", preps = setOf(Preposition.In), matcher = matchConceptByHead(setOf(
                    CardinalDirectionConcept.CardinalDirection.name))
                )
                varReference(CoreFields.Name.fieldName, "cardinalDirection", extractConceptName)
                varReference(CoreFields.Kind.fieldName, "cardinalDirection", extractConceptHead)
            }

        }.demons
}
