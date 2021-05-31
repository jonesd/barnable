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

import info.dgjones.barnable.domain.cardgames.game.GameSnipSnapSnorum
import info.dgjones.barnable.parser.Lexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class CardGameDomainTest {
    val lexicon = buildLexicon()

    private fun buildLexicon(): Lexicon {
        val lexicon = Lexicon()
        CardGameDomain().buildDomainLexicon(lexicon)
        return lexicon
    }

    @Nested
    inner class DuplicateResolved {
        @Test
        fun `Run Game Builder`() {
            val textProcessor = runTextProcess(GameSnipSnapSnorum().source.content, lexicon)

            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        }
    }
}