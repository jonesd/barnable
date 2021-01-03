/*
 * Copyright  2020 David G Jones
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

package info.dgjones.barnable.parser

import info.dgjones.barnable.episodic.EpisodicMemory
import info.dgjones.barnable.nlp.TextSentence
import info.dgjones.barnable.nlp.WordElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DemonTest {
    @Test
    fun `demon can describe itself`() {
        val demon = TestDemon(withWordContext())

        assertEquals("test description", demon.description())
        assertEquals("{demon0/0=test description, active=true, priority=false}", demon.toString())
    }

    @Test
    fun `demon can be deactivated`() {
        val demon = TestDemon(withWordContext())

        assertTrue(demon.active)

        // test
        demon.deactivate()

        assertFalse(demon.active)
    }

    @Test
    fun `demon can be high priority`() {
        val demon = TestDemon(withWordContext(), true)

        assertEquals("test description", demon.description())
        assertEquals("{demon0/0=test description, active=true, priority=true}", demon.toString())
    }
}

class TestDemon(wordContext: WordContext, highPriority: Boolean = false): Demon(wordContext, highPriority) {
    override fun description(): String {
        return "test description"
    }
}

fun withWordContext(): WordContext {
    val sentence = TextSentence("test", listOf<WordElement>())
    val sentenceContext = SentenceContext(sentence, WorkingMemory(), EpisodicMemory(), false)
    return WordContext(1, "test", ConceptHolder(0), sentenceContext)
}