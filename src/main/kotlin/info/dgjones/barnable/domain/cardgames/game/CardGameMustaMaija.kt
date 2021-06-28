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

/**
 * Musta Maija (Black Mary) is a Finnish card game.
 * https://wikipedia.org/wiki/Musta_Maija
 */
class CardGameMustaMaija(numberOfPlayers: Int, val handSize: Int = 5) : CardGameRunner(numberOfPlayers) {
    private val stock = CardHolder("stock", faceUp = false)
    private var trumpSuit: PlayingCardSuit = SuitSpade

    override fun runPlayerTurn(currentPlayer: CardPlayer) {
        if (currentPlayer.hand.isEmpty() && stock.isEmpty()) {
            // player is out
            return
        }
        val transferred = replenishHandFromStock(currentPlayer)
        if (transferred.isEmpty()) {
            playAvailableCards(currentPlayer)
        }
    }

    private fun playAvailableCards(currentPlayer: CardPlayer) {
        val defender = activePlayerToLeft(currentPlayer)
    }

    private fun replenishHandFromStock(currentPlayer: CardPlayer): List<PlayingCard> {
        val shortCards = handSize - currentPlayer.hand.size()
        if (shortCards > 0) {
            val transferred = stock.transferFirst(shortCards, currentPlayer.hand)
            println("$currentPlayer picks up ${transferred.size} cards from ${stock.name}")
            return transferred
        } else {
            return listOf<PlayingCard>()
        }
    }

    override fun dealCards(deck: CardHolder) {
        val numberOfCards = dealNumberOfCardsPerPlayer()
        FixedDeal().dealCardsToPlayerHand(numberOfCards, deck, players, stock)
    }

    override fun runAdditionalSetup() {
        turnUpTopCardOfDeckAsTrumpSuit()
    }

    private fun turnUpTopCardOfDeckAsTrumpSuit() {
        val transferred = stock.transferFirst(1, stock)
        if (transferred.isNotEmpty()) {
            // FIXME should leave trump face up at bottom of stock
            trumpSuit = transferred.first().suit
        }
    }

    private fun dealNumberOfCardsPerPlayer() = 5

    override fun isGameFinished(): Boolean {
        return isOnlyOnePlayerHoldingCards() && stock.isEmpty()
    }

    private fun isOnlyOnePlayerHoldingCards() =
        players.filter { !it.hand.isEmpty() }.size == 1

    override fun playerScore(it: CardPlayer): Int {
        TODO("Not yet implemented")
    }
}

class MustaMaijaStrategy() {

}