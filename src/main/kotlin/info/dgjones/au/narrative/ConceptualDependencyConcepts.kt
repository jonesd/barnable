package info.dgjones.au.narrative

import info.dgjones.au.parser.Concept
import info.dgjones.au.parser.Slot

enum class Acts {
    ATRANS, // Transfer of Posession
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