package info.dgjones.barnable.domain.general

import info.dgjones.barnable.parser.Lexicon

enum class Liquids(override val title: String, override val kind: PhysicalObjectKind = PhysicalObjectKind.Liquid): PhysicalObjectDefinitions {
    CocaCola("Coca-Cola"),
    Coffee("coffee"),
    Coke("coke"),
    Soda("soda"),
    Tea("tea"),
    Water("water"),
    Wine("wine")
}

fun buildGeneralLiquidLexicon(lexicon: Lexicon) {
    Liquids.values().forEach { lexicon.addMapping(PhysicalObjectWord(it)) }
}
