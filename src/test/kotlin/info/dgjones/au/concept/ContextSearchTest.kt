package info.dgjones.au.concept

import info.dgjones.au.nlp.TextSentence
import info.dgjones.au.nlp.WordElement
import info.dgjones.au.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SearchContextTest {
    val matcher = matchConceptByHead("testHead")

    @Test
    fun `Can match concept after`() {
        val sentenceContext = withSentenceContext(2)
        val currentWord = withWordContext(0, "zero", sentenceContext)
        withWordContext(1, "testHead", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.After, currentWord) {
            called = true
            assertEquals("testHead", it.value?.name)
            assertEquals("index=1", it.value?.valueName("index"))
        }
        assertTrue(called) {"search context provided concept"}
    }
    @Test
    fun `First matching concept before`() {
        val sentenceContext = withSentenceContext(5)
        withWordContext(0, "testHead", sentenceContext)
        withWordContext(1, "one", sentenceContext)
        withWordContext(2, "testHead", sentenceContext)
        withWordContext(3, "three", sentenceContext)
        val currentWord = withWordContext(4, "two", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.Before, currentWord) {
            called = true
            assertEquals("testHead", it.value?.name)
            assertEquals("index=2", it.value?.valueName("index"))
        }
        assertTrue(called) {"search context provided concept"}
    }

    @Test
    fun `No match before fails`() {
        val sentenceContext = withSentenceContext(3)
        withWordContext(0, "zero", sentenceContext)
        withWordContext(1, "one", sentenceContext)
        val currentWord = withWordContext(2, "two", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.Before, currentWord) {
            called = true
        }
        assertFalse(called) {"no possible match before"}
    }

    @Test
    fun `Never matches on current word`() {
        val sentenceContext = withSentenceContext(1)
        val currentWord = withWordContext(0, "testHead", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.After, currentWord) {
            called = true
        }
        assertFalse(called) {"should not have matched current word"}
    }

    @Test
    fun `Match before concept following predecessor word`() {
        val sentenceContext = withSentenceContext(5)
        withWordContext(0, "zero", sentenceContext)
        withWordContext(1, "testHead", sentenceContext)
        withWordContext(2, "testHead", sentenceContext)
        withWordContext(3, "three", sentenceContext)
        val currentWord = withWordContext(4, "four", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), "word0", SearchDirection.Before, currentWord) {
            println("conceptHolder = $it")
            called = true
            assertEquals("testHead", it.value?.name)
            assertEquals("index=1", it.value?.valueName("index"))
        }
        assertTrue(called) {"search context provided concept"}
    }

    @Test
    fun `Match after concept following predecessor word`() {
        val sentenceContext = withSentenceContext(5)
        val currentWord = withWordContext(0, "zero", sentenceContext)
        withWordContext(1, "testHead", sentenceContext)
        withWordContext(2, "two", sentenceContext)
        withWordContext(3, "testHead", sentenceContext)
        withWordContext(4, "four", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), "word2", SearchDirection.After, currentWord) {
            println("conceptHolder = $it")
            called = true
            assertEquals("testHead", it.value?.name)
            assertEquals("index=3", it.value?.valueName("index"))
        }
        assertTrue(called) {"search context provided concept"}
    }

    @Test
    fun `Can limit distance of match concept after`() {
        val sentenceContext = withSentenceContext(3)
        val currentWord = withWordContext(0, "zero", sentenceContext)
        withWordContext(1, "one", sentenceContext)
        withWordContext(2, "testHead", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.After, currentWord, distance = 1) {
            called = true
        }
        assertFalse(called) {"should not have been able to reach testHead"}
    }

    @Test
    fun `Can limit distance of match concept before`() {
        val sentenceContext = withSentenceContext(3)
        withWordContext(0, "testHead", sentenceContext)
        withWordContext(1, "one", sentenceContext)
        val currentWord = withWordContext(2, "two", sentenceContext)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.Before, currentWord, distance = 1) {
            called = true
        }
        assertFalse(called) {"should not have been able to reach testHead"}
    }

    private fun withSentenceContext(numberOfWords: Int): SentenceContext {
        val sentence = withSentence(numberOfWords)
        return SentenceContext(sentence, WorkingMemory(), EpisodicMemory(),false)
    }

    private fun withSentence(numberOfWords: Int): TextSentence {
        val words = (0 until numberOfWords).map { "word$it" }
        val wordElements = words.map { wordElement(it) }
        val text = words.joinToString(" ")
        return TextSentence(text, wordElements)
    }

    private fun withWordContext(wordIndex: Int, conceptHead: String, sentenceContext: SentenceContext): WordContext {
        val conceptHolder = ConceptHolder(wordIndex, Concept(conceptHead).value("index", Concept("index=$wordIndex")))
        val wordContext = WordContext(wordIndex, "word$wordIndex", conceptHolder, sentenceContext)
        sentenceContext.pushWord(wordContext)
        return wordContext
    }

    private fun wordElement(word: String): WordElement {
        return WordElement(word, "", "word", "")
    }
}