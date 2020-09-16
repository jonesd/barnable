package info.dgjones.au.parser

import info.dgjones.au.nlp.WordMorphology
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexiconTest {
    @Test
    fun `can lookup simple word`() {
        val lexicon = Lexicon()
        val handler = WordHandler(EntryWord("test"))
        lexicon.addMapping(handler)

        // test
        val lexicalItems = lexicon.lookupWord("test")

        assertEquals(1, lexicalItems.size)
        assertEquals("test", lexicalItems.first().morphologies.first().full)
        assertEquals(handler, lexicalItems.first().handler)
    }

    fun `can be multiple matching simple words`() {
        val lexicon = Lexicon()
        val handler0 = WordHandler(EntryWord("test"))
        lexicon.addMapping(handler0)
        val handler1 = WordHandler(EntryWord("test"))
        lexicon.addMapping(handler1)

        // test
        val lexicalItems = lexicon.lookupWord("test")

        assertEquals(2, lexicalItems.size)
        assertEquals("test", lexicalItems.first().morphologies.first().full)
        val foundHandlers = lexicalItems.map { it.handler }
        assertTrue(foundHandlers.containsAll(listOf(handler0, handler1)))
        assertEquals(handler0, lexicalItems.first().handler)
    }

    @Test
    fun `returns empty list when no matches for word`() {
        val lexicon = Lexicon()
        val handler = WordHandler(EntryWord("test"))
        lexicon.addMapping(handler)

        // test
        val lexicalItems = lexicon.lookupWord("otherWord")

        assertEquals(0, lexicalItems.size)
    }

    @Test
    fun `can match single word by suffix`() {
        val lexicon = Lexicon()
        val handler = WordHandler(EntryWord("test"))
        lexicon.addMapping(handler)

        // test
        val lexicalItems = lexicon.lookupWord("testing")

        assertEquals(1, lexicalItems.size)
        assertEquals(1, lexicalItems.first().morphologies.size)
        val morphologyWord0 = lexicalItems.first().morphologies.first()
        assertEquals(WordMorphology("test", "ing", "testing"), morphologyWord0)
        assertEquals(handler, lexicalItems.first().handler)
    }
}