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

package info.dgjones.barnable.grammar

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.parser.*

/**
 * "A word, phrase, or clause that limits or qualifies the sense of another word or phrase."
 * https://en.wiktionary.org/wiki/modifier
 *
 * Modifiers are implemented as word elements that search After for a matching word kind
 * and then add a slot with the type of modifier and a value.
 *
 * The value of a modifier can be a specific value (for example the colour "yellow"), or
 * often as an intensity relative to the normal, such as greater or lesser (for example old
 * would map to age greater than normal).
 */

// Word Sense

fun addModifierMappings(field: Fields, value: String, words: List<String>, matcher: ConceptMatcher = defaultModifierTargetMatcher(), lexicon: Lexicon) {
    words.forEach { word ->
        lexicon.addMapping(ModifierWord(word, field, value, matcher))
    }
}

fun defaultModifierTargetMatcher() =
    matchConceptByHead(listOf(GeneralConcepts.Human.name, GeneralConcepts.PhysicalObject.name))

fun stateActMatcher() =
    matchAny(listOf(
        matchConceptByHead(listOf(GeneralConcepts.State.name, GeneralConcepts.Act.name)),
        matchConceptHasSlotName(CoreFields.State)
    ))

/**
 * Add a modifier to a matched concept. Only supports a single value to be associated with the field..
 * For example set Age=GreaterThanNorm to the following Human concept
 */
class ModifierWord(word: String, val field: Fields, val value: String = word, val matcher: ConceptMatcher = defaultModifierTargetMatcher()): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = SingleModifierDemon(field, value, matcher, keepRunning = false, wordContext)
        return listOf(demon)
    }
}

class SingleModifierDemon(val field: Fields, val value: String, val matcher: ConceptMatcher = defaultModifierTargetMatcher(), val keepRunning: Boolean = false, wordContext: WordContext): Demon(wordContext) {
    var thingHolder: ConceptHolder? = null

    override fun run() {
        searchContext(matcher, direction = SearchDirection.After, wordContext = wordContext) {
            it.value?.let { root ->
                // FIXME assumes immediate child slot
                root.with(Slot(field, Concept(value)))
                if (!keepRunning) {
                    active = false
                }
            }
        }
    }

    override fun description(): String {
        return "Add a single ModifierWord ${field.fieldName} = $value to the matching concept"
    }
}

/**
 * Add a modifier to a matched concept. Supports multiple values being associated with a slot.
 * For example add Squally to the list of characterstics for a Weather concept.
 */
class MultipleModifierWord(word: String, val field: Fields, val value: String = word, val matcher: ConceptMatcher = defaultModifierTargetMatcher()): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = MultipleModifierDemon(field, value, matcher, keepRunning = false, wordContext)
        return listOf(demon)
    }
}

class MultipleModifierDemon(val field: Fields, val value: String, val matcher: ConceptMatcher = defaultModifierTargetMatcher(), val keepRunning: Boolean = false, wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        fun createEmptyList(thingConcept: Concept): Concept {
            val c = buildConceptList(listOf<Concept>())
            thingConcept.value(field, c)
            return c
        }

        searchContext(matcher, direction = SearchDirection.After, wordContext = wordContext) {
            it.value?.let { root ->
                // FIXME assumes immediate child slot
                val listConcept = root.value(field) ?: createEmptyList(root)
                ConceptListAccessor(listConcept).add(Concept(value))
                if (!keepRunning) {
                    active = false
                }
            }
        }
    }

    override fun description(): String {
        return "Add a additional ModifierWord ${field.fieldName} = $value to the matching list concept"
    }
}