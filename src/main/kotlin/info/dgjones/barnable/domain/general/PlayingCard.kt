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

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.grammar.addModifierMappings
import info.dgjones.barnable.grammar.defaultModifierTargetMatcher
import info.dgjones.barnable.parser.*

fun buildGeneralPlayingCardLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordPlayingCardOfSuit())
    CardSuit.values().forEach { lexicon.addMapping(WordCardSuit(it)) }
    CardNamedRank.values().forEach {  rank ->
        lexicon.addMapping(WordCardRank(rank))
    }
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

private fun cardSuitNames(): List<String> {
    return CardSuit.values().map { it.title }
}

enum class CardNamedRank(val word: String, val rankValue: Int) {
    Ace("ace", 1),
    Jack("jack", 11),
    Queen("queen", 12),
    King("king", 13)
}

private fun cardNamedRankNames(): List<String> {
    return CardNamedRank.values().map { it.word }
}

class WordCardSuit(val cardSuit: CardSuit) : WordHandler(EntryWord(cardSuit.title)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, PlayingCardConcept.Suit.name) {
            slot(CoreFields.Name, cardSuit.name)
        }.demons
}

class WordCardRank(val rank: CardNamedRank) : WordHandler(EntryWord(rank.word)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, PlayingCardConcept.Rank.name) {
            slot(CoreFields.Name, rank.name)
            slot(NumberFields.Value, rank.rankValue.toString())
        }.demons
}

class WordPlayingCardOfSuit : WordHandler(EntryWord("of")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, PlayingCardConcept.PlayingCard.name) {
            expectHead(PlayingCardFields.Rank.fieldName, headValues = listOf(NumberConcept.Number.name, PlayingCardConcept.Rank.name), direction = SearchDirection.Before)
            expectConcept(PlayingCardFields.Suit.fieldName, matcher = matchConceptByHead(listOf(PlayingCardConcept.Suit.name)))
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(listOf(NumberConcept.Number.name, PlayingCardConcept.Rank.name)), SearchDirection.Before, null, false, wordContext, disambiguationHandler = disambiguationHandler),
            DisambiguateUsingMatch(matchConceptByHead(PlayingCardConcept.Suit.name), SearchDirection.After, null, false, wordContext, disambiguationHandler)
        )
    }
}
