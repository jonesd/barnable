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
import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.parser.*
import info.dgjones.barnable.util.transformCamelCaseToHyphenSeparatedWords

/**
 * Cardinal Direction
 */

enum class CardinalDirectionConcept {
    CardinalDirection
}

fun buildGeneralCardinalDirectionLexicon(lexicon: Lexicon) {
    CardinalDirection.values().forEach { cardinalDirection ->
        cardinalDirection.lexiconNames().forEach { lexiconName ->
            lexicon.addMapping(CardinalDirectionHandler(cardinalDirection, lexiconName))
        }

    }
}

enum class CardinalFields(override val fieldName: String): Fields {
    Degrees("degrees")
}

private enum class CardinalCategory {
    Cardinal,
    Intercardinal,
    SecondaryIntercardinal
}

enum class CardinalDirection(val degrees: Double, private val initials: String, private val category: CardinalCategory) {
    North(0.0, "N", CardinalCategory.Cardinal),
    NorthNorthEast(22.5, "NNE", CardinalCategory.SecondaryIntercardinal),
    NorthEast(45.0, "NE", CardinalCategory.Intercardinal),
    EastNorthEast(67.5, "ENE", CardinalCategory.SecondaryIntercardinal),
    East(90.0, "E", CardinalCategory.Cardinal),
    EastSouthEast(112.5, "ESE", CardinalCategory.SecondaryIntercardinal),
    SouthEast(135.0, "SE", CardinalCategory.Intercardinal),
    SouthSouthEast(157.5, "SSE", CardinalCategory.SecondaryIntercardinal),
    South(180.0, "S", CardinalCategory.Cardinal),
    SouthSouthWest(202.5, "SSW", CardinalCategory.SecondaryIntercardinal),
    SouthWest(225.0, "SW", CardinalCategory.Intercardinal),
    WestSouthWest(247.5, "WSW", CardinalCategory.SecondaryIntercardinal),
    West(270.0, "W", CardinalCategory.Cardinal),
    WestNorthWest(292.5, "WNW", CardinalCategory.SecondaryIntercardinal),
    NorthWest(315.0, "NW", CardinalCategory.Intercardinal),
    NorthNorthWest(337.5, "NNW", CardinalCategory.SecondaryIntercardinal);

    fun lexiconNames(): List<String> {
        return if (category == CardinalCategory.Cardinal) {
            listOf(name)
        } else {
            listOf(name, initials, transformCamelCaseToHyphenSeparatedWords(name))
        }
    }
}

class CardinalDirectionHandler(private val cardinalDirection: CardinalDirection, word: String): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, CardinalDirectionConcept.CardinalDirection.name) {
            slot(CoreFields.Name, cardinalDirection.name)
            slot(CardinalFields.Degrees, cardinalDirection.degrees.toString())
        }.demons
}