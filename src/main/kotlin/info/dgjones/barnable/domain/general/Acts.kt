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

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.grammar.GrammarFields
import info.dgjones.barnable.grammar.Preposition
import info.dgjones.barnable.grammar.Voice
import info.dgjones.barnable.grammar.matchPrepIn
import info.dgjones.barnable.narrative.BodyParts
import info.dgjones.barnable.parser.*

enum class ActFields(override val fieldName: String): Fields {
    Actor("actor"),
    Thing("thing"),
    From("from"),
    To("to"),
    Instrument("instr")
}

enum class Acts {
    ATRANS, // Transfer of Possession
    PROPEL, // Application of a physical Force
    PTRANS, // Transfer of physical location
    INGEST, // When an organism takes something from outside its environment and makes it internal
    EXPEL, // Opposite of INGEST
    MTRANS, // Transfer of mental information from one person to another
    MBUILD, // Thought process which create new conceptualizations from old ones
    MOVE, // Movement of a body part of some animate organism
    GRASP, //physical contacting an object
    SPEAK, // any vocalization
    ATTEND // directing a sense organ
}

fun LexicalConceptBuilder.expectActor(slotName: Fields = ActFields.Actor, variableName: String? = null) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = ExpectActor(root.wordContext) {
        root.completeVariable(variableSlot, it, this.episodicConcept)
    }
    root.addDemon(demon)
}

fun LexicalConceptBuilder.expectThing(slotName: Fields = ActFields.Thing, variableName: String? = null, matcher: ConceptMatcher = matchConceptByHeadOrGroup(
    GeneralConcepts.PhysicalObject.name)) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = ExpectThing(matcher, root.wordContext) {
        root.completeVariable(variableSlot, it, this.episodicConcept)
    }
    root.addDemon(demon)
}

fun LexicalConceptBuilder.instrumentByActorToThing(instrumentAct: Acts, instrumentThing: String = BodyParts.Fingers.name) {
    slot(ActFields.Instrument.fieldName, instrumentAct.name) {
        varReference(ActFields.Actor.fieldName, "actor")
        slot(ActFields.Thing.fieldName, instrumentThing)
        varReference(ActFields.To.fieldName, "thing")
        slot(CoreFields.Kind.fieldName, GeneralConcepts.Act.name)
    }
}

// Demons

/** Search for a Human to fill an actor slot of the Act.
 * This starts by looking for an actor referenced earlier in the text, however,
 * if it finds a voice = passive then it will switch to looking for a "by" Human
 * after the current word.
 *
 * Default Active - "Fred kicked the ball."
 * Passive - "The ball was kicked by Fred."
 */
class ExpectActor(wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        val matchingValues = GeneralConcepts.Human.name
        val actorMatcher = matchConceptByHeadOrGroup(matchingValues)
        val matcher = matchAny(listOf(
            matchConceptValueName(GrammarFields.Voice, Voice.Passive.name),
            actorMatcher
        ))
        searchContext(matcher, matchNever(), direction = SearchDirection.Before, wordContext = wordContext) {
            if (actorMatcher(it.value)) {
                action(it)
                active = false
            } else if (it.value?.valueName(GrammarFields.Voice) == Voice.Passive.name) {
                searchContext(actorMatcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) { actor ->
                    action(actor)
                    active = false
                }
            }
        }
    }
    override fun description(): String {
        return "Search backwards for a Human Actor.\nOn encountering a Voice=Passive then switch to forward search for 'by' Human Actor."
    }
}

class ExpectThing(val thingMatcher: ConceptMatcher = matchConceptByHead(GeneralConcepts.PhysicalObject.name), wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        val byMatcher = matchPrepIn(listOf(Preposition.By))
        val matcher = matchAny(listOf(
            // FIXME Really this should be triggered by actor search finding passive voice
            byMatcher,
            thingMatcher
        ))
        searchContext(matcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
            if (thingMatcher(it.value)) {
                action(it)
                active = false
            } else if (byMatcher(it.value)) {
                searchContext(thingMatcher, matchNever(), direction = SearchDirection.Before, wordContext = wordContext) { thing ->
                    action(thing)
                    active = false
                }
            }
        }
    }
    override fun description(): String {
        return "Search forwards for the object of an Act.\nOn encountering a By preposition then switch to backwards search for object."
    }
}