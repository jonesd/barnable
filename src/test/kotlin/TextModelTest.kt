import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextModelTest {
    val source = ED_JOBS.content

    @Test
    fun `Build up sentences from source`() {
        val textModel = TextModelBuilder(source).buildModel()
        assertEquals(7, textModel.sentences.size)
    }
}