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
    val strategies = mutableListOf<CardGamePlayerStrategy>()

    fun initGame() {
        createPlayers()
    }

    fun runGame() {
        check(players.isNotEmpty()) {"Game must have 1 or more players" }
        val deck = CardHolder("deck", createDeckCards())
        runAdditionalSetup()
        dealCards(deck)
        var currentPlayer = players[0]
        runBeforeFirstTurn()

        runSpecialBehavioursAtAnytime()

        runGameLoop(currentPlayer)
        printWinner()
    }

    private fun runGameLoop(currentPlayer: CardPlayer) {
        var currentPlayer1 = currentPlayer
        while (!isGameFinished()) {
            dump("Start Turn", currentPlayer1)
            runPlayerTurn(currentPlayer1)
            endOfPlayerTurn(currentPlayer1)
            runSpecialBehavioursAtAnytime()

            val nextPlayer = nextTurn(currentPlayer1)
            if (nextPlayer == null) {
                break
            } else {
                currentPlayer1 = nextPlayer
            }
        }
    }

    open fun runAdditionalSetup() {
        // nothing by default
    }

    open fun runBeforeFirstTurn() {
        // nothing by default
    }

    fun nextTurn(currentPlayer: CardPlayer): CardPlayer? {
        var nextPlayer: CardPlayer? = currentPlayer
        turnNumber += 1
        if (!doesCurrentPlayerHaveAnotherTurn) {
            nextPlayer = nextActivePlayer(currentPlayer)
        }
        doesCurrentPlayerHaveAnotherTurn = false
        if (shouldAbortGame()) {
            println("Aborted game...")
            return null
        }
        return nextPlayer
    }

    protected open fun shouldAbortGame() =
        turnNumber > 2000

    abstract fun runPlayerTurn(currentPlayer: CardPlayer)

    protected open fun runSpecialBehavioursAtAnytime() {
        // do nothing by default
    }

    private fun createPlayers() {
        repeat(numberOfPlayers) {
            val cardPlayer = createCardPlayer(it)
            players.add(cardPlayer)
            strategies.add(createStrategy(it, cardPlayer))
        }
    }

    open fun createStrategy(index: Int, cardPlayer: CardPlayer): CardGamePlayerStrategy {
        return CardGamePlayerStrategy(cardPlayer, this)
    }

    open fun createCardPlayer(it: Int) =
        CardPlayer("Player $it")

    open fun nextActivePlayer(currentPlayer: CardPlayer): CardPlayer? {
        // FIXME
        var previousPlayer = currentPlayer
        do {
            previousPlayer = nextPlayerAroundTable(previousPlayer)
            if (previousPlayer == currentPlayer) {
                return null
            } else if (previousPlayer != null && !previousPlayer.out) {
                return previousPlayer
            }
        } while (true)
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
            println("Player ${it.name} score: ${playerScore(it)}")
        }
    }

    abstract fun playerScore(it: CardPlayer): Int

    open fun endOfPlayerTurn(currentPlayer: CardPlayer) {
        // nothing by default
    }
}

open class CardGamePlayerStrategy(open val player: CardPlayer, open val game: CardGameRunner)
