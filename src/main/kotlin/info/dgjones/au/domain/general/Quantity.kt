package info.dgjones.au.domain.general

import info.dgjones.au.concept.Fields
import info.dgjones.au.concept.lexicalConcept
import info.dgjones.au.concept.matchConceptByHead
import info.dgjones.au.narrative.PhysicalObjectKind
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

class WordMeasureQuantity : WordHandler(EntryWord("measure")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, QuantityConcept.Quantity.name) {
            // FIXME also support "a measure of ..." = default to 1 unit
            expectHead(QuantityFields.Amount.fieldName, headValue = NumberConcept.Number.name, direction = SearchDirection.Before)
            slot(QuantityFields.Unit, QuantityConcept.Measure.name)
            expectHead(QuantityFields.Of.fieldName, headValues = listOf(PhysicalObjectKind.Liquid.name, PhysicalObjectKind.Food.name))
        }
        return lexicalConcept.demons
    }

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingWord("of", matchConceptByHead(listOf(PhysicalObjectKind.Food.name, PhysicalObjectKind.Liquid.name)), SearchDirection.After, wordContext, disambiguationHandler)
        )
    }
}