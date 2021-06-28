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

package info.dgjones.barnable.domain.cardgames

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PlayingCardTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Ace of clubs`() {
        val textProcessor = runTextProcess("ace of clubs", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val card = textProcessor.workingMemory.concepts[0]
        assertContainsRooted(card, concept(PlayingCardConcept.PlayingCard.name) {
            slot(PlayingCardFields.Rank,  PlayingCardConcept.Rank.name) {
                slot(CoreFields.Name, CardNamedRank.Ace.name)
                slot(NumberFields.Value, "1") }
            slot(PlayingCardFields.Suit, PlayingCardConcept.Suit.name) {
                slot(CoreFields.Name, CardSuit.Club.name)
            }
        })
    }
}
