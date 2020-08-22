import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexicalConceptBuilderTest {
    @Test
    fun `create concept with simple concept hierarchy`() {
        val defHolder = ConceptHolder(9)
        var testElement = WordElement("one", "", "","")
        val workingMemory = WorkingMemory()
        val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory)
        val wordContext = WordContext(0, WordElement("test", "", "",""), "test", defHolder, sentenceContext)

        val lexicalConcept = lexicalConcept(wordContext, "MTRANS") {
            slot("actor", "human") {
                slot("name", "mary")
            }
            slot("type", "Act")
        }
        assertEquals("MTRANS", lexicalConcept.head.name)
        assertEquals("mary", lexicalConcept.head.value("actor")?.valueName("name"))
        assertEquals("Act", lexicalConcept.head.valueName("type"))
    }

}