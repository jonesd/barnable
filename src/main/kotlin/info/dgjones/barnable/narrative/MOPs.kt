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

package info.dgjones.barnable.narrative

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.episodic.EpisodicConcept
import info.dgjones.barnable.episodic.EpisodicMemory
import info.dgjones.barnable.parser.Demon
import info.dgjones.barnable.parser.WordContext

enum class MopFields(override val fieldName: String): Fields {
    MOP("mop")
}

enum class MopService {
    MopService
}

enum class MopRestaurant {
    MopRestaurant
}

enum class MopMeal {
    MopMeal,
    EventEatMeal
}

enum class MopMealFields(override val fieldName: String): Fields {
    EATER_A("eaterA"),
    EATER_B("eaterB"),
}

// Episodic Implementation

fun EpisodicMemory.checkOrCreateMop(concept: Concept): EpisodicConcept {
    findExistingEpisodicMop(concept)?.let { existingMop -> return existingMop }
    val mop = createBareEpisodicConceptFrom(concept, mops)
    // FIXME use data to (ie enums/etc) to get away from from this map
    when (concept.name) {
        MopMeal.MopMeal.name -> {
            initializeCharacterSlotFrom(concept, MopMealFields.EATER_A, mop)
            initializeCharacterSlotFrom(concept, MopMealFields.EATER_B, mop)
            initializeEventSlotFrom(concept, CoreFields.Event, mop)
            println("Creating EP ${concept.name} ${mop.valueName(CoreFields.Instance)} ${mop.value(MopMealFields.EATER_A)} ${mop.value(MopMealFields.EATER_B)}")
        }
        else -> null
    }
    return mop
}

// Lexical

fun LexicalConceptBuilder.checkMop(slotName: String, variableName: String? = null) {
    val variable = root.createVariable(slotName, variableName)
    concept.with(variable.slot())
    val checkMopDemon = CheckMopDemon(concept, root.wordContext) { episodicMop ->
        if (episodicMop != null) {
            val episodicInstance = episodicMop.value(CoreFields.Instance)
            root.completeVariable(variable, root.wordContext.context.workingMemory.createDefHolder(episodicInstance))
            this.episodicConcept = episodicMop
            copyCompletedSlot(MopMealFields.EATER_A, episodicMop, concept)
            copyCompletedSlot(MopMealFields.EATER_B, episodicMop, concept)
            copyCompletedSlot(CoreFields.Event, episodicMop, concept)
//            } else {
//                println("Creating mop ${concept.name} in EP memory")
//                val human = buildHuman(concept.valueName("firstName"), concept.valueName("lastName"), concept.valueName("gender"))
//                // val saveCharacterDemon = SaveCharacterDemon(root.wordContext)
//                root.wordContext.context.episodicMemory.addConcept(human)
        }
    }
    root.addDemon(checkMopDemon)
    // checkOrCreateMop
}

class CheckMopDemon(val mop: Concept, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val matchedMop = wordContext.context.episodicMemory.checkOrCreateMop(mop)
        action(matchedMop)

        active = false
    }
    override fun description(): String {
        return "CheckMOP from episodic with ${mop.name}"
    }
}

class EpisodicRoleCheck(val mop: Concept, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val matchedMop = wordContext.context.episodicMemory.checkOrCreateMop(mop)
        action(matchedMop)
        active = false
    }
    override fun description(): String {
        return "CheckMOP from episodic with ${mop.name}"
    }
}

// InDepth p185
fun checkEpisodicRelationship(parent: Concept, episodicMemory: EpisodicMemory): String {
    return episodicMemory.checkOrCreateRelationship(parent)
    // FIXME also assume new relationship
}
