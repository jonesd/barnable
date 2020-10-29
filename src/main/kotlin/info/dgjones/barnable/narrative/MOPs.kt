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

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.episodic.EpisodicConcept
import info.dgjones.barnable.episodic.EpisodicMemory

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
            println("Creating EP ${concept.name} ${mop.valueName(CoreFields.INSTANCE)} ${mop.value(MopMealFields.EATER_A)} ${mop.value(MopMealFields.EATER_B)}")
        }
        else -> null
    }
    return mop
}
