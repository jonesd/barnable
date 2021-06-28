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

package info.dgjones.barnable.domain.cardgames.model

data class CardPlayer(val name: String,
                      val hand: CardHolder = CardHolder("hand"),
    val presentations: PlayerPresentationHolder = PlayerPresentationHolder()) {
    var out: Boolean = false
}

class CardGameModel(val name: String, val deal: CardDeal) {
}

class CardGameInstance(val model: CardGameModel, val players: List<CardPlayer>)

data class PlayingCardSuit(val suit: String, val symbol: String)
data class PlayingCardRank(val value: Int, val code: String = value.toString(), val title: String = code)
data class PlayingCard(val rank: PlayingCardRank, val suit: PlayingCardSuit) {
    override fun toString(): String {
        return code()
    }
    fun code(): String {
        return "${rank.code}${suit.symbol}"
    }
}

val SuitClub = PlayingCardSuit("Club", '\u2663'.toString())
val SuitDiamond = PlayingCardSuit("Diamond", '\u2662'.toString())
val SuitHeart = PlayingCardSuit("Heart", '\u2661'.toString())
val SuitSpade = PlayingCardSuit("Spade", '\u2660'.toString())
val StandardSuits = listOf(SuitClub, SuitDiamond, SuitHeart, SuitSpade)

val RankAce = PlayingCardRank(1, "A","Ace")
val Rank2 = PlayingCardRank(2)
val Rank3 = PlayingCardRank(3)
val Rank4 = PlayingCardRank(4)
val Rank5 = PlayingCardRank(5)
val Rank6 = PlayingCardRank(6)
val Rank7 = PlayingCardRank(7)
val Rank8 = PlayingCardRank(8)
val Rank9 = PlayingCardRank(9)
val Rank10 = PlayingCardRank(10)
val RankJack = PlayingCardRank(11, "J","Jack")
val RankQueen = PlayingCardRank(12, "Q","Queen")
val RankKing = PlayingCardRank(13, "K","King")
val StandardRanks = listOf(RankAce, Rank2, Rank3, Rank4, Rank5, Rank6, Rank7, Rank8, Rank9, Rank10,
RankJack, RankQueen, RankKing)

class CardDeckBuilder() {
    val cards = mutableListOf<PlayingCard>()

    fun withStandardDeck(): CardDeckBuilder {
        StandardSuits.forEach { suit ->
            StandardRanks.forEach { rank ->
                cards.add(PlayingCard(rank, suit))}}
        return this
    }

    fun shuffle(): CardDeckBuilder {
        cards.shuffle()
        return this
    }
    fun build() = cards
}

data class CardHolder(val name: String, private val cards: MutableList<PlayingCard> = mutableListOf<PlayingCard>(), val faceUp: Boolean = false) {
    fun transferFirst(numberOfCards: Int, destination: CardHolder): List<PlayingCard> {
        val transferred = mutableListOf<PlayingCard>()
        repeat(numberOfCards) {
            cards.removeFirstOrNull()?.let {
                destination.add(it)
                transferred.add(it)
            }
        }
        return transferred.toList()
    }
    fun transferAll(destination: CardHolder) {
        transferFirst(cards.size, destination)
    }
    fun transfer(playingCard: PlayingCard, destination: CardHolder) {
        if (cards.remove(playingCard)) {
            destination.add(playingCard)
        }
    }
    fun add(card: PlayingCard) {
        cards.add(card)
    }
    fun cards() = cards.toList()
    fun firstOrNull() = cards.firstOrNull()
    fun isEmpty() = cards.isEmpty()
    fun size() = cards.size
}

data class PlayerPresentationHolder(val cardHolders: MutableList<CardHolder> = mutableListOf()) {
    fun add(cardHolder: CardHolder) {
        cardHolders.add(cardHolder)
    }
}


interface CardDeal {
    fun dealCardsToPlayerHand(cards: Int, source: CardHolder, players: List<CardPlayer>, extraCardsHolder: CardHolder?)
}

class FixedDeal(): CardDeal {
    override fun dealCardsToPlayerHand(cards: Int, source: CardHolder, players: List<CardPlayer>, extraCardsHolder: CardHolder?) {
        players.forEach {
            source.transferFirst(cards, it.hand)
        }
        extraCardsHolder?.let {
            source.transferAll(it)
        }
    }
}

enum class CardGameType() {
    Shedding
}