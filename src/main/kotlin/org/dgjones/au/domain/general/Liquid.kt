package org.dgjones.au.domain.general

import org.dgjones.au.narrative.PhysicalObjectKind
import org.dgjones.au.parser.*

enum class Liquids {
    `Coca-Cola`,
    coffee,
    coke,
    soda,
    tea,
    water,
}

fun buildGeneralLiquidLexicon(lexicon: Lexicon) {
    Liquids.values().forEach { lexicon.addMapping(KindLiquid(it)) }
}

class KindLiquid(private val liquid: Liquids):  WordHandler(EntryWord(liquid.name)) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildLiquid(liquid, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}

fun buildLiquid(kindOfLiquid: Liquids, name: String = kindOfLiquid.name): Concept {
    return Concept(PhysicalObjectKind.Liquid.name)
        .with(Slot("kind", Concept(kindOfLiquid.name)))
        .with(Slot("name", Concept(name)))
}
