package org.dgjones.au.narrative

import org.dgjones.au.nlp.NaiveTextModelBuilder
import org.dgjones.au.nlp.TextModel
import org.dgjones.au.parser.TextProcessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/* From In-Depth Understanding p19 */
val DIVORCE2 = """
George was having lunch with another teacher and grading homework assignments when the waitress accidentally knocked a glass of coke on him. George was very annoyed and left refusing to pay the check. He decide to drive home to get out of his wet clothes.
    
When he got there, he found his wife Ann and another man in bed. George became extremely upset and felt like going out and getting plastered.
    
At the bar he ran into an old college roomate David, who he hadn't seen in years. David offered to buy him a few drinks and soon they were both pretty drunk. When George found out that David was a layer, he told him all about his troubles and asked David to represent him in court. Since David owed George money he had never returned, he felt obligated to help out.
 
Later, David wrote to Ann, informing her that George wanted a divorce. Her layer called back and told David that she intended to get the house, the children, and a lot of alimony. When George heard this, he was very worried. He didn't earn much at the junior high school. David told him not to worry, since the judge would award the case to George once he learned that Ann has been cheating him.

When they got to court, David presented George's case, but without a witness they had no proof and Ann won. George almost had a fit. David could only offer George his condolences.
""".trimIndent()

/* Attempt to work through Divorce2 based on the "In-Depth Understanding" trace starting p302 */
class Divorce2Test {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `First Sentence`() {
        val textModel = textModelFromFirstSentence()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        // FIXME implement more...

        Assertions.assertEquals(5, textProcessor.workingMemory.concepts.size)
    }

    private fun textModelFromFirstSentence(): TextModel {
        val divorce2model = NaiveTextModelBuilder(DIVORCE2).buildModel()

        val firstSentence = divorce2model.paragraphs[0].sentences[0]
        val textModel = NaiveTextModelBuilder(firstSentence.text).buildModel()
        return textModel
    }
}