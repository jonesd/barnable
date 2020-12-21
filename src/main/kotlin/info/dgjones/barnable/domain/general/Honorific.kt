/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.concept.matchConceptByHead
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
        lexicalConcept(wordContext, GeneralConcepts.Human.name) {
            slot(HumanFields.FirstName, "")
            lastName(HumanFields.LastName)
            slot(HumanFields.Gender, gender.name)
            // FIXME include title
            checkCharacter(CoreFields.Instance.fieldName)
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(listOf(GeneralConcepts.UnknownWord.name)), SearchDirection.After, 1, wordContext, disambiguationHandler)
        )
    }
}