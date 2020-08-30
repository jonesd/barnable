package org.dgjones.au.nlp

import org.dgjones.au.editorial.ED_JOBS
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextModelTest {
    private val source = ED_JOBS.content

    @Test
    fun `Build up sentences from source`() {
        val textModel = NaiveTextModelBuilder(source).buildModel()
        assertEquals(7, textModel.paragraphs.first().sentences.size)
    }
}