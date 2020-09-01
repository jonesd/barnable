package org.dgjones.au.parser

import org.dgjones.au.nlp.TextSentence
import org.dgjones.au.nlp.WordElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

// fun searchContext(matcher: ConceptMatcher, abortSearch: ConceptMatcher = matchNever(), matchPreviousWord: String? = null, direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, action: (ConceptHolder) -> Unit) {

class SearchContextTest {
    val matcher = matchConceptByHead("testHead")
    @Test
    fun `Can match concept after`() {
        val sentence = TextSentence("word0 word1", listOf(wordElement("word0"), wordElement("word1")))
        val sentenceContext = SentenceContext(sentence, WorkingMemory(), false)
        val wordContext0 = WordContext(0, sentence.elements[0], "word0", ConceptHolder(0, Concept("zero")), sentenceContext)
        sentenceContext.pushWord(wordContext0)
        val wordContext1 = WordContext(1, sentence.elements[1], "word1", ConceptHolder(1, Concept("testHead")), sentenceContext)
        sentenceContext.pushWord(wordContext1)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.After, wordContext0) {
            println("conceptHolder = $it")
            called = true
            assertEquals("testHead", it.value?.name)
        }
        assertTrue(called) {"search context provided concept"}
    }

    @Test
    fun `Never matches on current word`() {
        val sentence = TextSentence("word0", listOf(wordElement("word0")))
        val sentenceContext = SentenceContext(sentence, WorkingMemory(), false)
        val wordContext0 = WordContext(0, sentence.elements[0], "word0", ConceptHolder(0, Concept("testHead")), sentenceContext)
        sentenceContext.pushWord(wordContext0)
        var called = false
        searchContext(matcher, matchNever(), null, SearchDirection.After, wordContext0) {
            called = true
        }
        assertFalse(called) {"should not have matched current word"}
    }

//    fun createWordContext(varargs words: List<WordContext>)): WordContext {
//        return word(words[0])
//    }

    fun wordElement(word: String): WordElement {
        return WordElement(word, "", "word", "")
    }

    fun word(index: Int, concept: Concept, word: String = "something"): WordContext {
        val wordElement = WordElement(word, "", word, "")
        val conceptHolder =  ConceptHolder(index)
        conceptHolder.value = concept
        val sentenceContext = SentenceContext(TextSentence("some sentence", listOf()), WorkingMemory())
        return WordContext(index, wordElement, word, conceptHolder, sentenceContext)
    }
}
