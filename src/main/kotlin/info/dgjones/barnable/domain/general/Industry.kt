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
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.narrative.MopFields
import info.dgjones.barnable.narrative.MopRestaurant
import info.dgjones.barnable.parser.*

fun buildGeneralIndustryLexicon(lexicon: Lexicon) {
    buildEatingPlaces(lexicon)
}

class NaicsCode(val code: Long)

enum class Industry {
    EatingPlaces
}

// https://www.naics.com/sic-industry-description/?code=5812
enum class EatingPlaces(val title: String, val naics: NaicsCode) {
    Restaurant("restaurant", NaicsCode(5812)),

    Cafe("cafe", NaicsCode(58129902)),
    CoffeeShop("coffee shop", NaicsCode(58120304))
}

fun buildEatingPlaces(lexicon: Lexicon) {
    EatingPlaces.values().forEach {
        lexicon.addMapping(WordEatingPlace(it))
    }
}

class WordEatingPlace(val eatingPlace: EatingPlaces): WordHandler(
    EntryWord(eatingPlace.title)
) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GeneralConcepts.Setting.name) {
            slot(MopFields.MOP, MopRestaurant.MopRestaurant.name)
            slot(CoreFields.Name, eatingPlace.title)
        }.demons
}