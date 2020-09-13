package info.dgjones.au.domain.general

import info.dgjones.au.parser.Lexicon

fun buildGeneralDomainLexicon(lexicon: Lexicon) {
    buildGeneralFoodLexicon(lexicon)
    buildGeneralLiquidLexicon(lexicon)
}