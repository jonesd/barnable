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

    private fun requestCardsOfMatchingRank(currentPlayer: CardPlayer) {
        val otherPlayer = selectAnotherPlayer(currentPlayer)
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
        }
    }

    private fun transferMatchingCardsToPlayerAsking(
        matchingCards: List<PlayingCard>,
        otherPlayer: CardPlayer,
        currentPlayer: CardPlayer
    ) {
        matchingCards.forEach { otherPlayer.hand.transfer(it, currentPlayer.hand) }
    }

    private fun pickUpFromOcean(
        currentPlayer: CardPlayer,
        exemplar: PlayingCard
    ) {
        ocean.firstOrNull()?.let { found ->
            ocean.transfer(found, currentPlayer.hand)
            if (found.rank == exemplar.rank) {
                println("Player ${currentPlayer.name} found matching card $found - they have another turn")
                doesCurrentPlayerHaveAnotherTurn = true
            } else {
                println("Player ${currentPlayer.name} picks up card from ocean")
            }
        }
    }

    private fun cardsMatchingRank(cards: List<PlayingCard>, rank: PlayingCardRank) =
        cards.filter { it.rank == rank }

    private fun selectAnotherPlayer(currentPlayer: CardPlayer) =
        players.filter { it !== currentPlayer }.random()

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
