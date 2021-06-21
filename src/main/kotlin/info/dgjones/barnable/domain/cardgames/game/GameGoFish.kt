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

class GameGoFish(private val numberOfPlayers: Int = 7) {
    private val players = mutableListOf<CardPlayer>()
    private val ocean = CardHolder("ocean")
    private var turnNumber = 1
    private var anotherTurn = false

    fun runGame() {
        repeat(numberOfPlayers) {
            players.add(CardPlayer("Player $it"))
        }
        val allCards = CardDeckBuilder().withStandardDeck().shuffle().build()
        val deck = CardHolder("deck", allCards)
        val numberOfCards = if (numberOfPlayers <= 3) 7 else 5
        FixedDeal().dealCardsToPlayerHand(numberOfCards, deck,  players, ocean)
        var currentPlayer = players[0]
        collectAnyBooks()

        while (isAnyCardsInHand()) {
            dump("Start Turn", currentPlayer)
            requestCardsOfMatchingRank(currentPlayer)
            collectAnyBooks()

            turnNumber += 1
            if (!anotherTurn) {
                currentPlayer = nextPlayer(currentPlayer)
            }
            anotherTurn = false
        }
        printWinner()
    }

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
                    matchingCards.forEach { otherPlayer.hand.transfer(it, currentPlayer.hand) }
                }
            }
        }
    }

    private fun pickUpFromOcean(
        currentPlayer: CardPlayer,
        exemplar: PlayingCard
    ) {
        ocean.firstOrNull()?.let { found ->
            ocean.transfer(found, currentPlayer.hand)
            if (found.rank == exemplar.rank) {
                println("Player ${currentPlayer.name} found matching card $found - they have another turn")
                anotherTurn = true
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

    private fun nextPlayer(currentPlayer: CardPlayer): CardPlayer {
        val nextPlayerIndex = (players.indexOf(currentPlayer) + 1) % players.size
        return players[nextPlayerIndex]
    }

    private fun isAnyCardsInHand() =
        players.any { !it.hand.isEmpty() }

    private fun dump(state: String, currentPlayer: CardPlayer?) {
        println("$turnNumber $state ******************")
        println("ocean=$ocean")
        players.forEach { println("${if (it == currentPlayer) "ACTIVE" else ""} $it") }
    }

    private fun printWinner() {
        players.forEach {
            println("${it.name} score: ${it.presentations.cardHolders.size}")
        }
    }
}
