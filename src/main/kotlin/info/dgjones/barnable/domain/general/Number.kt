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

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.grammar.ConjunctionConcept
import info.dgjones.barnable.grammar.defaultModifierTargetMatcher
import info.dgjones.barnable.parser.*

enum class NumberConcept {
    Number,
    NumberAnd
}

enum class NumberFields(override val fieldName: String): Fields {
    Value("value"),
    CollectedValues("collectedValues")
}

// Word Sense
enum class NumberValues(val value: Int, val accumulateAsMultiple: Boolean = false) {
    One(1),
    Two(2),
    Three( 3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
    Eight(8),
    Nine(9),
    Ten(10),
    Eleven(11),
    Twelve(12),
    Thirteen(13),
    Fourteen(14),
    Fifteen(15),
    Sixteen(16),
    Seventeen(17),
    Eighteen(18),
    Nineteen(19),
    Twenty(20),
    Thirty(30),
    Forty(40),
    Fifty(50),
    Sixty(60),
    Seventy(70),
    Eighty(80),
    Ninety(90),
    Hundred(100, true),
    Thousand(1000, true),
    Million(1000000, true),

    Dozen(12, true);

    fun accumulate(current: Int): Int =
        if (this.accumulateAsMultiple) currentOrUnit(current) * this.value else current + this.value

    private fun currentOrUnit(current: Int): Int =
        if (current != 0 ) current else 1
}

fun buildGeneralNumberLexicon(lexicon: Lexicon) {
    buildNumberValueAnd(lexicon)
    buildNumberValueWords(lexicon)
}

private fun buildNumberValueAnd(lexicon: Lexicon) {
    lexicon.addMapping(AndNumberElement())
}

private fun buildNumberValueWords(lexicon: Lexicon) {
    NumberValues.values().forEach {
        lexicon.addMapping(NumberHandler(it.value, it.name))
    }
}

/*
Handle the scenario of "hundred and one" where number elements will be collected together for the total number value
 */
class AndNumberElement: WordHandler(EntryWord("and")) {
    private val matchingNumberElement = matchConceptByHead(NumberConcept.Number.name)
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, NumberConcept.NumberAnd.name) {
            ignoreHolder()
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(
                matchingNumberElement,
                SearchDirection.Before,
                1,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchingNumberElement,
                SearchDirection.After,
                1,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

/**
 * Handle individual number element that will be collated together into the calculating the full number value.
 */
class NumberHandler(var value: Int, val name: String): WordHandler(EntryWord(name.toLowerCase())) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, NumberConcept.Number.name) {
            slot(NumberFields.Value, value.toString())
            pushValueToInitialNumberElement(name)
            copySlotValueToConcept(NumberFields.Value, defaultModifierTargetMatcher(), QuantityFields.Amount, wordContext)
        }.demons
}

private fun LexicalConceptBuilder.pushValueToInitialNumberElement(name: String) {
    val matchNumberOrAndNumber = matchConceptByHead(setOf(NumberConcept.Number.name, NumberConcept.NumberAnd.name))
    val demon = ExpectEarliestDemon(matchConceptByHead(NumberConcept.Number.name), abortSearch = matchNot(matchNumberOrAndNumber), wordContext = root.wordContext) {
        fun markSubsequentNumbersAsIgnored(firstNumberElement: Concept) {
            if (firstNumberElement !== root.wordContext.defHolder.value) {
                ignoreHolder()
            }
        }
        fun calculateFromCollectedValues(words: List<String>): Int {
            return words.map { NumberValues.valueOf(it) }.fold( 0, {acc, next ->  next.accumulate(acc)})
        }

        it.value?.let { firstNumberElement ->
            val values = firstNumberElement.value(NumberFields.CollectedValues)?: run {
                val list: Concept = buildConceptList(listOf())
                firstNumberElement.value(NumberFields.CollectedValues, list)
                list
            }
            val list = ConceptListAccessor(values)
            list.add(Concept(name))
            val total = calculateFromCollectedValues(list.valueNames())
            println("Updated ${root.wordContext.word} number = $total")
            firstNumberElement.value(NumberFields.Value, Concept(total.toString()))
            markSubsequentNumbersAsIgnored(firstNumberElement)
        }
    }
    root.addDemon(demon)
}
