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

package info.dgjones.barnable.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NaiveTokenizerTest {

    @Test
    fun `handle empty sentence without exception`() {
        val words = NaiveTokenizer().splitTextIntoWords("")

        assertEquals(listOf<String>(), words)
    }

    @Test
    fun `handle sequence of words and ensures period at end`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two three")

        assertEquals(listOf("one", "two", "three", "."), words)
    }

    @Test
    fun `detect period for new sentence`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two. three four")

        assertEquals(listOf("one", "two", ".", "three", "four", "."), words)
    }

    @Test
    fun `ignore period for word in abbreviation list`() {
        val words = NaiveTokenizer().splitTextIntoWords("one Dr. three four")

        assertEquals(listOf("one", "Dr.", "three", "four", "."), words)
    }

    @Test
    fun `ignore period for money word`() {
        val words = NaiveTokenizer().splitTextIntoWords("one 1.78 three four")

        assertEquals(listOf("one", "1.78", "three", "four", "."), words)
    }

    @Test
    fun `detect multiple sentences`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two. three four. five")

        assertEquals(listOf("one", "two", ".", "three", "four", ".", "five", "."), words)
    }

    @Test
    fun `considers question mark as sentence end`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two? three")

        assertEquals(listOf("one", "two", "?", "three", "."), words)
    }

    @Test
    fun `considers exclamation as sentence end`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two! three")

        assertEquals(listOf("one", "two", "!", "three", "."), words)
    }

    @Test
    fun `do not include trailing white space after sentence`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two.    ")

        assertEquals(listOf("one", "two", "."), words)
    }

    @Test
    fun `split clause on comma`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two, three four")

        assertEquals(listOf("one", "two", ",", "three", "four", "."), words)
    }

    @Test
    fun `split clause on semicolon`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two; three four")

        assertEquals(listOf("one", "two", ";", "three", "four", "."), words)
    }

    @Test
    fun `split clause on colon`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two: three four")

        assertEquals(listOf("one", "two", ":", "three", "four", "."), words)
    }

    @Test
    fun `splits out paragraphs and sentences`() {
        val textModel = NaiveTokenizer().tokenizeText("""
            paragraph0 sentence0. sentence1.
            paragraph1 sentence2.
            paragraph2 sentence3. sentence4. sentence5
        """.trimIndent())

        assertEquals(3, textModel.paragraphs.size)
        val paragraph0 =  textModel.paragraphs[0]
        assertEquals(2, paragraph0.sentences.size)
        assertEquals("paragraph0 sentence0 .", paragraph0.sentences[0].text)
        assertEquals(3, paragraph0.sentences[0].elements.size)
        assertEquals("sentence1 .", paragraph0.sentences[1].text)
        assertEquals(2, paragraph0.sentences[1].elements.size)
    }
}