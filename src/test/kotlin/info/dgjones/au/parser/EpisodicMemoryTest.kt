package info.dgjones.au.parser

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.matchConceptByHead
import info.dgjones.au.concept.matchConceptValueName
import info.dgjones.au.episodic.EpisodicMemory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EpisodicMemoryTest {
    @Test
    fun `add concept should respect recency and put most recently added items at front`() {
        val episodicMemory = EpisodicMemory()

        episodicMemory.addConcept(Concept("zero"))
        episodicMemory.addConcept(Concept("one"))
        episodicMemory.addConcept(Concept("two"))

        val content = episodicMemory.concepts()

        assertEquals("two", content[0].name)
        assertEquals("one", content[1].name)
        assertEquals("zero", content[2].name)
    }

    @Test
    fun `find matching concept`() {
        val episodicMemory = EpisodicMemory()
        episodicMemory.addConcept(Concept("zero"))
        episodicMemory.addConcept(Concept("one"))

        val match = episodicMemory.search(matchConceptByHead("zero"))
        assertEquals("zero", match?.name)
    }

    @Test
    fun `find first matching concept`() {
        val episodicMemory = EpisodicMemory()
        episodicMemory.addConcept(Concept("zero"))
        episodicMemory.addConcept(Concept("one").value("child", Concept("childValue")))
        episodicMemory.addConcept(Concept("two").value("child", Concept("childValue")))

        val match = episodicMemory.search(matchConceptValueName("child", "childValue"))
        assertEquals("two", match?.name)
    }

    @Test
    fun `matched concept promoted to front of concepts list`() {
        val episodicMemory = EpisodicMemory()
        episodicMemory.addConcept(Concept("zero"))
        episodicMemory.addConcept(Concept("one").value("child", Concept("childValue")))

        // Initially "one" is at front of concepts as it was most recently added
        assertEquals("one", episodicMemory.concepts[0].name)
        assertEquals(2, episodicMemory.concepts.size)

        // run search matches zero and so moves it to front of the concepts list
        val found = episodicMemory.search(matchConceptByHead("zero"))
        assertEquals("zero", found?.name)

        // "zero" should now be at the front of the list as it was most recently interacted with
        assertEquals("zero", episodicMemory.concepts[0].name)
        assertEquals(2, episodicMemory.concepts.size)
    }
}