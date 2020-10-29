package info.dgjones.barnable.grammar

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.parser.*

/**
 * A word, phrase, or clause that limits or qualifies the sense of another word or phrase.
 * https://en.wiktionary.org/wiki/modifier
 */

enum class ModifierConcepts {
    GreaterThanNormal,
    Normal,
    LessThanNormal
}

enum class Modifiers(val modifier: Fields, val value: ModifierConcepts, val words: List<String>) {
    GreaterWeight(CoreFields.Weight, ModifierConcepts.GreaterThanNormal,
        listOf("fat", "heavy", "obese", "overweight")),
    LesserWeight(CoreFields.Weight, ModifierConcepts.LessThanNormal,
        listOf("thin", "underweight")),

    Old(CoreFields.Age, ModifierConcepts.GreaterThanNormal,
        listOf("old")),
    Young(CoreFields.Age, ModifierConcepts.LessThanNormal,
        listOf("young"))
}

// Word Sense

fun buildGrammarModifierLexicon(lexicon: Lexicon) {
    Modifiers.values().forEach { modifier ->
        modifier.words.forEach { word ->
            lexicon.addMapping(ModifierWord(word, modifier.modifier, modifier.value.name))
        } }
}

class ModifierWord(word: String, val field: Fields, val value: String = word): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var thingHolder: ConceptHolder? = null

            override fun run() {
                val thingConcept = thingHolder?.value
                if (thingConcept != null) {
                    thingConcept.with(Slot(field, Concept(value)))
                    active = false
                }
            }

            override fun description(): String {
                return "ModifierWord ${word}"
            }
        }
        //FIXME list of kinds is not complete
        val thingDemon = ExpectDemon(matchConceptByHead(listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)), SearchDirection.After, wordContext) {
            demon.thingHolder = it
        }
        return listOf(demon, thingDemon)
    }
}