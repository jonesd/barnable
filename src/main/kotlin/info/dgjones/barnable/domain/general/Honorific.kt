package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.concept.matchConceptByHead
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.parser.*

/**
 * Support Honorifics, such as Mr, Dr, etc.
 *
 * See: https://en.wikipedia.org/wiki/English_honorifics
 */
fun buildGeneralHonorificLexicon(lexicon: Lexicon) {
    // Common Titles
    lexicon.addMapping(TitleWord("Mr", Gender.Male))
    lexicon.addMapping(TitleWord("Mr.", Gender.Male))
    lexicon.addMapping(TitleWord("Mrs", Gender.Female))
    lexicon.addMapping(TitleWord("Mrs.", Gender.Female))
    lexicon.addMapping(TitleWord("Miss", Gender.Female))
    lexicon.addMapping(TitleWord("Ms", Gender.Female))
    lexicon.addMapping(TitleWord("Ms.", Gender.Female))
    lexicon.addMapping(TitleWord("Mx", Gender.Neutral))
}

class TitleWord(word: String, val gender: Gender): WordHandler(EntryWord(word, noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.FIRST_NAME, "")
            lastName(HumanFields.LAST_NAME)
            slot(HumanFields.GENDER, gender.name)
            // FIXME include title
            checkCharacter(CoreFields.INSTANCE.fieldName)
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(listOf(InDepthUnderstandingConcepts.UnknownWord.name)), SearchDirection.After, 1, wordContext, disambiguationHandler)
        )
    }
}