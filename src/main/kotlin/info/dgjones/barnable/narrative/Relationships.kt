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

package info.dgjones.barnable.narrative

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.domain.general.Gender
import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.parser.*

enum class Relationships(override val fieldName: String): Fields {
    Name("Relationship")
}

enum class Marriage(override val fieldName: String): Fields {
    Concept("R-Marriage"),
    Wife("wife"),
    Husband("husband")
}

// Word Sense

fun buildNarrativeRelationshipLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordHusband())
    lexicon.addMapping(WordWife())
}

class WordWife: WordHandler(EntryWord("wife")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.GENDER, Gender.Female.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Husband, gender = Gender.Male)
                nextChar(Marriage.Wife.fieldName, relRole = "Wife")
                checkRelationship(CoreFields.INSTANCE, waitForSlots = listOf(Marriage.Husband.fieldName, Marriage.Wife.fieldName))
            }
            innerInstan(CoreFields.INSTANCE.fieldName, observeSlot = Marriage.Wife.fieldName)
        }.demons
}

class WordHusband: WordHandler(EntryWord("husband")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.GENDER, Gender.Male.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Wife, gender = Gender.Female)
                nextChar(Marriage.Husband.fieldName, relRole = "Husband")
                checkRelationship(CoreFields.INSTANCE, waitForSlots = listOf(Marriage.Husband.fieldName, Marriage.Wife.fieldName))
            }
            innerInstan(CoreFields.INSTANCE.fieldName, observeSlot = Marriage.Husband.fieldName)
        }.demons
}