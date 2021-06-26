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

import info.dgjones.barnable.domain.cardgames.model.CardDeckBuilder
import info.dgjones.barnable.domain.cardgames.model.CardHolder
import info.dgjones.barnable.domain.cardgames.model.CardPlayer
import info.dgjones.barnable.domain.cardgames.model.FixedDeal

/**
 * Musta Maija (Black Mary) is a Finnish card game.
 * https://wikipedia.org/wiki/Musta_Maija
 */
class CardGameMustaMaija(numberOfPlayers: Int) : CardGameRunner(numberOfPlayers) {
    private val stock = CardHolder("stock", faceUp = false)

    override fun runPlayerTurn(currentPlayer: CardPlayer) {
        TODO("Not yet implemented")
    }

    override fun dealCards(deck: CardHolder) {
        val numberOfCards = dealNumberOfCardsPerPlayer()
        FixedDeal().dealCardsToPlayerHand(numberOfCards, deck, players, stock)
    }

    override fun runAdditionalSetup() {
        trumpSuit = stock.transferFirst(1, stock)
    }

    private fun dealNumberOfCardsPerPlayer() = 5

    override fun isGameFinished(): Boolean {
        TODO("Not yet implemented")
    }

    override fun playerScore(it: CardPlayer): Int {
        TODO("Not yet implemented")
    }
}
