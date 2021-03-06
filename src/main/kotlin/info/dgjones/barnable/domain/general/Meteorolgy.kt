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
import info.dgjones.barnable.grammar.ModifierWord
import info.dgjones.barnable.grammar.MultipleModifierWord
import info.dgjones.barnable.grammar.Preposition
import info.dgjones.barnable.grammar.expectPrep
import info.dgjones.barnable.parser.*

/**
 * Model basic Meteorological domain.
 */
enum class MeteorologyConcept {
    Weather,
    WeatherCharacteristic,
    Wind
}

enum class MeteorologyFields(override val fieldName: String) : Fields {
    Weather("weather"),
    WeatherCharacteristics("characteristics")
}

fun buildGeneralMeteorologyLexicon(lexicon: Lexicon) {
    buildWeatherConcepts(lexicon)
    buildWeatherCharacteristics(lexicon)
    buildWindConcepts(lexicon)
    buildWindModifiers(lexicon)
    buildWindDirections(lexicon)
}

enum class WeatherCharacteristics {
    Snowy,
    Squally,
    Thundery,
    Wintry;

    fun word() = this.name.toLowerCase()
}

/* Implement characteristic word elements as modifiers for the Weather */
fun buildWeatherCharacteristics(lexicon: Lexicon) {
    val weatherMatcher = matchConceptByHead(MeteorologyConcept.Weather.name)
    WeatherCharacteristics.values().forEach {
        lexicon.addMapping(
            MultipleModifierWord(
                it.word(),
                MeteorologyFields.WeatherCharacteristics,
                it.name,
                weatherMatcher
            )
        )
    }
}

private fun buildWeatherConcepts(lexicon: Lexicon) {
    WeatherConcept.values().forEach {
        lexicon.addMapping(WeatherHandler(it))
    }
}

enum class WeatherConcept {
    Drizzle,
    Fair,
    Rain,
    Shower;

    fun word() = this.name.toLowerCase()
}

class WeatherHandler(private val weather: WeatherConcept) : WordHandler(EntryWord(weather.word())) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, MeteorologyConcept.Weather.name) {
            slot(CoreFields.Name, weather.name)
            slot(CoreFields.State, StateConcepts.Neutral.name)
            slot(CoreFields.Location, GeneralConcepts.Location.name) {
                expectPrep(
                    CardinalFields.Direction.fieldName,
                    variableName = "cardinalDirection",
                    preps = setOf(Preposition.In),
                    matcher = matchConceptByHead(
                        setOf(
                            CardinalDirectionConcept.CardinalDirection.name
                        )
                    )
                )
                varReference(CoreFields.Name.fieldName, "cardinalDirection", extractConceptName)
                varReference(CoreFields.Kind.fieldName, "cardinalDirection", extractConceptHead)
            }

        }.demons
}

/** Names for winds */
enum class WindConcepts() {
    Hurricane,
    Gale,
    Storm,
    Wind;

    fun word() = this.name.toLowerCase()
}

fun buildWindConcepts(lexicon: Lexicon) {
    WindConcepts.values().forEach {
        lexicon.addMapping(WindHandler(it))
    }
}

enum class WindSeverity(val scale: ScaleConcepts) {
    Light(ScaleConcepts.LessThanNormal),
    Heavy(ScaleConcepts.GreaterThanNormal),
    Severe(ScaleConcepts.GreaterThanNormal),
    Violent(ScaleConcepts.GreaterThanNormal);

    fun word() = this.name.toLowerCase()
}

private fun buildWindModifiers(lexicon: Lexicon) {
    val windMatcher = matchConceptByHead(MeteorologyConcept.Wind.name)
    WindSeverity.values().forEach {
        lexicon.addMapping(ModifierWord(it.word(), CoreFields.Scale, it.scale.name, windMatcher))
    }
}

private fun buildWindDirections(lexicon: Lexicon) {
    val windMatcher = matchConceptByHead(MeteorologyConcept.Wind.name)
    CardinalDirection.values().forEach {
        val word = it.name.toLowerCase() + "erly"
        lexicon.addMapping(ModifierWord(word, CardinalFields.Direction, it.name, windMatcher))
    }
}

class WindHandler(private val wind: WindConcepts) : WordHandler(EntryWord(wind.word())) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, MeteorologyConcept.Wind.name) {
            slot(CoreFields.Name, wind.name)
            slot(CoreFields.State, StateConcepts.Neutral.name)
        }.demons
}