/*
 * Copyright  2021 David G Jones
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

package info.dgjones.barnable.parser


import info.dgjones.barnable.nlp.WordMorphology
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DisambiguationHandlerTest {


    @Test
    fun `Resolves handler when only one exists`() {
        val wordContext = withWordContext()
        val wordHandler = mockk<WordHandler>(relaxed = true)

        val agenda = mockk<Agenda>()
        val disambiguationHandler = DisambiguationHandler(wordContext, listOf(LexicalItem(listOf(WordMorphology("test")), wordHandler)), agenda)

        disambiguationHandler.startDisambiguations()

        verify { wordHandler.build(wordContext)}
    }

    @Test
    fun `Resolves to handler when all its disambiguation demons succeeds`() {
        val wordContext = withWordContext()
        val wordHandler = mockk<WordHandler>(relaxed = true)
        val disambiguationDemon = mockk<Demon>(relaxed = true)

        val agenda = mockk<Agenda>()
        every { agenda.withDemon(any(), disambiguationDemon) } returns Unit

        val disambiguationHandler = DisambiguationHandler(wordContext, listOf(LexicalItem(listOf(WordMorphology("test")), wordHandler)), agenda)
        every { wordHandler.disambiguationDemons(wordContext, disambiguationHandler) } returns listOf(disambiguationDemon)

        // Test
        disambiguationHandler.startDisambiguations()
        disambiguationHandler.disambiguationMatchCompleted(disambiguationDemon)

        verify { wordHandler.build(wordContext)}
    }

    @Test
    fun `Resolves to first handler whose disambiguation demons succeeds`() {
        val wordContext = withWordContext()

        val wordHandler0 = mockk<WordHandler>(relaxed = true)
        val disambiguationDemon0 = mockk<Demon>(relaxed = true)

        val wordHandler1 = mockk<WordHandler>(relaxed = true)
        val disambiguationDemon1 = mockk<Demon>(relaxed = true)

        val agenda = mockk<Agenda>()
        every { agenda.withDemon(any(), any()) } returns Unit

        val disambiguationHandler = DisambiguationHandler(wordContext, listOf(
            LexicalItem(listOf(WordMorphology("test0")), wordHandler0),
            LexicalItem(listOf(WordMorphology("test1")), wordHandler1)
        ), agenda)
        every { wordHandler0.disambiguationDemons(wordContext, disambiguationHandler) } returns listOf(disambiguationDemon0)
        every { wordHandler1.disambiguationDemons(wordContext, disambiguationHandler) } returns listOf(disambiguationDemon1)

        // Test
        disambiguationHandler.startDisambiguations()
        disambiguationHandler.disambiguationMatchCompleted(disambiguationDemon1)

        verify(exactly = 0) { wordHandler0.build(wordContext)}
        verify { wordHandler1.build(wordContext)}
    }


}