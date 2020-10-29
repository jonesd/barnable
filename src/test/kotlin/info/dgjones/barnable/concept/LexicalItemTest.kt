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

package info.dgjones.barnable.concept

import info.dgjones.barnable.nlp.WordMorphology
import info.dgjones.barnable.parser.EntryWord
import info.dgjones.barnable.parser.LexicalItem
import info.dgjones.barnable.parser.WordIgnore
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexicalItemTest {
    @Test
    fun `text fragment for no words should be blank`() {
        val lexicalItem = LexicalItem(listOf(), WordIgnore(EntryWord("test")))

        assertEquals("", lexicalItem.textFragment())
    }

    @Test
    fun `text fragment of single word`() {
        val lexicalItem = LexicalItem(listOf(WordMorphology("test")), WordIgnore(EntryWord("test")))

        assertEquals("test", lexicalItem.textFragment())
    }

    @Test
    fun `text fragment of single word with suffix`() {
        val lexicalItem = LexicalItem(listOf(WordMorphology("test", "test", "testing")), WordIgnore(EntryWord("test")))

        assertEquals("testing", lexicalItem.textFragment())
    }

    @Test
    fun `text fragment of expression word`() {
        val lexicalItem = LexicalItem(
            listOf(
                WordMorphology("test", "ing", "testing"),
                WordMorphology("is"),
                WordMorphology("need", "ed", "needed")
            ),
            WordIgnore(EntryWord("test"))
        )

        assertEquals("testing is needed", lexicalItem.textFragment())
    }
}