/*
 * Copyright  2021 David G Jones
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

import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.concept.ScaleConcepts
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.concept.matchConceptByKind
import info.dgjones.barnable.grammar.addModifierMappings
import info.dgjones.barnable.grammar.defaultModifierTargetMatcher
import info.dgjones.barnable.parser.*

fun buildGeneralPlayingCardLexicon(lexicon: Lexicon) {

}

enum class PlayingCardConcept {
    PlayingCard,
    Rank,
    Suit
}

enum class PlayingCardFields(override val fieldName: String): Fields {
    Rank("rank"),
    Suit("suit")
}
enum class CardSuit(val title: String) {
    Heart("heart"),
    Spade("spade"),
    Diamond("diamond"),
    Club("club")
}

enum class CardRank(val words: List<String>, val rankValue: Int) {
    Ace(listOf("ace", "1", "one"), 1),
    Two(listOf("two", "2"), 2),
    Three(listOf("three", "3"), 3),
    Four(listOf("four", "4"), 4),
    Five(listOf("five", "5"), 5),
    Six(listOf("six", "6"), 6),
    Seven(listOf("seven", "7"), 7),
    Eight(listOf("eight", "8"), 8),
    Nine(listOf("nine", "9"), 9),
    Ten(listOf("ten", "10"), 10),
    Jack(listOf("jack"), 11),
    Queen(listOf("queen"), 12),
    King(listOf("king"), 13),
}

class WordPlayingCardOfSuit : WordHandler(EntryWord("of")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, PlayingCardConcept.PlayingCard.name) {
            // FIXME also support "a measure of ..." = default to 1 unit
            expectHead(PlayingCardFields.Rank.fieldName, headValue = NumberConcept.Number.name, direction = SearchDirection.Before)
            slot(QuantityFields.Unit, QuantityConcept.Measure.name)
            expectConcept(PlayingCardFields.Suit.fieldName, matcher = matchConceptByKind(listOf(PhysicalObjectKind.Liquid.name, PhysicalObjectKind.Food.name)))
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingWord("of", matchConceptByKind(listOf(PhysicalObjectKind.Food.name, CardSuit.Liquid.name)), SearchDirection.After, false, wordContext, disambiguationHandler)
        )
    }
}