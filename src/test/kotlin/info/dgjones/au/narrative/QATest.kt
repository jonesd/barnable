package info.dgjones.au.narrative

import info.dgjones.au.nlp.NaiveTextModelBuilder
import info.dgjones.au.parser.TextProcessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class QATest {
    val lexicon = buildInDepthUnderstandingLexicon()

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
}