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
import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.concept.matchConceptByHead
import info.dgjones.barnable.grammar.MultipleModifierWord
import info.dgjones.barnable.parser.*

fun buildGeneralMeteorolgyLexicon(lexicon: Lexicon) {
    buildWeatherConcepts(lexicon)
    buildWeatherModifiers(lexicon)
}

fun buildWeatherModifiers(lexicon: Lexicon) {
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

enum class MeteorologyConcept {
    Weather,
    WeatherCharacteristic
}

enum class MeteorologyFields(override val fieldName: String): Fields {
    Weather("weather"),
    WeatherCharacteristics("characteristics")
}

enum class WeatherConcept {
    Rain,
    Shower;

    fun word() = this.name.toLowerCase()
}

enum class WeatherCharacteristics {
    Snow,
    Squally,
    Thundery,
    Wintry;

    fun word() = this.name.toLowerCase()
}

class WeatherHandler(val weather: WeatherConcept): WordHandler(EntryWord(weather.word())) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, MeteorologyConcept.Weather.name) {
            slot(MeteorologyFields.Weather, weather.name)
        }.demons
}
