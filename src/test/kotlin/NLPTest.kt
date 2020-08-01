import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NLPTest {
    @Test
    fun `split editorial by sentence`() {
        val s = EditorialNLP(ED_JOBS.content).detectSentences()
        print(s)
        Assertions.assertEquals(7, s.size)
    }

    @Test
    fun `tokenize editorial sentence`() {
        val nlp = EditorialNLP(ED_JOBS.content);
        val s = nlp.detectSentences()[0]
        val tokens = nlp.tokenize(s)
        print(s)
        Assertions.assertEquals(11, tokens.size)
    }

    @Test
    fun `named recognition`() {
        val nlp = EditorialNLP(ED_JOBS.content);
        val s = nlp.detectSentences()[0]
        val spans = nlp.namedEntityRecognition(nlp.text)
        Assertions.assertEquals(1, spans.size)
    }

    @Test
    fun `chunking`() {
        val nlp = EditorialNLP(ED_JOBS.content);
        val s = nlp.detectSentences()[0]
        val chunks = nlp.chunking(s)
        Assertions.assertEquals(11, chunks.size)
    }
}