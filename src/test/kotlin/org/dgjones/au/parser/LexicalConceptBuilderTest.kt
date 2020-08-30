package org.dgjones.au.parser

import org.dgjones.au.nlp.TextSentence
import org.dgjones.au.nlp.WordElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexicalConceptBuilderTest {
    @Test
    fun `create concept with simple concept hierarchy`() {
        val defHolder = ConceptHolder(9)
        var testElement = WordElement("one", "", "","")
        val workingMemory = WorkingMemory()
        val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory)
        val wordContext = WordContext(0, WordElement("test", "", "",""), "test", defHolder, sentenceContext)

        val lexicalConcept = lexicalConcept(wordContext, "MTRANS") {
            slot("actor", "human") {
                slot("name", "mary")
            }
            slot("type", "Act")
        }
        assertEquals("MTRANS", lexicalConcept.head.name)
        assertEquals("mary", lexicalConcept.head.value("actor")?.valueName("name"))
        assertEquals("Act", lexicalConcept.head.valueName("type"))
    }

    @Test
    fun `create concept with expectDemon`() {
        val defHolder = ConceptHolder(9)
        var testElement = WordElement("one", "", "","")
        val workingMemory = WorkingMemory()
        val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory)
        val wordContext = WordContext(0, WordElement("test", "", "",""), "test", defHolder, sentenceContext)

        val lexicalConcept = lexicalConcept(wordContext, "MTRANS") {
            expectHead("actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("to", headValue = "Human")
            slot("kind", "Act")
        }
        assertEquals("MTRANS", lexicalConcept.head.name)
        assertEquals("*VAR.0*", lexicalConcept.head.valueName("actor"))
        assertEquals("*VAR.1*", lexicalConcept.head.valueName("to"))
    }

    @Test
    fun `create concept with shared variables`() {
        val defHolder = ConceptHolder(9)
        var testElement = WordElement("one", "", "","")
        val workingMemory = WorkingMemory()
        val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory)
        val wordContext = WordContext(0, WordElement("test", "", "",""), "test", defHolder, sentenceContext)

        val lexicalConcept = lexicalConcept(wordContext, "MTRANS") {
            expectHead("actor", "ACTOR", headValue = "Human", direction = SearchDirection.Before)
            varReference("from", "ACTOR")
            expectHead("to", headValue = "Human")
            slot("kind", "Act")
        }

        assertEquals("MTRANS", lexicalConcept.head.name)
        assertEquals("*VAR.ACTOR*", lexicalConcept.head.valueName("actor"))
        assertEquals("*VAR.ACTOR*", lexicalConcept.head.valueName("from"))
        assertEquals("*VAR.0*", lexicalConcept.head.valueName("to"))
    }
}