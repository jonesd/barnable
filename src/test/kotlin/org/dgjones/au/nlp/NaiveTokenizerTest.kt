package org.dgjones.au.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NaiveTokenizerTest {

    @Test
    fun `handle empty sentence without exception`() {
        val words = NaiveTokenizer().splitTextIntoWords("")

        assertEquals(listOf<String>(), words)
    }

    @Test
    fun `handle sequence of words`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two three")

        assertEquals(listOf("one", "two", "three"), words)
    }

    @Test
    fun `detect period for new sentence`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two. three four")

        assertEquals(listOf("one", "two", ".", "three", "four"), words)
    }

    @Test
    fun `ignore period for word in abbreviation list`() {
        val words = NaiveTokenizer().splitTextIntoWords("one Dr. three four")

        assertEquals(listOf("one", "Dr.", "three", "four"), words)
    }

    @Test
    fun `detect multiple sentences`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two. three four. five")

        assertEquals(listOf("one", "two", ".", "three", "four", ".", "five"), words)
    }

    @Test
    fun `do not include trailing white space after sentence`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two.    ")

        assertEquals(listOf("one", "two", "."), words)
    }

    @Test
    fun `split out comma`() {
        val words = NaiveTokenizer().splitTextIntoWords("one two, three four")

        assertEquals(listOf("one", "two", ",", "three", "four"), words)
    }
}