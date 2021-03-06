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

package info.dgjones.barnable.concept

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConceptResolutionTest {
    @Test
    fun `set concept value is resolved`() {
        assertTrue(isConceptResolved(Concept("someValue")))
    }
    @Test
    fun `null concept value is unresolved`() {
        assertFalse(isConceptResolved(null))
    }
    @Test
    fun `variable concept value is unresolved`() {
        assertFalse(isConceptResolved(Concept(Concept.VARIABLE_PREFIX+"1")))
    }
    @Test
    fun `blank concept value is unresolved`() {
        assertFalse(isConceptResolved(Concept("")))
    }

    @Test
    fun `set slot value is resolved`() {
        assertTrue(isConceptResolved(Concept("someValue")))
    }
}