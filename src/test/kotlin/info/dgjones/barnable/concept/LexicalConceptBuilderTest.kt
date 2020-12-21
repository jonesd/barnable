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

import info.dgjones.barnable.episodic.EpisodicMemory
import info.dgjones.barnable.nlp.TextSentence
import info.dgjones.barnable.nlp.WordElement
import info.dgjones.barnable.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexicalConceptBuilderTest {
    @Test
    fun `create concept with simple concept hierarchy`() {
        val defHolder = ConceptHolder(9)
        val testElement = WordElement("one", "", "","")
        val workingMemory = WorkingMemory()
        val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory, EpisodicMemory())
        val wordContext = WordContext(0, "test", defHolder, sentenceContext)

        // test
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
        val testElement = WordElement("one", "", "","")
        val workingMemory = WorkingMemory()
        val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory, EpisodicMemory())
        val wordContext = WordContext(0, "test", defHolder, sentenceContext)

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
        val testElement = WordElement("one", "", "","")
        val workingMemory = WorkingMemory()
        val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory, EpisodicMemory())
        val wordContext = WordContext(0,"test", defHolder, sentenceContext)

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