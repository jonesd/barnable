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

package info.dgjones.barnable.domain.cardgames.game

import info.dgjones.barnable.domain.cardgames.model.*

class CardGameGoFish(numberOfPlayers: Int = 7) : CardGameRunner(numberOfPlayers) {
    private val ocean = CardHolder("ocean")

    override fun createStrategy(index: Int, cardPlayer: CardPlayer): CardGamePlayerStrategy {
        return GoFishBasicStrategy(cardPlayer, this)
    }

    override fun runPlayerTurn(currentPlayer: CardPlayer) {
        requestCardsOfMatchingRank(currentPlayer)
    }

    override fun runSpecialBehavioursAtAnytime() {
        collectAnyBooks()
    }

    override fun isGameFinished() = !isAnyCardsInHand()

    override fun dealCards(deck: CardHolder) {
        val numberOfCards = dealNumberOfCardsPerPlayer()
        FixedDeal().dealCardsToPlayerHand(numberOfCards, deck, players, ocean)
    }

    private fun dealNumberOfCardsPerPlayer() =
        if (numberOfPlayers <= 3) 7 else 5

    private fun strategyFor(player: CardPlayer) =
        strategies.first { it.player == player } as GoFishBasicStrategy

    private fun requestCardsOfMatchingRank(currentPlayer: CardPlayer) {
        if (!currentPlayer.hand.isEmpty()) {
            strategyFor(currentPlayer).selectPlayerToRequestCards()
        } else {
            println("${currentPlayer.name} does not have any cards and so cannot request cards from another player")
        }/*
        if (!currentPlayer.hand.isEmpty()) {
            currentPlayer.hand.cards().randomOrNull()?.let { exemplar ->
                println("${currentPlayer.name} asks ${otherPlayer.name} whether they have any cards of rank ${exemplar.rank.code}")
                val matchingCards = cardsMatchingRank(otherPlayer.hand.cards(), exemplar.rank)
                if (matchingCards.isEmpty()) {
                    println("${otherPlayer.name} says GO FISH!")
                    pickUpFromOcean(currentPlayer, exemplar)
                } else {
                    println("${otherPlayer.name} gives $matchingCards to ${currentPlayer.name}")
                    transferMatchingCardsToPlayerAsking(matchingCards, otherPlayer, currentPlayer)
                }
            }
        }*/
    }

    fun requestCardsOfMatchingRank(currentPlayer: CardPlayer, requestsFrom: CardPlayer, exemplar: PlayingCard) {
        require(currentPlayer.hand.cards().contains(exemplar)) {"Cannot make a request for card of rank when not holding an example"}

        println("${currentPlayer.name} asks ${requestsFrom.name} whether they have any cards of rank ${exemplar.rank.code}")
        val matchingCards = cardsMatchingRank(requestsFrom.hand.cards(), exemplar.rank)
        if (matchingCards.isEmpty()) {
            println("${requestsFrom.name} says GO FISH!")
            goFishForCardFromOcean(currentPlayer, exemplar)
        } else {
            println("${requestsFrom.name} hands ${matchingCards.size} cards to ${currentPlayer.name}")
            transferMatchingCardsToPlayerAsking(matchingCards, requestsFrom, currentPlayer)
        }
    }

    private fun transferMatchingCardsToPlayerAsking(
        matchingCards: List<PlayingCard>,
        otherPlayer: CardPlayer,
        currentPlayer: CardPlayer
    ) {
        matchingCards.forEach { otherPlayer.hand.transfer(it, currentPlayer.hand) }
    }

    private fun goFishForCardFromOcean(
        currentPlayer: CardPlayer,
        requestedCard: PlayingCard
    ) {
        ocean.firstOrNull()?.let { found ->
            ocean.transfer(found, currentPlayer.hand)
            if (found.rank == requestedCard.rank) {
                println("Player ${currentPlayer.name} found $found with matching rank ${requestedCard.rank} - they have another turn")
                doesCurrentPlayerHaveAnotherTurn = true
            } else {
                println("Player ${currentPlayer.name} picks up card from ocean")
            }
        }
    }

    private fun cardsMatchingRank(cards: List<PlayingCard>, rank: PlayingCardRank) =
        cards.filter { it.rank == rank }

    private fun collectAnyBooks() {
        players.forEach { player ->
            val groups = player.hand.cards().groupBy { it.rank }.filter { it.value.size == 4 }
            groups.forEach { rank, bookCards ->
                val book = CardHolder("Book $rank")
                bookCards.forEach {
                    player.hand.transfer(it, book)
                }
                println("Player ${player.name} found book: $book")
                player.presentations.add(book)
            }
        }
    }

    private fun isAnyCardsInHand() =
        players.any { !it.hand.isEmpty() }

    override fun dump(state: String, currentPlayer: CardPlayer?) {
        println("$turnNumber $state ******************")
        println("ocean=$ocean")
        players.forEach { println("${if (it == currentPlayer) "ACTIVE" else ""} $it") }
    }

    override fun playerScore(it: CardPlayer) =
        it.presentations.cardHolders.size
}

class GoFishBasicStrategy(player: CardPlayer, override val game: CardGameGoFish): CardGamePlayerStrategy(player, game) {
    fun selectPlayerToRequestCards() {
        val requestFrom = game.players.filter { it !== player }.random()
        player.hand.cards().randomOrNull()?.let { exemplar ->
            game.requestCardsOfMatchingRank(player, requestFrom, exemplar)
        }
    }
}
