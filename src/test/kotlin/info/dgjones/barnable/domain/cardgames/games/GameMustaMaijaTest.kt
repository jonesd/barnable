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

package info.dgjones.barnable.domain.cardgames.games

import info.dgjones.barnable.domain.cardgames.game.CardGameMustaMaija
import info.dgjones.barnable.domain.cardgames.game.compareAttackDefend
import info.dgjones.barnable.domain.cardgames.game.mustamaijaCard
import info.dgjones.barnable.domain.cardgames.model.*
import org.junit.jupiter.api.Assertions

import org.junit.jupiter.api.Test

class GameMustaMaijaTest {
    @Test
    fun runGame() {
        //FIXME
//        val game = CardGameMustaMaija(3)
//        game.initGame()
//        game.runGame()
    }
}

class MustaMaijaAttackDefendPairTest {

    @Test
    fun leadingWithMustamaijaAlwaysWins() {
        Assertions.assertTrue(compareAttackDefend(mustamaijaCard, PlayingCard(RankKing, SuitHeart), SuitHeart))
    }
    @Test
    fun defendingWithMustamaijaAlwaysLoses() {
        Assertions.assertTrue(compareAttackDefend(PlayingCard(RankKing, SuitDiamond), mustamaijaCard, SuitHeart))
    }
    @Test
    fun higherRankSameSuitWins() {
        Assertions.assertTrue(compareAttackDefend(PlayingCard(Rank10, SuitDiamond), PlayingCard(Rank9, SuitDiamond), SuitHeart))
        Assertions.assertFalse(compareAttackDefend(PlayingCard(Rank9, SuitDiamond), PlayingCard(Rank10, SuitDiamond), SuitHeart))
    }
    @Test
    fun sameRankSameSuitConsideredAttackerWin() {
        Assertions.assertTrue(compareAttackDefend(PlayingCard(Rank10, SuitDiamond), PlayingCard(Rank10, SuitDiamond), SuitHeart))
    }
    @Test
    fun lowerTrumpBeatsNonTrump() {
        Assertions.assertTrue(compareAttackDefend(PlayingCard(Rank2, SuitDiamond), PlayingCard(RankKing, SuitHeart), SuitDiamond))
        Assertions.assertFalse(compareAttackDefend(PlayingCard(RankKing, SuitHeart), PlayingCard(Rank2, SuitDiamond), SuitDiamond))
    }
    @Test
    fun higherValueBeatsLowerValue() {
        Assertions.assertTrue(compareAttackDefend(PlayingCard(RankKing, SuitDiamond), PlayingCard(RankQueen, SuitHeart), SuitClub))
        Assertions.assertFalse(compareAttackDefend(PlayingCard(RankQueen, SuitHeart), PlayingCard(RankKing, SuitDiamond), SuitClub))
    }
}
