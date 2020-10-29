package info.dgjones.barnable.domain.general

import info.dgjones.barnable.parser.*

/**
 * List of common foods that are stored as a kind of PhysicalObject
 */
enum class Foods(override val title: String, override val kind: PhysicalObjectKind = PhysicalObjectKind.Food): PhysicalObjectDefinitions {
    Lobster("lobster"),
    Sugar("sugar")
}

fun buildGeneralFoodLexicon(lexicon: Lexicon) {
    Foods.values().forEach { lexicon.addMapping(PhysicalObjectWord(it)) }
}
