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
import info.dgjones.barnable.narrative.MopMeal
import info.dgjones.barnable.narrative.MopMealFields
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ConjunctionTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class BuildGroup {
        @Test
        fun `Builds group with two humans`() {
            val textProcessor = runTextProcess("Fred and George went to the restaurant.", lexicon)

            assertEquals(1, textProcessor.workingMemory.concepts.size)
            val ptrans = textProcessor.workingMemory.concepts.first()
            assertEquals(GroupConcept.Group.name, ptrans.valueName(ActFields.Actor))
            val actors = ptrans.value(ActFields.Actor)?.value(GroupFields.Elements)?.children()
            assertEquals(2, actors?.size)
            assertEquals("Fred", actors?.get(0)?.valueName(HumanFields.FirstName))
            assertEquals("George", actors?.get(1)?.valueName(HumanFields.FirstName))

            assertEquals("Setting", ptrans.valueName("to"));
            assertEquals("restaurant", ptrans.value("to")?.valueName(CoreFields.Name));
//            val group = ptrans.valueName(C)
//            assertEquals(PhysicalObjectKind.PhysicalObject.name, concept.valueName(CoreFields.Kind))
//            assertEquals("book", concept.valueName(CoreFields.Name))
//            assertEquals("red", concept.valueName(ColourFields.Colour))
        }
    }
}