package info.dgjones.au.concept

import info.dgjones.au.nlp.WordMorphology
import info.dgjones.au.parser.EntryWord
import info.dgjones.au.parser.LexicalItem
import info.dgjones.au.parser.WordIgnore
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