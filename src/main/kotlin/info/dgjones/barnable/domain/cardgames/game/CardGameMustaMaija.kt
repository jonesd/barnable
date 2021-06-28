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
import info.dgjones.barnable.domain.cardgames.model.aceHighComparator;

/**
 * Musta Maija (Black Mary) is a Finnish card game.
 * https://wikipedia.org/wiki/Musta_Maija
 */
class CardGameMustaMaija(numberOfPlayers: Int, val handSize: Int = 5) : CardGameRunner(numberOfPlayers) {
    private val stock = CardHolder("stock", faceUp = false)
    private val discard = CardHolder("discard", faceUp = false)
    private var trumpSuit: PlayingCardSuit = SuitSpade

    override fun runPlayerTurn(currentPlayer: CardPlayer) {
        if (currentPlayer.hand.isEmpty() && stock.isEmpty()) {
            // player is out
            return
        }
        val defender = nextActivePlayer(currentPlayer)
        if (defender != null) {
            strategyFor(currentPlayer).playTurn(defender, defender.hand.size())
        } else {
            println("No defender against player ${currentPlayer.name}")
        }
    }

    override fun createStrategy(index: Int, cardPlayer: CardPlayer): CardGamePlayerStrategy {
        return MustaMaijaBasicStrategy(cardPlayer, this)
    }

    private fun strategyFor(player: CardPlayer) =
        strategies.first { it.player == player } as MustaMaijaBasicStrategy

    fun playTrick(attacker: CardPlayer, defender: CardPlayer, ledCards: List<PlayingCard>) {
        require(ledCards.size <= defender.hand.size()) {"Attacker ${attacker.name} must not play more cards than defender ${defender.name}"}
        val attackPairs = ledCards.map { MustaMaijaAttackDefendPair(it, trumpSuit) }
        strategyFor(defender).defendAgainst(attacker, attackPairs)
    }

    fun defenderResponse(attacker: CardPlayer, defender: CardPlayer, pairs: List<MustaMaijaAttackDefendPair>) {
        val discardAttacker = mutableListOf<PlayingCard>()
        val discardDefender = mutableListOf<PlayingCard>()
        val pickupDefender = mutableListOf<PlayingCard>()
        pairs.forEach { pair ->
            if (pair.isAttackerVictorious()) {
                println("Attacker ${attacker.name} ${pair.led} beats ${defender.name} ${pair.defend}")
                pickupDefender.add(pair.led)
            } else {
                println("Attacker ${attacker.name} ${pair.led} loses ${defender.name} ${pair.defend}")
                discardAttacker.add(pair.led)
            }
            pair.defend?.let {
                discardDefender.add(it)
            }
        }
        println("Transfer ${discardAttacker} from ${attacker.name} to $discard")
        attacker.hand.transfer(discardAttacker, discard)
        println("Transfer ${discardDefender} from ${defender.name} to $discard")
        defender.hand.transfer(discardDefender, discard)
        println("Transfer ${pickupDefender} from ${attacker.name} to ${defender.name} ${defender.hand}")
        attacker.hand.transfer(pickupDefender, defender.hand)
    }

    fun replenishHandFromStock(currentPlayer: CardPlayer): List<PlayingCard> {
        val shortCards = handSize - currentPlayer.hand.size()
        return if (shortCards > 0) {
            val transferred = stock.transferFirst(shortCards, currentPlayer.hand)
            println("$currentPlayer picks up ${transferred.size} cards from ${stock.name}")
            transferred
        } else {
            listOf<PlayingCard>()
        }
    }

    override fun dealCards(deck: CardHolder) {
        val numberOfCards = dealNumberOfCardsPerPlayer()
        FixedDeal().dealCardsToPlayerHand(numberOfCards, deck, players, stock)
    }

    override fun runBeforeFirstTurn() {
        super.runBeforeFirstTurn()
        turnUpTopCardOfDeckAsTrumpSuit()
    }

    private fun turnUpTopCardOfDeckAsTrumpSuit() {
        val transferred = stock.transferFirst(1, stock)
        if (transferred.isNotEmpty()) {
            // FIXME should leave trump face up at bottom of stock
                //FIXME ignore spades?
            trumpSuit = transferred.first().suit
        }
    }

    private fun dealNumberOfCardsPerPlayer() = 5

    override fun isGameFinished(): Boolean {
        return isOnlyOnePlayerHoldingCards() && stock.isEmpty()
    }

    private fun isOnlyOnePlayerHoldingCards() =
        players.filter { !it.hand.isEmpty() }.size == 1

    override fun playerScore(player: CardPlayer) =
        if (player.out) 1 else 0

    fun isStockEmpty() = stock.isEmpty()

    override fun dump(state: String, currentPlayer: CardPlayer?) {
        super.dump(state, currentPlayer)
        println("trumps=${trumpSuit.symbol}")
        println("stock=${stock.cards()}")
        println("discard=${discard.cards()}")
    }
}

val mustamaijaCard = PlayingCard(RankQueen, SuitSpade)

fun compareAttackDefend(led: PlayingCard, defend: PlayingCard?, trump: PlayingCardSuit): Boolean {
    val pair = MustaMaijaAttackDefendPair(led, trump)
    pair.defend = defend
    return pair.isAttackerVictorious()
}

class MustaMaijaAttackDefendPair(val led: PlayingCard, val trump: PlayingCardSuit) {
    var defend: PlayingCard? = null

    fun isAttackerVictorious(): Boolean {
        defend?.let { defend ->
            if (led == mustamaijaCard) {
                return true
            }
            if (defend == mustamaijaCard) {
                return true
            }
            if (isTrump(led) && !isTrump(defend)) {
                return true
            }
            if (isTrump(defend) && !isTrump(led)) {
                return false;
            }
            return isHigherRank(led, defend)
        }
        return true
    }

    private fun isHigherRank(a: PlayingCard, b: PlayingCard) =
        aceHighComparator.compare(a.rank, b.rank) >= 0

    private fun isTrump(card: PlayingCard) =
        card.suit == trump
}

class MustaMaijaBasicStrategy(player: CardPlayer, override val game: CardGameMustaMaija): CardGamePlayerStrategy(player, game) {
    fun playTurn(defender: CardPlayer, defenderCards: Int) {
        if (player.hand.size() < game.handSize && !game.isStockEmpty()) {
            game.replenishHandFromStock(player)
        } else if (defenderCards > 0) {
            playAvailableCardsAgainst(defender, defenderCards)
        }
    }
    fun playAvailableCardsAgainst(defender: CardPlayer, defenderCards: Int) {
        val cards = minOf(player.hand.size(), defenderCards)
        val attacking = player.hand.cards().subList(0, cards)
        game.playTrick(player, defender, attacking)
    }

    fun defendAgainst(attacker: CardPlayer, attackPairs: List<MustaMaijaAttackDefendPair>) {
        val available = player.hand.cards().toMutableList()
        attackPairs.forEach { pair ->
            val card = available.firstOrNull { defend -> !compareAttackDefend(pair.led, defend, pair.trump)}
            card?.let {
                pair.defend = card
                available.remove(card)
            }
        }
        game.defenderResponse(attacker, player, attackPairs)
    }

}
