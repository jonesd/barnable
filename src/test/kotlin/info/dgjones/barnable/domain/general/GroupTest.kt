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

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.CoreFields
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GroupTest {
    @Test
    fun `Create Group`() {
        val ball = Concept(GeneralConcepts.PhysicalObject.name)
        ball.value(CoreFields.Name, Concept("ball"))
        ball.value(CoreFields.Kind, Concept(PhysicalObjectKind.GameObject.name))
        val book = Concept(GeneralConcepts.PhysicalObject.name)
        book.value(CoreFields.Name, Concept("book"))
        book.value(CoreFields.Kind, Concept(PhysicalObjectKind.Book.name))

        // test
        val group = buildGroup(listOf(ball, book))

        val groupAccessor = GroupAccessor(group)
        assertEquals(GroupConcept.Group.name, group.name)
        assertEquals(PhysicalObjectKind.PhysicalObject.name, groupAccessor.elementType())
        assertEquals(2, groupAccessor.size)
        assertEquals("ball", groupAccessor[0]?.valueName(CoreFields.Name))
        assertEquals("book", groupAccessor[1]?.valueName(CoreFields.Name))
    }
}
