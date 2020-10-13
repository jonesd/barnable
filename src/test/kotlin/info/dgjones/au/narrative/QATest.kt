package info.dgjones.au.narrative

import info.dgjones.au.domain.general.Human
import info.dgjones.au.qa.QuestionProcessor
import info.dgjones.au.parser.buildTextModel
import info.dgjones.au.parser.runTextProcess
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
            assertEquals("John", meal.value("eaterA")?.valueName("firstName"))
            assertEquals("George", meal.value("eaterB")?.valueName("firstName"))
            assertEquals("EventEatMeal", meal.valueName("event"))

            // Question

            var qa = QuestionProcessor(textProcessor)
            val result = qa.question(buildTextModel("Who did John have lunch with?"))

            assertEquals(1, result.sentenceResult.size)
            val answerConcept = result.sentenceResult[0]
            assertEquals("John", answerConcept.value("actor")?.valueName(Human.FIRST_NAME))
            assertEquals("MopMeal", answerConcept.valueName("act"))

            assertEquals("George", result.answer)
        }

        @Test
        fun `Question - Who ate lunch?`() {
            val textProcessor = runTextProcess("John had lunch with George .", lexicon)

            // Question

            var qa = QuestionProcessor(textProcessor)
            val result = qa.question(buildTextModel("Who ate lunch?"))

            assertEquals("John and George", result.answer)
        }
    }

}