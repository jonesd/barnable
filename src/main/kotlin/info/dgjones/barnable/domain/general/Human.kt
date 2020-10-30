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

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.parser.*

enum class HumanConcept {
    Human
}

enum class HumanFields(override val fieldName: String): Fields {
    FirstName("firstName"),
    LastName("lastName"),
    Gender("gender")
}

enum class Gender {
    Male,
    Female,
    Neutral
}

fun characterMatcher(human: Concept): ConceptMatcher =
    characterMatcher(human.valueName(HumanFields.FirstName),
        human.valueName(HumanFields.LastName),
        human.valueName(HumanFields.Gender),
        human.valueName(RoleThemeFields.RoleTheme.fieldName)
    )

fun characterMatcher(firstName: String?, lastName: String?, gender: String?, roleTheme: String?): ConceptMatcher =
    ConceptMatcherBuilder()
        .with(matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
        .matchSetField(HumanFields.FirstName, firstName)
        .matchSetField(HumanFields.LastName, lastName)
        .matchSetField(RoleThemeFields.RoleTheme, roleTheme)
        .matchSetField(HumanFields.Gender, gender)
        .matchAll()

fun characterMatcherWithInstance(human: Concept): ConceptMatcher =
    ConceptMatcherBuilder()
        .with(characterMatcher(human))
        .matchSetField(CoreFields.Instance, human.valueName(CoreFields.Instance))
        .matchAll()


fun LexicalConceptBuilder.human(firstName: String = "", lastName: String = "", gender: Gender? = null, initializer: LexicalConceptBuilder.() -> Unit): Concept {
    val child = LexicalConceptBuilder(root, InDepthUnderstandingConcepts.Human.name)
    child.slot(HumanFields.FirstName, firstName)

    return Concept(InDepthUnderstandingConcepts.Human.name)
        .with(Slot(HumanFields.FirstName, Concept(firstName)))
        .with(Slot(HumanFields.LastName, Concept(lastName)))
        //FIXME empty concept doesn't seem helpful
        .with(Slot(HumanFields.Gender, Concept(gender?.name ?: "")))
}

fun buildHuman(firstName: String? = "", lastName: String? = "", gender: String? = null): Concept {
    return Concept(InDepthUnderstandingConcepts.Human.name)
        .with(Slot(HumanFields.FirstName, Concept(firstName ?: "")))
        .with(Slot(HumanFields.LastName, Concept(lastName ?: "")))
        .with(Slot(HumanFields.Gender, Concept(gender ?: "")))
}

fun humanKeyValue(human: Concept) =
    human.selectKeyValue(HumanFields.FirstName, HumanFields.LastName, RoleThemeFields.RoleTheme)

// Word Senses

fun buildGeneralHumanLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordPerson(buildHuman("Ann", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Anne", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Bill", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("Fred", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("George", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("Jane", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("John", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("Mary", "", Gender.Female.name)))
}

class WordPerson(val human: Concept, word: String = human.valueName(HumanFields.FirstName)?:"unknown"): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            // FIXME not sure about defaulting to ""
            slot(HumanFields.FirstName, human.valueName(HumanFields.FirstName) ?: "")
            lastName(HumanFields.LastName)
            //slot(Human.LAST_NAME, human.valueName(Human.LAST_NAME) ?: "")
            slot(HumanFields.Gender, human.valueName(HumanFields.Gender)?: "")
            checkCharacter(CoreFields.Instance.fieldName)
        }.demons
}

fun LexicalConceptBuilder.lastName(slotName: Fields, variableName: String? = null) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = LastNameDemon(root.wordContext) {
        if (it != null) {
            root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
        }
    }
    root.addDemon(demon)
}

class LastNameDemon(wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val matcher = matchConceptByHead(InDepthUnderstandingConcepts.UnknownWord.name)
        searchContext(matcher, matchNever(), direction = SearchDirection.After, distance = 1, wordContext = wordContext) { holder ->
            holder.value?.let {
                val lastName = it.value("word")
                active = false
                action(lastName)
            }
        }
    }
    override fun description(): String {
        return "If an unknown word immediately follows,\nThen assume it is a character's last name\nand update character information."
    }
}