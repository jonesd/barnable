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

package info.dgjones.barnable.util

import info.dgjones.barnable.nlp.WordMorphology
import info.dgjones.barnable.parser.Lexicon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WordTransformTest {
    @Nested
    inner class TransformCamelCase {

        @Test
        fun `Split with spaces`() {
            // test
            assertEquals("one", transformCamelCaseToSpaceSeparatedWords("one"))
            assertEquals("One", transformCamelCaseToSpaceSeparatedWords("One"))
            assertEquals("one Two", transformCamelCaseToSpaceSeparatedWords("oneTwo"))
            assertEquals("One Two", transformCamelCaseToSpaceSeparatedWords("OneTwo"))
            assertEquals("One Two Three", transformCamelCaseToSpaceSeparatedWords("OneTwoThree"))
        }
        @Test
        fun `Split with hyphens`() {
            // test
            assertEquals("one", transformCamelCaseToHyphenSeparatedWords("one"))
            assertEquals("One", transformCamelCaseToHyphenSeparatedWords("One"))
            assertEquals("one-Two", transformCamelCaseToHyphenSeparatedWords("oneTwo"))
            assertEquals("One-Two", transformCamelCaseToHyphenSeparatedWords("OneTwo"))
            assertEquals("One-Two-Three", transformCamelCaseToHyphenSeparatedWords("OneTwoThree"))
        }
    }
}