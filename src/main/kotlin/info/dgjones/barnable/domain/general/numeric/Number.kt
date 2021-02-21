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
import info.dgjones.barnable.grammar.defaultModifierTargetMatcher
import info.dgjones.barnable.parser.*
import info.dgjones.barnable.util.transformCamelCaseToLowerCaseList
import info.dgjones.barnable.util.transformCamelCaseToSeparatedWords
import info.dgjones.barnable.util.transformCamelCaseToSpaceSeparatedWords

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

    Dozen(12, true),
    Bakers_Dozen(13, true),

    Score(20, true),

    /**
     * "'long hundred' ... the number that was referred to as "hundred" in Germanic languages prior to the 15th century, which is now known as
     * 120, one hundred and twenty, or six score"
     * -- https://en.wikipedia.org/wiki/Long_hundred
     */
    LongHundred(LONG_HUNDRED_VALUE, true),
    GreatHundred(LONG_HUNDRED_VALUE, true),
    Twelfty(LONG_HUNDRED_VALUE, true),
    LongThousand(LONG_THOUSAND_VALUE, true),
    ShortHundred(SHORT_HUNDRED_VALUE, true);

    val words = transformCamelCaseToLowerCaseList(name)
}

private const val SHORT_HUNDRED_VALUE = 100;
private const val LONG_HUNDRED_VALUE = 120;
private const val LONG_THOUSAND_VALUE = 1200;

fun buildGeneralNumberLexicon(lexicon: Lexicon) {
    buildNumberValueAnd(lexicon)
    buildNumberValueWords(lexicon)
    buildNumberValueFromDigitsWord(lexicon)
}

private fun buildNumberValueAnd(lexicon: Lexicon) {
    lexicon.addMapping(AndNumberElement())
}

private fun buildNumberValueFromDigitsWord(lexicon: Lexicon) {
    lexicon.addUnknownHandler(NumberDigitsUnregistered())
}

private fun buildNumberValueWords(lexicon: Lexicon) {
    NumberValues.values().forEach {
        lexicon.addMapping(NumberHandler(it))
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
                true,
                wordContext,
                disambiguationHandler
            ),
            DisambiguateUsingMatch(
                matchingNumberElement,
                SearchDirection.After,
                1,
                true,
                wordContext,
                disambiguationHandler
            )
        )
    }
}

/**
 * Handle individual number element that will be collated together into the calculating the full number value.
 */
class NumberHandler(var value: NumberValues): WordHandler(EntryWord(value.words[0], value.words)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, NumberConcept.Number.name) {
            slot(NumberFields.Value, value.value.toString())
            pushValueToInitialNumberElement(value.name)
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
        fun calculateValue(word: String): Int {
            return try {
                NumberValues.valueOf(word).value
            } catch (e: IllegalArgumentException) {
                word.toInt()
            }
        }
        fun accumulateAsMultiple(word: String): Boolean {
            return try {
                NumberValues.valueOf(word).accumulateAsMultiple
            } catch (e: IllegalArgumentException) {
                false
            }
        }
        fun currentOrUnit(current: Int): Int =
            if (current != 0 ) current else 1

        fun accumulate(word: String, current: Int): Int {
            val value = calculateValue(word)
            return if (accumulateAsMultiple(word)) currentOrUnit(current) * value else current + value
        }

        fun calculateFromCollectedValues(words: List<String>): Int {
            return words.fold( 0, {acc, next ->  accumulate(next, acc)})
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

/**
 * Handle numbers represented as digits. Used when no matching "word" has been registered.
 */
class NumberDigitsUnregistered: WordHandler(EntryWord("")) {
    fun sanitizeNumberWord(text: String):String = text.filter { it != ',' }

    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, NumberConcept.Number.name) {
            slot(NumberFields.Value, sanitizeNumberWord(wordContext.word))
            pushValueToInitialNumberElement(sanitizeNumberWord(wordContext.word))
            copySlotValueToConcept(NumberFields.Value, defaultModifierTargetMatcher(), QuantityFields.Amount, wordContext)
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingSentenceWordRegex(
                """^-?([1-9][0-9]{0,2}(,[0-9]{3})*|[0-9]+)$""".toRegex(),
                false,
                wordContext,
                disambiguationHandler
            )
        )
    }
}