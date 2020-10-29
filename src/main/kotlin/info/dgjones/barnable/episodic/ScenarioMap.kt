package info.dgjones.barnable.episodic

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.matchConceptByHead
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts

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
    var currentEvent: EpisodicConcept? = null

    fun setCurrentEvent(concept: Concept, mainEvent: Boolean = false) {
        val event = concept.value(CoreFields.Event)
        val participants = concept.find(matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
        this.currentEvent = event
    }
}

class Episode {
    val scenes = mutableListOf<Concept>()
    val participants = mutableListOf<Concept>()
    val events = mutableListOf<Concept>()
}