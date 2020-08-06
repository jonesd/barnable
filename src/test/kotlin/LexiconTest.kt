import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexiconTest {
    @Test
    fun `can create concept`() {
        val lexicon = Lexicon()
        lexicon.addMapping(WordHandler("test"))
        print(lexicon)
        assertNotNull(lexicon)
    }
}