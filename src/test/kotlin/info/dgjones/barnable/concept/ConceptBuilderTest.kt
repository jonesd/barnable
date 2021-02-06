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

package info.dgjones.barnable.concept

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConceptBuilderTest {
    @Test
    fun `create concept heard`() {
        val actual = concept("root")

        assertEquals(actual, Concept("root"))
    }

    @Test
    fun `create concept with slot`() {
        val actual = concept("root") {
            slot("zero", "Zero")
        }

        val expected = Concept("root")
        expected.value("zero", Concept("Zero"))

        assertEquals(actual, expected)
    }

    @Test
    fun `create concept with nested slot`() {
        val actual = concept("root") {
            slot("zero", "Zero") {
                slot("one", "One")
            }
        }

        val expected = Concept("root")
        val child = Concept("Zero")
        expected.value("zero", child)
        child.value("one", Concept("One"))

        assertEquals(actual, expected)
    }
}