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

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.Gender
import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.domain.general.GeneralConcepts
import info.dgjones.barnable.episodic.innerInstance
import info.dgjones.barnable.grammar.possessiveRef
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
        lexicalConcept(wordContext, GeneralConcepts.Human.name) {
            slot(HumanFields.Gender, Gender.Female.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Husband, gender = Gender.Male)
                nextChar(Marriage.Wife.fieldName, relRole = "Wife")
                checkRelationship(CoreFields.Instance, waitForSlots = listOf(Marriage.Husband.fieldName, Marriage.Wife.fieldName))
            }
            innerInstance(CoreFields.Instance, observeSlot = Marriage.Wife.fieldName)
        }.demons
}

class WordHusband: WordHandler(EntryWord("husband")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, GeneralConcepts.Human.name) {
            slot(HumanFields.Gender, Gender.Male.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Wife, gender = Gender.Female)
                nextChar(Marriage.Husband.fieldName, relRole = "Husband")
                checkRelationship(CoreFields.Instance, waitForSlots = listOf(Marriage.Husband.fieldName, Marriage.Wife.fieldName))
            }
            innerInstance(CoreFields.Instance, observeSlot = Marriage.Husband.fieldName)
        }.demons
}

// Lexical

// InDepth p185
fun LexicalConceptBuilder.checkRelationship(slotName: Fields, variableName: String? = null, waitForSlots: List<String>) {
    val variable = root.createVariable(slotName.fieldName, variableName)
    concept.with(variable.slot())
    val demon = CheckRelationshipDemon(concept, waitForSlots, root.wordContext) {
        if (it != null) {
            variable.complete(root.createDefHolder(it), root.wordContext, this.episodicConcept)
        }
    }
    root.addDemon(demon)
}

class CheckRelationshipDemon(private var parent: Concept, private var dependentSlotNames: List<String>, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val rootConcept =  wordContext.defHolder.value
        if (rootConcept != null && isDependentSlotsComplete(parent, dependentSlotNames)) {
            val instance = checkEpisodicRelationship(parent, wordContext.context.episodicMemory)
            active = false
            action(Concept(instance))
        }
    }
    override fun description(): String {
        return "CheckRelationship waitingFor $dependentSlotNames"
    }
}
