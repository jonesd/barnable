import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InDepthUnderstandingTest {

    val johnPickedUpTheBallAndPutItInTheBox = "John picked up the ball and put it in the box"

    @Test
    fun `Execution Test`() {
        val textModel = TextModelBuilder(johnPickedUpTheBallAndPutItInTheBox).buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        assertEquals(-1, textProcessor.workingMemory.concepts.size)
        // fixem val textModel = TextModelBuilder(source).buildModel()
        // fixme TextProcessor(textModel).runProcessor()
    }
}