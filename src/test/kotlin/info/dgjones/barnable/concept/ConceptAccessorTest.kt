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

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ConceptAccessorTest {
    @Test
    fun `create accessor for root`() {
        val c = Concept("test")
        c.with(Slot("child", Concept("childValue")))

        val accessor = buildConceptPathAccessor(c, "")
        assertSame(c, accessor?.invoke())
    }

    @Test
    fun `create accessor for child`() {
        val c = Concept("test")
        val childValue1 = Concept("childValue1")
        c.with(Slot("child1", childValue1))
        val childValue2 = Concept("childValue2")
        c.with(Slot("child2", childValue2))

        assertSame(childValue1, buildConceptPathAccessor(c, "child1")?.invoke())
        assertSame(childValue2, buildConceptPathAccessor(c, "child2")?.invoke())
    }
}