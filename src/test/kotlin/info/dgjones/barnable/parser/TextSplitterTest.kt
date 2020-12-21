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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextSplitterTest {
    @Test
    fun `Splits sentence down to individual word handlers`() {
        val lexicon = Lexicon()
        val handler0 = withWordMapping(lexicon, "zero")
        val handler1 = withWordMapping(lexicon, "one")
        val handler2 = withWordMapping(lexicon, "two")
        val textSplitter = TextSplitter(lexicon)

        // test zero
        val splits = textSplitter.split(buildTextModel("zero one two").initialSentence())

        assertEquals(3, splits.units.size)

        val unit0 = splits.units[0]
        assertEquals("zero", unit0.first().morphologies.first().full)
        assertEquals(handler0, unit0.first().handler)

        val unit1 = splits.units[1]
        assertEquals("one", unit1.first().morphologies.first().full)
        assertEquals(handler1, unit1.first().handler)

        val unit2 = splits.units[2]
        assertEquals("two", unit2.first().morphologies.first().full)
        assertEquals(handler2, unit2.first().handler)
    }

    @Test
    fun `Favour longer expressions`() {
        val lexicon = Lexicon()
        withWordMapping(lexicon, "zero")
        withWordMapping(lexicon, "one")
        val handler2 = withWordMapping(lexicon, "two")
        val handler01 = withWordMapping(lexicon, "zero", listOf("zero", "one"))
        val textSplitter = TextSplitter(lexicon)

        // test zero
        val splits = textSplitter.split(buildTextModel("zero one two").initialSentence())

        assertEquals(2, splits.units.size)

        val unit0 = splits.units[0]
        assertEquals("zero", unit0.first().morphologies.first().full)
        assertEquals(2, unit0.first().morphologies.size)
        assertEquals("one", unit0.first().morphologies[1].full)
        assertEquals(1, unit0.size)
        assertEquals(handler01, unit0.first().handler)

        val unit1 = splits.units[1]
        assertEquals("two", unit1.first().morphologies.first().full)
        assertEquals(handler2, unit1.first().handler)
    }

    private fun withWordMapping(lexicon: Lexicon, word: String, expression: List<String> = listOf(word)): WordHandler {
        val handler = WordHandler(EntryWord(word, expression))
        lexicon.addMapping(handler)
        return handler
    }
}