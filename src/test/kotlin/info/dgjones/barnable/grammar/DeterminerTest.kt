/*
 * Copyright  2021 David G Jones
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
import info.dgjones.barnable.domain.general.ColourFields
import info.dgjones.barnable.domain.general.PhysicalObjectKind
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DeterminerTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Determiner modifier for every`() {
        val textProcessor = runTextProcess("every book", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        Assertions.assertEquals(PhysicalObjectKind.PhysicalObject.name, concept.valueName(CoreFields.Kind))
        Assertions.assertEquals("book", concept.valueName(CoreFields.Name))
        Assertions.assertEquals("every", concept.valueName(DeterminerFields.Determiner))
    }
}
