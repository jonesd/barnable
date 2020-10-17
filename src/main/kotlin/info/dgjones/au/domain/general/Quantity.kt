package info.dgjones.au.domain.general

import info.dgjones.au.concept.*
import info.dgjones.au.parser.*

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
            DisambiguateUsingWord("of", matchConceptByKind(listOf(PhysicalObjectKind.Food.name, PhysicalObjectKind.Liquid.name)), SearchDirection.After, wordContext, disambiguationHandler)
        )
    }
}