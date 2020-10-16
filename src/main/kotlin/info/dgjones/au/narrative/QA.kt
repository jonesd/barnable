package info.dgjones.au.narrative

import info.dgjones.au.concept.lexicalConcept
import info.dgjones.au.domain.general.Gender
import info.dgjones.au.domain.general.buildHuman
import info.dgjones.au.parser.*


//class WordWho: WordHandler(EntryWord("who")) {
//    override fun build(wordContext: WordContext): List<Demon> {
//        return listOf(WhoDemon(wordContext))
//    }
//}

//FIXME only tries to handle simple ConceptCompletion involving a character and action
class WordWho: WordHandler(EntryWord("who")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "WhoAnswer") {
            expectHead("actor", headValue = "Human", direction = SearchDirection.After)
            // FIXME brittle hack for first test... perhaps this should be event matching
            expectHead("act", headValue = MopMeal.MopMeal.name, direction = SearchDirection.After)
        }
        return lexicalConcept.demons
    }
}

class WhoDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {

        //FIXME not sure what to use for placeholder
        wordContext.defHolder.value = buildHuman("who", "who", Gender.Male.name)
    }
}

/*
Handler for Concept Completion retrieval heuristic used during Question/Answering phase
 */
/*
class CompletionQuestionDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null) {
            if (def.value(TimeFields.TIME) == null) {
                def.value(TimeFields.TIME.fieldName, Concept(TimeConcepts.Past.name))
                active = false
            }
        }
    }

    override fun description(): String {
        return "Suffix ED marks word sense as in the past"
    }
}*/