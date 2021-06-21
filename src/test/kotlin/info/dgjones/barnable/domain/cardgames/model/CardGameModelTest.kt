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

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CardGameModelTest {

    @Nested
    inner class CardDeckBuilderTest {
        @Test
        fun buildsStandardDeckOf52Cards() {
            val cards = CardDeckBuilder().withStandardDeck().build()

            assertEquals(52, cards.size)
        }
        @Test
        fun standardDeckShouldHaveNoDuplicates() {
            val cards = CardDeckBuilder().withStandardDeck().build()

            assertEquals(52, cards.toSet().size)
        }
        @Test
        fun standardDeckGeneratesCardInOrder() {
            val cards = CardDeckBuilder().withStandardDeck().build()
            assertEquals(PlayingCard(RankAce, SuitClub), cards[0])
            assertEquals(PlayingCard(Rank2, SuitClub), cards[1])
            assertEquals(PlayingCard(RankKing, SuitSpade), cards[51])
        }
        @Test
        fun canShuffleOrderOfCards() {
            val sortedCards = CardDeckBuilder().withStandardDeck().build()
            val cards = CardDeckBuilder().withStandardDeck().shuffle().build()

            assertEquals(sortedCards.size, cards.size)
            assertNotEquals(sortedCards, cards)
        }
    }
}
