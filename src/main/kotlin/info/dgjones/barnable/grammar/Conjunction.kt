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
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.parser.*


enum class Conjunction {
    And
}

fun withConjunctionObj(concept: Concept, conjunction: Concept) {
    concept.with(Slot("conjObj", conjunction))
}

fun buildConjunction(conjunction: String): Concept {
    return Concept("conjunction")
        .with(Slot(CoreFields.Is, Concept(conjunction)))
}

fun matchConjunction(): ConceptMatcher {
    return matchConceptByHead(ParserKinds.Conjunction.name)
}

// Word Senses

fun buildGrammarConjunctionLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordAndBuildGroup())
    lexicon.addMapping(WordAndAddToGroup())
    lexicon.addMapping(WordCommaBuildGroup())
    lexicon.addMapping(WordCommaBoundary())
}

class WordAnd: WordHandler(EntryWord("and")) {
    override fun build(wordContext: WordContext): List<Demon> {
        // Only handles grouper
        wordContext.defHolder.value = Concept(GroupConcept.Group.name)
        // wordContext.defHolder.value = buildConjunction(Conjunction.And.name)
        return listOf(IgnoreDemon(wordContext))
    }
}

/*
Handle the scenario of "george and harold" forming a group of two persons
 */
class WordAndBuildGroup: WordHandler(EntryWord("and")) {
    private val matchingHeads = listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GroupConcept.Group.name) {
            slot(GroupFields.Elements, "values") {
                expectHead("0", "exemplar", matchingHeads, clearHolderOnCompletion = false, markAsInside = false, direction = SearchDirection.Before)
                expectHead("1", null, matchingHeads, clearHolderOnCompletion = true, direction = SearchDirection.After)
            }
            varReference(GroupFields.ElementsType.fieldName, "exemplar", extractConceptHead)
            replaceWordContextWithCurrent("exemplar")
            // only want to save as resolution... saveAsObject()
            ignoreHolder()
        }.demons

    override fun disambiguationDemons(wordContext: WordContext,disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.Before,
                1,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                1,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

/*
Handle the scenario of "fred, george and mary" where the "and" will add mary to a Group formed from "fred, george"
 */
class WordAndAddToGroup: WordHandler(EntryWord("and")) {
    private val matchingHeads = listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, "Ignore") {
            addToGroup(matchConceptByHead(matchingHeads), SearchDirection.After)
            ignoreHolder()
         }.demons

    override fun disambiguationDemons(wordContext: WordContext,disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(
                matchConceptByHead(GroupConcept.Group.name),
                SearchDirection.Before,
                distance = null,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                1,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

class WordCommaBuildGroup: WordHandler(EntryWord(",", noSuffix = true)) {
    private val matchingHeads = listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GroupConcept.Group.name) {
            slot(GroupFields.Elements, "values") {
                expectHead("0", "exemplar", matchingHeads, clearHolderOnCompletion = true, direction = SearchDirection.Before)
                expectHead("1", null, matchingHeads, clearHolderOnCompletion = true, direction = SearchDirection.After)
            }
            varReference(GroupFields.ElementsType.fieldName, "exemplar", extractConceptHead)
            saveAsObject()
        }.demons

    override fun disambiguationDemons(wordContext: WordContext,disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.Before,
                1,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                1,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

class WordCommaBoundary: WordHandler(EntryWord(",", noSuffix = true)) {
    private val matchingHeads = listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Clause.Boundary.name) {
            ignoreHolder()
        }.demons

    override fun disambiguationDemons(wordContext: WordContext,disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(
                matchNot(matchConceptByHead(matchingHeads)),
                SearchDirection.Before,
                1,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchNot(matchConceptByHead(matchingHeads)),
                SearchDirection.After,
                1,
                wordContext,
                disambiguationHandler
            )
        )
    }
}