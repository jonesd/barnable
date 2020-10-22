package info.dgjones.au.grammar

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.Slot
import info.dgjones.au.concept.matchConceptByHead
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

/**
 *
 */

fun buildGrammarModifierLexicon(lexicon: Lexicon) {

}

class ModifierWord(word: String, val modifier: String, val value: String = word): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var thingHolder: ConceptHolder? = null

            override fun run() {
                val thingConcept = thingHolder?.value
                if (thingConcept != null) {
                    thingConcept.with(Slot(modifier, Concept(value)))
                    //FIXME remove thingConcept.addModifier(modifier, value)
                    active = false
                }
            }

            override fun description(): String {
                return "ModifierWord $value"
            }
        }
        //FIXME list of kinds is not complete
        val thingDemon = ExpectDemon(matchConceptByHead(listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)), SearchDirection.After, wordContext) {
            demon.thingHolder = it
        }
        return listOf(demon, thingDemon)
    }
}