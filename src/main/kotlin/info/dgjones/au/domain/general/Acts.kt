package info.dgjones.au.domain.general

import info.dgjones.au.concept.*
import info.dgjones.au.grammar.GrammarFields
import info.dgjones.au.grammar.Preposition
import info.dgjones.au.grammar.Voice
import info.dgjones.au.grammar.matchPrepIn
import info.dgjones.au.narrative.BodyParts
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

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
    MOVE, // Movement of a bodypart of some animate organism
    GRASP, //physical contacting an object
    SPEAK, // any vocalization
    ATTEND // directing a sense organ
}

fun buildATrans(actor: Concept, thing: Concept, from: Concept, to: Concept): Concept {
    return Concept(Acts.ATRANS.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("from", from))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun buildGrasp(actor: Concept, thing: Concept): Concept {
    return Concept(Acts.GRASP.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("instr", buildMove(actor, Concept(BodyParts.Fingers.name), thing)))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun buildMove(actor: Concept, thing: Concept, to: Concept? = null): Concept {
    return Concept(Acts.MOVE.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun buildIngest(actor: Concept, thing: Concept): Concept {
    return Concept(Acts.INGEST.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun buildMTrans(actor: Concept, thing: Concept, from: Concept, to: Concept): Concept {
    return Concept(Acts.MTRANS.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("from", from))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun buildPropel(actor: Concept, thing: Concept): Concept {
    return Concept(Acts.PROPEL.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun buildPTrans(actor: Concept, thing: Concept?, to: Concept?, instr: Concept?): Concept {
    return Concept(Acts.PTRANS.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("to", to))
        .with(Slot("instr", instr))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun buildAttend(actor: Concept, thing: Concept?, to: Concept?): Concept {
    return Concept(Acts.ATTEND.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}

fun LexicalConceptBuilder.expectActor(slotName: Fields = ActFields.Actor, variableName: String? = null) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = ExpectActor(root.wordContext) {
        root.completeVariable(variableSlot, it, this.episodicConcept)
    }
    root.addDemon(demon)
}

fun LexicalConceptBuilder.expectThing(slotName: Fields = ActFields.Thing, variableName: String? = null, matcher: ConceptMatcher = matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name)) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = ExpectThing(matcher, root.wordContext) {
        root.completeVariable(variableSlot, it, this.episodicConcept)
    }
    root.addDemon(demon)
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
        val actorMatcher = matchConceptByHead(InDepthUnderstandingConcepts.Human.name)
        val matcher = matchAny(listOf(
            matchConceptValueName(GrammarFields.Voice, Voice.Passive.name),
            actorMatcher
        ))
        searchContext(matcher, matchNever(), direction = SearchDirection.Before, wordContext = wordContext) {
            if (it.value?.name == InDepthUnderstandingConcepts.Human.name) {
                action(it)
                active = false
            } else if (it.value?.valueName(GrammarFields.Voice) == Voice.Passive.name) {
                searchContext(actorMatcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
                    action(it)
                    active = false
                }
            }
        }
    }
    override fun description(): String {
        return "Search backwards for a Human Actor.\nOn encountering a Voice=Passive then switch to forward search for 'by' Human Actor."
    }
}

class ExpectThing(val thingMatcher: ConceptMatcher = matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name), wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        val byMatcher = matchPrepIn(listOf(Preposition.By.name))
        val matcher = matchAny(listOf(
            // FIXME Really this should be triggered by actor search finding passive voic
            byMatcher,
            thingMatcher
        ))
        searchContext(matcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
            if (thingMatcher(it.value)) {
                action(it)
                active = false
            } else if (byMatcher(it.value)) {
                searchContext(thingMatcher, matchNever(), direction = SearchDirection.Before, wordContext = wordContext) {
                    action(it)
                    active = false
                }
            }
        }
    }
    override fun description(): String {
        return "Search forwards for the object of an Act.\nOn encountering a By preposition then switch to backword search for object."
    }
}