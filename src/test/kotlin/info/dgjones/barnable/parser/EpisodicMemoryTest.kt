/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.parser

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.matchConceptByHead
import info.dgjones.barnable.concept.matchConceptValueName
import info.dgjones.barnable.episodic.EpisodicMemory
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