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
import info.dgjones.barnable.domain.general.GeneralConcepts
import info.dgjones.barnable.parser.*


enum class ConjunctionConcept {
    And,
    Or
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
    lexicon.addMapping(WordCommaAddToGroup())

    lexicon.addMapping(WordCommaBoundary())

    lexicon.addMapping(WordOrBuildAlternatives())
}

class WordAnd: WordHandler(EntryWord("and")) {
    override fun build(wordContext: WordContext): List<Demon> {
        // Only handles grouper
        wordContext.defHolder.value = Concept(GroupConcept.Group.name)
        // wordContext.defHolder.value = buildConjunction(Conjunction.And.name)
        return listOf(IgnoreDemon(wordContext))
    }
}

private val groupMatchingHeads: List<String> = listOf(GeneralConcepts.Human.name, GeneralConcepts.PhysicalObject.name/*, MeteorologyConcept.Weather.name*/)
private val groupMatchingHeadsForOr: List<String> = listOf(GeneralConcepts.Human.name, GeneralConcepts.PhysicalObject.name, MeteorologyConcept.Weather.name)

/*
Handle the scenario of "george and harold" forming a group of two persons
 */
class WordAndBuildGroup: WordHandler(EntryWord("and")) {
    private val matchingHeads = groupMatchingHeads
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GroupConcept.Group.name) {
            slot(GroupFields.Elements, GroupConcept.Values.name) {
                expectHead("0", "exemplar", matchingHeads, clearHolderOnCompletion = false, markAsInside = false, direction = SearchDirection.Before)
                expectHead("1", null, matchingHeads, clearHolderOnCompletion = true, direction = SearchDirection.After)
            }
            slot(GroupFields.Conjunction.fieldName, ConjunctionConcept.And.name)
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
                false,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                1,
                false,
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
    private val matchingHeads = groupMatchingHeads
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
                false,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                1,
                false,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

/*
Comma will form a group for specific concept types.
 */
class WordCommaBuildGroup: WordHandler(EntryWord(",", noSuffix = true)) {
    private val matchingHeads = groupMatchingHeads
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GroupConcept.Group.name) {
            slot(GroupFields.Elements,  GroupConcept.Values.name) {
                expectHead("0", "exemplar", matchingHeads, clearHolderOnCompletion = true, direction = SearchDirection.Before)
                expectHead("1", null, matchingHeads, clearHolderOnCompletion = true, direction = SearchDirection.After)
            }
            slot(GroupFields.Conjunction.fieldName, ConjunctionConcept.And.name)
            varReference(GroupFields.ElementsType.fieldName, "exemplar", extractConceptHead)
            saveAsObject()
        }.demons

    override fun disambiguationDemons(wordContext: WordContext,disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.Before,
                1,
                false,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                null /*1*/,
                false,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

class WordCommaAddToGroup: WordHandler(EntryWord(",", noSuffix = true)) {
    private val matchingHeads = groupMatchingHeads
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
                false,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                null /*1*/,
                false,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

class WordCommaBoundary: WordHandler(EntryWord(",", noSuffix = true)) {
    private val matchingHeads = groupMatchingHeads
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
                false,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchNot(matchConceptByHead(matchingHeads)),
                SearchDirection.After,
                1,
                false,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

/*
Handle the scenario of "rain or showers" forming a group of two items of the same matching head
 */
class WordOrBuildAlternatives: WordHandler(EntryWord("or")) {
    private val matchingHeads = groupMatchingHeadsForOr
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GroupConcept.Group.name) {
            slot(GroupFields.Elements,  GroupConcept.Values.name) {
                expectHead("0", "exemplar", matchingHeads, clearHolderOnCompletion = false, markAsInside = false, direction = SearchDirection.Before)
                expectHead("1", null, matchingHeads, clearHolderOnCompletion = true, direction = SearchDirection.After)
            }
            varReference(GroupFields.ElementsType.fieldName, "exemplar", extractConceptHead)
            slot(GroupFields.Conjunction.fieldName, ConjunctionConcept.Or.name)
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
                false,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchConceptByHead(matchingHeads),
                SearchDirection.After,
                1,
                false,
                wordContext,
                disambiguationHandler
            )
        )
    }
}
