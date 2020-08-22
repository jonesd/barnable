import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptBuilderTest {
    @Test
    fun `create concept`() {
        val concept = concept {

        }.build()

        assertEquals(Concept("TODO"), concept)
    }
/*
    fun `create concept extended`() {
        val concept = concept("MTRANS") {


        }.build()

        assertEquals(Concept("TODO"), concept)
    }*/

}