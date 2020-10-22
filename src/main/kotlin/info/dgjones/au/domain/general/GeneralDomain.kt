package info.dgjones.au.domain.general

import info.dgjones.au.parser.Lexicon

fun buildGeneralDomainLexicon(lexicon: Lexicon) {
    buildGeneralPhysicalObjectsLexicon(lexicon)
    buildGeneralFoodLexicon(lexicon)
    buildGeneralLiquidLexicon(lexicon)
    buildGeneralHumanLexicon(lexicon)
    buildGeneralHonorificLexicon(lexicon)
    buildGeneralRoleThemeLexicon(lexicon)
    buildGeneralTimeLexicon(lexicon)
    buildGeneralNumberLexicon(lexicon)
    buildGeneralQuantityLexicon(lexicon)
    buildGeneralColourLexicon(lexicon)
}