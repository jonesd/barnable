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

class TimeTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Time modification - Yesterday`() {
        val textProcessor = runTextProcess("John walked home yesterday", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val walk = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(Acts.PTRANS.name, walk.name)
        Assertions.assertEquals("John", walk.value(ActFields.Actor)?.valueName(HumanFields.FirstName))
        Assertions.assertEquals("Home", walk.value(ActFields.To)?.valueName(CoreFields.Name))
        Assertions.assertEquals("Yesterday", walk.valueName(TimeFields.TIME))
    }
}