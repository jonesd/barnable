package info.dgjones.au.domain.general

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.concept.lexicalConcept
import info.dgjones.au.concept.matchConceptByHead
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

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
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.FIRST_NAME, "")
            lastName(HumanFields.LAST_NAME)
            slot(HumanFields.GENDER, gender.name)
            // FIXME include title
            checkCharacter(CoreFields.INSTANCE.fieldName)
        }
        return lexicalConcept.demons
    }
    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(listOf(InDepthUnderstandingConcepts.UnknownWord.name)), SearchDirection.After, 1, wordContext, disambiguationHandler)
        )
    }
}