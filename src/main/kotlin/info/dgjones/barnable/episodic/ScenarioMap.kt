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

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.matchConceptByHead
import info.dgjones.barnable.domain.general.GeneralConcepts

/*
Scenario Map records the instantiated scenarios, the participants present in each scenario,
and how participants move between a scenario.
A scenario represents a number of events occurring within the same setting.
A scenario setting can be broken down into scenes, for example a scene outside of a restaurant, and a scene inside of it.

See: InDepth p255/9.1
 */
class ScenarioMap {
    val scenarios = mutableMapOf<EpisodicInstance, EpisodicConcept>()
    val activeScenario: EpisodicConcept? = null
    private var currentEvent: EpisodicConcept? = null

    fun setCurrentEvent(concept: Concept, mainEvent: Boolean = false) {
        val event = concept.value(CoreFields.Event)
        val participants = concept.find(matchConceptByHead(GeneralConcepts.Human.name))
        this.currentEvent = event
    }
}

class Episode {
    val scenes = mutableListOf<Concept>()
    val participants = mutableListOf<Concept>()
    val events = mutableListOf<Concept>()
}