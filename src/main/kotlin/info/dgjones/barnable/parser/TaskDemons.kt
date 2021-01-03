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

package info.dgjones.barnable.parser

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.episodic.EpisodicMemory
import info.dgjones.barnable.grammar.*
import info.dgjones.barnable.narrative.*

class ExpectDemon(val matcher: ConceptMatcher, val direction: SearchDirection, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, abortSearch = matchConjunction(), direction = direction, wordContext = wordContext) {
            action(it)
            active = false
        }
    }

    override fun description(): String {
        return "ExpectDemon $direction $action"
    }
}

/**
 * Find the earliest match in the sentence from a contiguous match from the current wordContext.
 * Ignore skipMatcher elements.
 */
class ExpectEarliestDemon(val matcher: ConceptMatcher, val skipMatcher: ConceptMatcher = matchNever(), val abortSearch: ConceptMatcher = matchConjunction(), wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        val anyMatcher = matchAny(listOf(matcher, skipMatcher))
        var currentMatch = wordContext.defHolder
        do {
            var newMatch: ConceptHolder? = null
            val startWordContext = wordContext.context.wordContexts.first { it.defHolder == currentMatch }
            searchContext(
                anyMatcher,
                abortSearch = abortSearch,
                direction = SearchDirection.Before,
                wordContext = startWordContext
            ) {
                if (it.value != null) {
                    newMatch = it
                }
            }
            if (newMatch != null) {
                currentMatch = newMatch as ConceptHolder
            }
        } while (newMatch != null)
        action(currentMatch)
        active = false
    }

    override fun description(): String {
        return "ExpectEarliestDemon $action"
    }
}

class InsertAfterDemon(val matcher: ConceptMatcher, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
            if (it.value != null) {
                active = false
                action(it)
            }
        }
    }

    override fun description(): String {
        return "InsertAfter $matcher"
    }
}

class CopySlotValueToConceptDemon(val sourceField: Fields, val matcher: ConceptMatcher = defaultModifierTargetMatcher(), val updateField: Fields, val keepRunning: Boolean = false, wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val sourceValue = wordContext.defHolder.value?.valueName(sourceField)
        searchContext(matcher, direction = SearchDirection.After, wordContext = wordContext) { updateHolder ->
            updateHolder.value?.let { root ->
                root.slotOrCreate(updateField.fieldName).let { slot ->
                    if (sourceValue != slot.value?.name) {
                        // FIXME should we alias concept or create new?
                        slot.value =  if (sourceValue != null) Concept(sourceValue) else null
                        wordContext.defHolder.addFlag(ParserFlags.Inside)
                    }
                }
                if (!keepRunning) {
                    active = false
                }
            }
        }
    }

    override fun description(): String {
        return "copy slot value ${sourceField.fieldName} to ${updateField.fieldName} on found concept"
    }
}

class UpdateEventDemon(private val episodicConcept: Concept, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        //FIXME should this also handle "having a meeting"
        if (wordContext.previousWord()?.equals("have", ignoreCase = true) == true) {
            episodicConcept.value(CoreFields.Event)?.let { event ->
                wordContext.context.episodicMemory.setCurrentEvent(event, mainEvent = true)
                // FIXME spawn extra demons?
            }
        }
    }

    override fun description(): String {
        return "UpdateEvent if HAVE precedes: set as main event, update scenario map"
    }
}

fun isDependentSlotsComplete(parent: Concept, childrenSlotNames: List<String>): Boolean {
    // FIXME PERFORMANCE try and cache the conceptAccessors
    return childrenSlotNames.all { conceptPathResolvedValue(parent, it) != null }
}

fun conceptPathResolvedValue(parent: Concept?, slotName: String): Concept? {
    parent?.let {
        buildConceptPathAccessor(parent, slotName)?.invoke()?.let { concept ->
            if (isConceptResolved(concept)) {
                return concept
            }
        }
    }
    return null
}
