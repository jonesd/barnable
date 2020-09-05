package org.dgjones.au.domain.general

import org.dgjones.au.parser.Lexicon

fun buildGeneralDomainLexicon(lexicon: Lexicon) {
    buildGeneralFoodLexicon(lexicon)
    buildGeneralLiquidLexicon(lexicon)
}