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

package info.dgjones.barnable.narrative

import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.qa.QuestionProcessor
import info.dgjones.barnable.parser.buildTextModel
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QATest {
    val lexicon = buildInDepthUnderstandingLexicon()

    /* FIXME rewrite test for newer QA
    @Test
    fun `Question who gave mary the book - shared working memory`() {
        val textModel = NaiveTextModelBuilder("John gave Mary a book").buildModel()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        // Question 1
        val questionModel = NaiveTextModelBuilder("Who gave Mary a book").buildModel()
        val response = textProcessor.processQuestion(questionModel.paragraphs.first().sentences.first())

        Assertions.assertEquals("John", response)

        // Question 2
        val questionModel2 = NaiveTextModelBuilder("John gave who the book").buildModel()
        val response2 = textProcessor.processQuestion(questionModel2.paragraphs.first().sentences.first())

        Assertions.assertEquals("Mary", response2)
    }
    */

    @Nested
    inner class ConceptCompletionTest {

        @Test
        fun `Question - Who did John eat lunch with?`() {
            val textProcessor = runTextProcess("John had lunch with George .", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val meal = textProcessor.workingMemory.concepts[0]
            assertEquals("MopMeal", meal.name)
            assertEquals("John", meal.value("eaterA")?.valueName(HumanFields.FirstName))
            assertEquals("George", meal.value("eaterB")?.valueName(HumanFields.FirstName))
            assertEquals("EventEatMeal", meal.valueName("event"))

            // Question

            val qa = QuestionProcessor(textProcessor)
            val result = qa.question(buildTextModel("Who did John have lunch with?"))

            assertEquals(1, result.sentenceResult.size)
            val answerConcept = result.sentenceResult[0]
            assertEquals("John", answerConcept.value("actor")?.valueName(HumanFields.FirstName))
            assertEquals("MopMeal", answerConcept.valueName("act"))

            assertEquals("George", result.answer)
        }

        @Test
        fun `Question - Who ate lunch?`() {
            val textProcessor = runTextProcess("John had lunch with George .", lexicon)

            // Question

            val qa = QuestionProcessor(textProcessor)
            val result = qa.question(buildTextModel("Who ate lunch?"))

            assertEquals("John and George", result.answer)
        }
    }

}