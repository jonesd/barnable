import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexiconTest {
    @Test
    fun `can create concept`() {
        val lexicon = buildLexicon()
        print(lexicon)
        assertNotNull(lexicon)
    }
}