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

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.Gender
import info.dgjones.barnable.parser.Demon
import info.dgjones.barnable.parser.SearchDirection
import info.dgjones.barnable.parser.WordContext
import info.dgjones.barnable.parser.searchContext

/* Populate a slot with a Human of matching gender marked as being of Possessive case */
fun LexicalConceptBuilder.possessiveRef(slotName: Fields, variableName: String? = null, gender: Gender) {
    val variable = root.createVariable(slotName.fieldName, variableName)
    concept.with(variable.slot())
    val demon = PossessiveReference(gender, root.wordContext) {
        if (it != null) {
            root.completeVariable(variable, it, this.episodicConcept)
        }
    }
    root.addDemon(demon)
}

class PossessiveReference(val gender: Gender, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext( matchCase(Case.Possessive), direction = SearchDirection.Before, wordContext = wordContext) {
            val instance = it.value?.value(CoreFields.Instance.fieldName)
            action(instance)
            if (instance != null) {
                active = false
            }
        }
    }

    override fun description(): String {
        return "Poss-Ref for $gender"
    }
}
