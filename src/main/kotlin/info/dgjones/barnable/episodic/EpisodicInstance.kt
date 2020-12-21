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

package info.dgjones.barnable.episodic

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.parser.Demon
import info.dgjones.barnable.parser.WordContext
import info.dgjones.barnable.parser.conceptPathResolvedValue


fun LexicalConceptBuilder.innerInstance(slotName: Fields, variableName: String? = null, observeSlot: String) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = InnerInstanceDemon(observeSlot, root.wordContext) {
        if (it != null) {
            root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
        }
    }
    root.addDemon(demon)
}

/* Assign the Instance slot value of the resolved concept */
class InnerInstanceDemon(val slotName: String, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    var conceptAccessor: ConceptSlotAccessor? = null
    override fun run() {
        wordContext.defHolder.value?.let { rootConcept ->
            conceptPathResolvedValue(rootConcept, slotName)?.let { parentConcept ->
                val instanceConcept = parentConcept.value(CoreFields.Instance)
                if (isConceptResolved(instanceConcept)) {
                    active = false
                    action(instanceConcept)
                }
            }
        }
    }

    override fun description(): String {
        return "When the inner ${CoreFields.Instance} of $slotName has been bound\nThen use it to bind the demon's role"
    }
}
