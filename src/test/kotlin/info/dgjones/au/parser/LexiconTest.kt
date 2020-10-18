package info.dgjones.au.parser

import info.dgjones.au.nlp.WordMorphology
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexiconTest {
    @Test
    fun `can lookup simple word`() {
        val lexicon = Lexicon()
        val handler = withWordMapping(lexicon, "test")

        // test
        val lexicalItems = lexicon.lookupInitialWord("test")

        assertEquals(1, lexicalItems.size)
        assertEquals("test", lexicalItems.first().morphologies.first().full)
        assertEquals(handler, lexicalItems.first().handler)
    }

    @Test
    fun `can be multiple matching simple words`() {
        val lexicon = Lexicon()
        val handler0 = withWordMapping(lexicon, "test")
        val handler1 = withWordMapping(lexicon, "test")

        // test
        val lexicalItems = lexicon.lookupInitialWord("test")

        assertEquals(2, lexicalItems.size)
        assertEquals("test", lexicalItems.first().morphologies.first().full)
        val foundHandlers = lexicalItems.map { it.handler }
        assertTrue(foundHandlers.containsAll(listOf(handler0, handler1)))
        assertEquals(handler0, lexicalItems.first().handler)
    }

    @Test
    fun `returns empty list when no matches for word`() {
        val lexicon = Lexicon()
        val handler = withWordMapping(lexicon, "test")

        // test
        val lexicalItems = lexicon.lookupInitialWord("otherWord")

        assertEquals(0, lexicalItems.size)
    }

    @Test
    fun `can match single word by suffix`() {
        val lexicon = Lexicon()
        val handler = withWordMapping(lexicon, "test")

        // test
        val lexicalItems = lexicon.lookupInitialWord("testing")

        assertEquals(1, lexicalItems.size)
        assertEquals(1, lexicalItems.first().morphologies.size)
        val morphologyWord0 = lexicalItems.first().morphologies.first()
        assertEquals(WordMorphology("test", "ing", "testing"), morphologyWord0)
        assertEquals(handler, lexicalItems.first().handler)
    }

    @Test
    fun `can match multiple variants of word by suffix`() {
        val lexicon = Lexicon()
        val handler0 = withWordMapping(lexicon, "test")
        val handler1 = withWordMapping(lexicon, "test")

        // test
        val lexicalItems = lexicon.lookupInitialWord("testing")

        assertEquals(2, lexicalItems.size)

        val lexicalItem0 = lexicalItems[0]
        assertEquals(1, lexicalItem0.morphologies.size)
        val morphologyWord0 = lexicalItem0.morphologies.first()
        assertEquals(WordMorphology("test", "ing", "testing"), morphologyWord0)
        assertEquals(handler0, lexicalItem0.handler)

        val lexicalItem1 = lexicalItems[1]
        assertEquals(handler1, lexicalItem1.handler)
    }

    @Test
    fun `maps a stream of input words to lexical expression`() {
        val lexicon = Lexicon()
        val handler0 = withWordMapping(lexicon, "one", listOf("one", "two"))

        // test
        val lexicalItems = lexicon.lookupNextEntry(listOf("one", "two", "three"))

        assertEquals(1, lexicalItems.size)
        val lexical0 = lexicalItems.first()
        assertEquals(handler0, lexical0.handler)
        println(lexical0.handler.word)
    }

    @Test
    fun `maps a stream of input words to lexical expression supports morphological for subsequent words of expression`() {
        val lexicon = Lexicon()
        val handler0 = withWordMapping(lexicon, "one", listOf("one", "two"))
        val twoWithSuffix = "twoing"

        // test
        val sentence = listOf("one", twoWithSuffix, "three")
        val lexicalItems = lexicon.lookupNextEntry(sentence)

        assertEquals(1, lexicalItems.size)
        val lexical0 = lexicalItems.first()
        assertEquals(handler0, lexical0.handler)
        println(lexical0.handler.word)
    }

    @Test
    fun `maps a stream of input words to lexical items fails if subsequent input words do not match`() {
        val lexicon = Lexicon()
        val handler0 = withWordMapping(lexicon, "one", listOf("one", "two"))

        // test
        val lexicalItems = lexicon.lookupNextEntry(listOf("one", "other", "other2"))

        assertEquals(0, lexicalItems.size)
    }

    @Test
    fun `maps a stream of input words to lexical items`() {
        val lexicon = Lexicon()
        val handler0 = withWordMapping(lexicon, "one", listOf("one", "two"))
        val handler1 = withWordMapping(lexicon, "one")

        val handler2 = withWordMapping(lexicon, "one", listOf("one", "other"))

        // test
        val lexicalItems = lexicon.lookupNextEntry(listOf("one", "two", "three"))

        assertEquals(2, lexicalItems.size)
        val lexical0 = lexicalItems[0]
        assertEquals(handler0, lexical0.handler)
        assertEquals(listOf("one", "two"), lexical0.morphologies.map {it.full})

        val lexical1 = lexicalItems[1]
        assertEquals(handler1, lexical1.handler)
        assertEquals(listOf("one"), lexical1.morphologies.map {it.full})
    }

    @Test
    fun `no suffix words should only match entry word`() {
        val lexicon = Lexicon()
        val handlerMrs = withWordMapping(lexicon, "mrs", noSuffix = true)
        val handlerMr = withWordMapping(lexicon, "mr", noSuffix = true)

        // test
        val lexicalItems = lexicon.lookupInitialWord("mrs")

        assertEquals(1, lexicalItems.size)
        assertEquals(1, lexicalItems.first().morphologies.size)
        val morphologyWord0 = lexicalItems.first().morphologies.first()
        assertEquals(WordMorphology("mrs", "", "mrs"), morphologyWord0)
        assertEquals(handlerMrs, lexicalItems.first().handler)
    }

    @Test
    fun `should not generate suffixes for noSuffix word`() {
        val lexicon = Lexicon()
        val handlerMr = withWordMapping(lexicon, "mr", noSuffix = true)

        // test
        val lexicalItems = lexicon.lookupInitialWord("mrs")

        assertEquals(0, lexicalItems.size)
    }

    // FIXME what about word handler special overrides - past, extra....
/*
    @Test
    fun `can match on precise phrase match`() {
        val lexicon = Lexicon()
        val handler = WordHandler(EntryWord("measure", listOf("measure", "to", "eat")))
        lexicon.addMapping(handler)

        // test
        val lexicalItems = lexicon.lookupWord("testing")


    }
    */

    private fun withWordMapping(lexicon: Lexicon, word: String, expression: List<String> = listOf(word), noSuffix:Boolean = false): WordHandler {
        val handler = WordHandler(EntryWord(word, expression, noSuffix = noSuffix))
        lexicon.addMapping(handler)
        return handler
    }
}