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

import info.dgjones.barnable.concept.ConceptMatcher
import info.dgjones.barnable.concept.matchNever
import kotlin.math.abs

enum class SearchDirection {
    After,
    Before
}

/*
Fundamental search function to navigate the sentence for concepts or words.
If a match is found, then the action is called with the result
*/
fun searchContext(matcher: ConceptMatcher, abortSearch: ConceptMatcher = matchNever(), matchPreviousWord: String? = null, direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, distance: Int? = null, action: (ConceptHolder) -> Unit) {
    var found: ConceptHolder? = null
    var searchAborted = false

    fun isMatchWithSentenceWord(index: Int): Boolean {
        return (matchPreviousWord != null && index >= 0 && wordContext.context.sentenceWordAtWordIndex(index) == matchPreviousWord)
    }

    // Only allow matches within 'distance' param from current word
    fun isOutsideAllowedDistance(index: Int, distance: Int?) =
        distance != null && abs(index - wordContext.wordIndex) > distance

    // Require previous word to 'matchPreviousWord' param
    fun isMissingPreviousWordMatch(index: Int) =
        matchPreviousWord != null && !isMatchWithSentenceWord(index - 1)

    fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing?.value != null) {
            return existing
        }
        if (searchAborted
            || isOutsideAllowedDistance(index, distance)
            || isMissingPreviousWordMatch(index)) {
            return null
        }
        val defHolder = wordContext.context.defHolderAtWordIndex(index)
        val value = defHolder.value
        if (abortSearch.matches(value)) {
            searchAborted = true
            return null
        }
        return if (matcher.matches(value)) defHolder else null
    }

    fun wordIterator(): IntProgression {
        return if (direction == SearchDirection.Before) {
            wordContext.wordIndex - 1 downTo 0
        } else {
            wordContext.wordIndex + 1 until wordContext.context.wordContexts.size
        }
    }

    wordIterator().forEach {
        found = updateFrom(found, wordContext, it)
    }

    val foundConcept = found
    if (foundConcept?.value != null) {
        action(foundConcept)
        println("Search found concept=$foundConcept for match=$matcher")
    }
}