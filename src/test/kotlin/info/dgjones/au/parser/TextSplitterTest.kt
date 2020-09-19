package info.dgjones.au.parser

import info.dgjones.au.nlp.WordMorphology
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextSplitterTest() {
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

    private fun withWordMapping(lexicon: Lexicon, word: String, expression: List<String> = listOf(word)): WordHandler {
        val handler = WordHandler(EntryWord(word, expression))
        lexicon.addMapping(handler)
        return handler
    }
}