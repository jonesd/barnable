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

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.domain.general.GroupConcept
import info.dgjones.barnable.domain.general.GroupFields
import info.dgjones.barnable.parser.Demon
import info.dgjones.barnable.parser.WordContext

/**
 * Mark word as being Plural
 */
class PluralDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null && def.value(GroupFields.GroupInstances) == null) {
            def.value(GroupFields.GroupInstances, Concept(GroupConcept.MultipleGroup.name))
            active = false
        }
    }
    override fun description(): String {
        return "Suffix S marks word sense as being multiple"
    }
}
