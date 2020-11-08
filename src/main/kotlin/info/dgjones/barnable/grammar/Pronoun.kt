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

package info.dgjones.barnable.grammar

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.domain.general.Gender
import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.parser.*

/*
pronoun
A part of speech that acts as a substitute for a noun or noun phrase and refers to a topic of the discussion.
Pronouns can refer to a participant in the discussion and can be used instead of a person's name, which is the case for
pronouns I and you.
Other pronouns, such as he, she, and it, can be used to refer to other people or objects that have already been
mentioned without repeating their names.
https://en.wiktionary.org/wiki/Appendix:Glossary#pronoun
 */

fun buildGrammarPronounLexicon(lexicon: Lexicon) {
    // FIXME not sure about these mappings...
    lexicon.addMapping(WordPronoun("hers", Gender.Female, Case.Possessive))
    lexicon.addMapping(WordPronoun("his", Gender.Male, Case.Possessive))
    lexicon.addMapping(WordPronoun("her", Gender.Female, Case.Possessive))
    lexicon.addMapping(WordPronoun("him", Gender.Male, Case.Objective))

    // FIXME why aren't these WordPronoun?
    lexicon.addMapping(PronounWord("he", Gender.Male))
    lexicon.addMapping(PronounWord("she", Gender.Female))
}

class WordPronoun(word: String, val gender: Gender, val case: Case): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Ref.name) {
            ignoreHolder()
            slot(GrammarFields.Case, case.name)
            slot(HumanFields.Gender, gender.name)
            findCharacter(CoreFields.Instance.fieldName)
        }.demons
}

class PronounWord(word: String, private val genderMatch: Gender): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        // FIXME partial implementation - also why not use demon
        wordContext.defHolder.addFlag(ParserFlags.Ignore)
        val localHuman = wordContext.context.localCharacter
        if (localHuman != null && localHuman.valueName("gender") == genderMatch.name) {
            wordContext.defHolder.value = localHuman
        } else {
            val mostRecentHuman = wordContext.context.mostRecentCharacter
            if (mostRecentHuman != null && mostRecentHuman.valueName("gender") == genderMatch.name) {
                wordContext.defHolder.value = mostRecentHuman
            }
        }
        if (!wordContext.isDefSet()) {
            val mostRecentHuman = wordContext.context.workingMemory.charactersRecent.firstOrNull { it.valueName("gender") == genderMatch.name }
            if (mostRecentHuman != null) {
                wordContext.defHolder.value = mostRecentHuman
            }
        }
        return listOf()
    }
}