package info.dgjones.au.domain.general

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.CoreFields
import info.dgjones.au.concept.Slot
import info.dgjones.au.parser.*

enum class Liquids {
    `Coca-Cola`,
    coffee,
    coke,
    soda,
    tea,
    water,
    wine
}

fun buildGeneralLiquidLexicon(lexicon: Lexicon) {
    Liquids.values().forEach { lexicon.addMapping(LiquidWord(it)) }
}

class LiquidWord(private val liquid: Liquids):  WordHandler(EntryWord(liquid.name)) {
    override fun build(wordContext: WordContext): List<Demon> =
        buildLexicalPhysicalObject(PhysicalObjectKind.Liquid.name, liquid.name, wordContext).demons
}

