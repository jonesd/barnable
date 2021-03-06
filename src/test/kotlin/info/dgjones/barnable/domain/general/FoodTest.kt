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

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FoodTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Food Word Sense`() {
        val textProcessor = runTextProcess("Lobster", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val lobster = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(PhysicalObjectKind.PhysicalObject.name, lobster.name)
        Assertions.assertEquals(PhysicalObjectKind.Food.name, lobster.valueName(CoreFields.Kind))
        Assertions.assertEquals("lobster", lobster.valueName(CoreFields.Name))
    }
}