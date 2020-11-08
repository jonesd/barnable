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
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.parser.*

/*
"A word, normally non-inflecting, that is typically employed to connect a following noun or pronoun, in an adjectival or
adverbial sense, with some other word. Examples of prepositions in English are in, from and during."
https://en.wiktionary.org/wiki/Appendix:Glossary#preposition
 */
enum class PrepositionConcept {
    Preposition
}

enum class Preposition {
    By,
    In,
    Into,
    On,
    To,
    With
}

fun buildPrep(preposition: Preposition): Concept {
    val concept = Concept(PrepositionConcept.Preposition.name)
    withPreposition(concept, preposition)
    return concept
}

fun withPreposition(concept: Concept, preposition: Preposition) {
    concept.with(Slot(CoreFields.Is, Concept(preposition.name)))
}

fun matchPrepIn(preps: Collection<Preposition>): ConceptMatcher {
    val prepNames = preps.map { it.name }
    return { c -> prepNames.contains(c?.valueName(CoreFields.Is)) }
}

fun LexicalConceptBuilder.expectPrep(slotName: String, variableName: String? = null, preps: Collection<Preposition>, matcher: ConceptMatcher, direction: SearchDirection = SearchDirection.After) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val matchers = matchAll(
        listOf(matchPrepIn(preps), matcher)
    )
    val demon = PrepDemon(matchers, direction, root.wordContext) {
        root.completeVariable(variableSlot, it, this.episodicConcept)
    }
    root.addDemon(demon)
}

// Word Senses

fun buildGrammarPropositionLexicon(lexicon: Lexicon) {
    // FIXME InDepth p304 "with" also needs to "determine social activity"
    lexicon.addMapping(PrepositionWord(Preposition.With, setOf(InDepthUnderstandingConcepts.Human.name)))
    lexicon.addMapping(PrepositionWord(Preposition.In, setOf(InDepthUnderstandingConcepts.PhysicalObject.name, InDepthUnderstandingConcepts.Setting.name)))
    lexicon.addMapping(PrepositionWord(Preposition.By, setOf(InDepthUnderstandingConcepts.Setting.name)))
}

/*
A preposition word will mark a matched target word as being connected to the preposition. This can then be matched
This target can then be matched by another word searching for a preposition.
For example: John had lunch with George
 - With word will mark George as being related to the With preposition
 - Lunch looks for a following Human related to a With preposition
 */
class PrepositionWord(private val preposition: Preposition, private val matchConcepts: Set<String>): WordHandler(EntryWord(preposition.name.toLowerCase())) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildPrep(preposition)
        wordContext.defHolder.addFlag(ParserFlags.Ignore)

        val matcher = matchConceptByHead(matchConcepts)
        val addPrepObj = InsertAfterDemon(matcher, wordContext) {
            if (wordContext.isDefSet()) {
                val itValue = it.value
                val holderValue = wordContext.defHolder.value
                if (itValue != null && holderValue != null) {
                    withPreposition(itValue, preposition)
                    wordContext.defHolder.addFlag(ParserFlags.Inside)
                    println("Updated with preposition=$preposition concept=${it}")
                }
            }
        }
        return listOf(addPrepObj)
    }
}

// Demons

class PrepDemon(val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        // FIXME may need to stop on clause boundary
        searchContext(matcher, matchNever(), direction = direction, wordContext = wordContext) {
            action(it)
            if (it != null) {
                active = false
            }
        }
    }

    override fun description(): String {
        return "PrepDemon $matcher"
    }
}