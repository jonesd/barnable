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

package info.dgjones.barnable.grammar

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.domain.general.GeneralConcepts
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConjunctionTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class BuildGroup {
        @Test
        fun `List of two humans`() {
            val textProcessor = runTextProcess("Fred and George", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val group = textProcessor.workingMemory.concepts.first()
            val actors = group.value(GroupFields.Elements)?.children()
            assertEquals(2, actors?.size)
            assertEquals("Fred", actors?.get(0)?.valueName(HumanFields.FirstName))
            assertEquals("George", actors?.get(1)?.valueName(HumanFields.FirstName))
            assertEquals(GeneralConcepts.Human.name, group.valueName(GroupFields.ElementsType))
        }

        @Test
        fun `List of three humans`() {
            val textProcessor = runTextProcess("Jane, Fred and George", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val group = textProcessor.workingMemory.concepts.first()
            val actors = group.value(GroupFields.Elements)?.children()
            assertEquals(3, actors?.size)
            assertEquals("Jane", actors?.get(0)?.valueName(HumanFields.FirstName))
            assertEquals("Fred", actors?.get(1)?.valueName(HumanFields.FirstName))
            assertEquals("George", actors?.get(2)?.valueName(HumanFields.FirstName))
            assertEquals(GeneralConcepts.Human.name, group.valueName(GroupFields.ElementsType))
        }

        @Test
        fun `List of three humans with oxford comma`() {
            val textProcessor = runTextProcess("Jane, Fred, and George", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val group = textProcessor.workingMemory.concepts.first()
            val actors = group.value(GroupFields.Elements)?.children()
            assertEquals(3, actors?.size)
            assertEquals("Jane", actors?.get(0)?.valueName(HumanFields.FirstName))
            assertEquals("Fred", actors?.get(1)?.valueName(HumanFields.FirstName))
            assertEquals("George", actors?.get(2)?.valueName(HumanFields.FirstName))
            assertEquals(GeneralConcepts.Human.name, group.valueName(GroupFields.ElementsType))
        }

        @Test
        fun `List of two physical objects`() {
            val textProcessor = runTextProcess("ball and book", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val group = textProcessor.workingMemory.concepts.first()
            val elements = group.value(GroupFields.Elements)?.children()
            assertEquals(2, elements?.size)
            assertEquals("ball", elements?.get(0)?.valueName(CoreFields.Name))
            assertEquals("book", elements?.get(1)?.valueName(CoreFields.Name))
            assertEquals(GeneralConcepts.PhysicalObject.name, group.valueName(GroupFields.ElementsType))
        }

        @Test
        fun `Action with two human actors`() {
            val textProcessor = runTextProcess("Fred and George went to the restaurant.", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val ptrans = textProcessor.workingMemory.concepts.first()
            assertEquals(GroupConcept.Group.name, ptrans.valueName(ActFields.Actor))
            val actors = ptrans.value(ActFields.Actor)?.value(GroupFields.Elements)?.children()
            assertEquals(2, actors?.size)
            assertEquals("Fred", actors?.get(0)?.valueName(HumanFields.FirstName))
            assertEquals("George", actors?.get(1)?.valueName(HumanFields.FirstName))

            assertEquals(GeneralConcepts.Setting.name, ptrans.valueName(ActFields.To));
            assertEquals("restaurant", ptrans.value(ActFields.To)?.valueName(CoreFields.Name));
        }

        @Test
        fun `Action with two physical objects `() {
            val textProcessor = runTextProcess("Fred picked up the ball and book and dropped them in the box.", lexicon)

            assertEquals(2, textProcessor.workingMemory.concepts.size)
            // picked up the ball and book
            val pickedUp = textProcessor.workingMemory.concepts.first()
            assertEquals("Fred", pickedUp.value(ActFields.Actor)?.valueName(HumanFields.FirstName))
            val pickedUpThings = pickedUp.value(ActFields.Thing)
            assertEquals(GroupConcept.Group.name, pickedUpThings?.name)
            val pickedUpElements = pickedUpThings?.value(GroupFields.Elements)?.children()
            assertEquals(2, pickedUpElements?.size)
            assertEquals("ball", pickedUpElements?.get(0)?.valueName(CoreFields.Name))
            assertEquals("book", pickedUpElements?.get(1)?.valueName(CoreFields.Name))
            // dropped them in the box
            val dropped = textProcessor.workingMemory.concepts[1]
            assertEquals("Fred", dropped.value(ActFields.Actor)?.valueName(HumanFields.FirstName))
            val droppedThings = dropped.value(ActFields.Thing)
            assertEquals(GroupConcept.Group.name, droppedThings?.name)
            val droppedElements = droppedThings?.value(GroupFields.Elements)?.children()
            assertEquals(2, pickedUpElements?.size)
            assertEquals("ball", droppedElements?.get(0)?.valueName(CoreFields.Name))
            assertEquals("book", droppedElements?.get(1)?.valueName(CoreFields.Name))
            assertEquals("box", dropped.value(ActFields.To)?.valueName(CoreFields.Name))
        }

        @Test
        fun `Action with three human actors`() {
            val textProcessor = runTextProcess("Jane, Fred and George went to the restaurant.", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val ptrans = textProcessor.workingMemory.concepts.first()
            assertEquals(GroupConcept.Group.name, ptrans.valueName(ActFields.Actor))
            val actors = ptrans.value(ActFields.Actor)?.value(GroupFields.Elements)?.children()
            assertEquals(3, actors?.size)
            assertEquals("Jane", actors?.get(0)?.valueName(HumanFields.FirstName))
            assertEquals("Fred", actors?.get(1)?.valueName(HumanFields.FirstName))
            assertEquals("George", actors?.get(2)?.valueName(HumanFields.FirstName))

            assertEquals(GeneralConcepts.Setting.name, ptrans.valueName(ActFields.To));
            assertEquals("restaurant", ptrans.value(ActFields.To)?.valueName(CoreFields.Name));
        }
    }
}