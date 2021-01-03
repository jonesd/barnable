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
import info.dgjones.barnable.parser.*

enum class QuantityConcept {
    Quantity,
    Measure
}

enum class QuantityFields(override val fieldName: String): Fields {
    Amount("amount"),
    Unit("unit"),
    Of("of")
}

fun buildGeneralQuantityLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordMeasureQuantity())
    }

/**
 * Word sense for measuring a quantity of a substance.
 *
 * "two measures of sugar"
 */
class WordMeasureQuantity : WordHandler(EntryWord("measure")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, QuantityConcept.Quantity.name) {
            // FIXME also support "a measure of ..." = default to 1 unit
            expectHead(QuantityFields.Amount.fieldName, headValue = NumberConcept.Number.name, direction = SearchDirection.Before)
            slot(QuantityFields.Unit, QuantityConcept.Measure.name)
            expectConcept(QuantityFields.Of.fieldName, matcher = matchConceptByKind(listOf(PhysicalObjectKind.Liquid.name, PhysicalObjectKind.Food.name)))
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingWord("of", matchConceptByKind(listOf(PhysicalObjectKind.Food.name, PhysicalObjectKind.Liquid.name)), SearchDirection.After, false, wordContext, disambiguationHandler)
        )
    }
}