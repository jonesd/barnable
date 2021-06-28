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

abstract class CardGameRunner(val numberOfPlayers: Int = 1) {
    val players = mutableListOf<CardPlayer>()
    var turnNumber = 1
    var doesCurrentPlayerHaveAnotherTurn = false

    fun runGame() {
        createPlayers()
        val deck = CardHolder("deck", createDeckCards())
        runAdditionalSetup()
        dealCards(deck)
        var currentPlayer = players[0]
        runBeforeFirstTurn()

        runSpecialBehavioursAtAnytime()

        while (!isGameFinished()) {
            dump("Start Turn", currentPlayer)
            runPlayerTurn(currentPlayer)
            runSpecialBehavioursAtAnytime()

            currentPlayer = nextTurn(currentPlayer)
        }
        printWinner()
    }

    open fun runAdditionalSetup() {
        // nothing by default
    }

    open fun runBeforeFirstTurn() {
        // nothing by default
    }

    fun nextTurn(currentPlayer: CardPlayer): CardPlayer {
        var currentPlayer1 = currentPlayer
        turnNumber += 1
        if (!doesCurrentPlayerHaveAnotherTurn) {
            currentPlayer1 = nextActivePlayer(currentPlayer1)
        }
        doesCurrentPlayerHaveAnotherTurn = false
        return currentPlayer1
    }

    protected abstract fun runPlayerTurn(currentPlayer: CardPlayer)
    protected open fun runSpecialBehavioursAtAnytime() {
        // do nothing by default
    }

    protected fun createPlayers() {
        repeat(numberOfPlayers) {
            players.add(CardPlayer("Player $it"))
        }
    }

    open fun nextActivePlayer(currentPlayer: CardPlayer): CardPlayer? {
        do
        val previousPlayer = currentPlayer
        val nextPlayer = nextPlayerAroundTable(previousPlayer)
        if (nextPlayer.out)
        return nextPlayerAroundTable(currentPlayer)
    }

    private fun nextPlayerAroundTable(currentPlayer: CardPlayer): CardPlayer {
        val nextPlayerIndex = nextPlayerIndex(currentPlayer)
        return players[nextPlayerIndex]
    }

    private fun nextPlayerIndex(player: CardPlayer) =
        (players.indexOf(player) + 1) % players.size

    protected open fun createDeckCards() = CardDeckBuilder().withStandardDeck().shuffle().build()
    abstract fun dealCards(deck: CardHolder)
    abstract fun isGameFinished(): Boolean

    open fun dump(state: String, currentPlayer: CardPlayer?) {
        println("$turnNumber $state ******************")
        players.forEach {
            println("${if (it == currentPlayer) "ACTIVE" else ""} $it")
        }
        dumpExtraTurnState(state, currentPlayer)
    }

    open fun dumpExtraTurnState(state: String, currentPlayer: CardPlayer?) {
        // nothing by default
    }

    fun printWinner() {
        players.forEach {
            println("${it.name} score: ${playerScore(it)}")
        }
    }

    abstract fun playerScore(it: CardPlayer): Int
}

