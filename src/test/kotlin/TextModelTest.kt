import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextModelTest {
    val source = ED_JOBS.content

    @Test
    fun `Build up sentences from source`() {
        val sentences = TextModelBuilder(source).buildModel()
        assertEquals(7, sentences.size)
    }
}