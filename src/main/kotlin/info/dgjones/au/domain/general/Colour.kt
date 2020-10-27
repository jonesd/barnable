package info.dgjones.au.domain.general

import info.dgjones.au.concept.Fields
import info.dgjones.au.grammar.ModifierWord
import info.dgjones.au.parser.Lexicon

enum class Colour {
    Colour
}

enum class ColourFields(override val fieldName: String): Fields {
    Colour("colour")
}

enum class Colours(val title: String) {
    Beige("beige"),
    Black("black"),
    Blue("blue"),
    Brown("brown"),
    Gray("gray"),
    Green("green"),
    Grey("grey"),
    Orange("orange"),
    Pink("pink"),
    Purple("purple"),
    Red("red"),
    Teal("teal"),
    White("white"),
    Yellow("yellow"),
}

fun buildGeneralColourLexicon(lexicon: Lexicon) {
    Colours.values().forEach { lexicon.addMapping(ModifierWord(it.title, ColourFields.Colour)) }
}