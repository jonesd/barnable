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

package info.dgjones.barnable.parser

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.narrative.HumanAccessor
import info.dgjones.barnable.domain.general.GeneralConcepts

// Find matching human in episodic memory, and associate concept
// InDepth p185
fun LexicalConceptBuilder.checkCharacter(slotName: String, variableName: String? = null) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val checkCharacterDemon = CheckCharacterDemon(concept, root.wordContext) { episodicCharacter ->
        if (episodicCharacter != null) {
            val episodicInstance = episodicCharacter.value(CoreFields.Instance)
            root.completeVariable(variableSlot, root.wordContext.context.workingMemory.createDefHolder(episodicInstance))
            this.episodicConcept = episodicCharacter
            copyCompletedSlot(HumanFields.FirstName, episodicCharacter, concept)
            copyCompletedSlot(HumanFields.LastName, episodicCharacter, concept)
            copyCompletedSlot(HumanFields.Gender, episodicCharacter, concept)
            root.wordContext.context.workingMemory.markAsRecentCharacter(concept)
        } else {
            println("Creating character ${concept.valueName(HumanFields.FirstName)} in EP memory")
            root.wordContext.context.workingMemory.markAsRecentCharacter(concept)
        }
    }
    root.addDemon(checkCharacterDemon)
}

// Find recent character with matching gender in Working Memory
// May be replaced by better value using knowledge layer
// InDepth p185
fun LexicalConceptBuilder.findCharacter(slotName: String, variableName: String? = null) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = FindCharacterDemon(concept.valueName(HumanFields.Gender), root.wordContext) {
        if (it != null) {
            root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
        }
    }
    root.addDemon(demon)
}

fun LexicalConceptBuilder.nextChar(slotName: String, variableName: String? = null, relRole: String? = null) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = NextCharacterDemon(root.wordContext) {
        if (it != null) {
            root.completeVariable(variableSlot, it, this.episodicConcept)
        }
    }
    root.addDemon(demon)
}

// Search working memory for tentative character based on gender
// InDepth p182
class FindCharacterDemon(val gender: String?, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        if (gender != null) {
            val matchedCharacter = wordContext.context.workingMemory.findCharacterByGender(gender)
            action(matchedCharacter)
            active = false
        }
    }

    override fun description(): String {
        return "FindCharacter from working memory gender=$gender"
    }
}

class SaveCharacterDemon(wordContext: WordContext): Demon(wordContext){
    override fun run() {
        val character = wordContext.def()
        if (character != null && HumanAccessor(character).isCompatible()) {
            wordContext.context.workingMemory.markAsRecentCharacter(character)
            active = false
        } else {
            println("SaveCharacter failed as def = $character")
        }
    }

    override fun description(): String {
        return "SaveCharacter ${wordContext.def()}"
    }
}

class NextCharacterDemon(wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        val matcher = matchConceptByHead(GeneralConcepts.Human.name)
        searchContext(matcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
            if (it.value != null) {
                active = false
                action(it)
            }
            //FIXME also look for unattached preceding Human
        }
    }

    override fun description(): String {
        return "NextCharacter"
    }
}

/* Find matching character in episodic memory. Only runs once */
class CheckCharacterDemon(val human: Concept, wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val matchedCharacter = wordContext.context.episodicMemory.checkOrCreateCharacter(human)
        action(matchedCharacter)
        active = false
    }
    override fun description(): String {
        return "CheckCharacter from episodic with ${human.valueName(HumanFields.FirstName)} ${human.valueName(
            HumanFields.Gender)}"
    }
}
