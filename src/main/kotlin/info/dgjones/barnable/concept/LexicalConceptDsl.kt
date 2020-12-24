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

package info.dgjones.barnable.concept

import info.dgjones.barnable.episodic.EpisodicConcept
import info.dgjones.barnable.parser.*

/* DSL to help build Concept as part of a word sense.
It also triggers demons to populate the concept slots.
 */
class LexicalRootBuilder(val wordContext: WordContext, private val headName: String) {
    val root = LexicalConceptBuilder(this, headName)

    val demons = mutableListOf<Demon>()

    private val variableSpace = VariableSpace()

    private val disambiguations = mutableListOf<Demon>()
    private var totalSuccessfulDisambiguations = 0

    fun build(): LexicalConcept {
        return LexicalConcept(wordContext, root.build(), demons, disambiguations)
    }

    // FIXME shouldn't associate this state that lives on beyond build() to builder
    // should create new holder instead
    fun createVariable(slotName: Fields, variableName: String? = null, expression: ConceptTransformer? = null): CompletableVariable {
        return createVariable(slotName.fieldName, variableName, expression)
    }

    fun createVariable(slotName: String, variableName: String? = null, expression: ConceptTransformer? = null): CompletableVariable {
        val variable = CompletableVariable(slotName, variableSpace, variableName, expression)
        variableSpace.declareVariable(variable)
        return variable
    }

    fun addVariableReference(slotName: Fields, variableName: String, expression: ConceptTransformer? = null): Slot =
        addVariableReference(slotName.fieldName, variableName, expression)

    fun addVariableReference(slotName: String, variableName: String, expression: ConceptTransformer? = null): Slot {
        val sourceVariable = variableSpace.getVariable(variableName)
        return sourceVariable.addVariableReference(slotName, expression)
    }

    fun completeVariable(variable: CompletableVariable, value: Concept, episodicConcept: EpisodicConcept? = null, wordContext: WordContext = this.wordContext, markAsInside: Boolean = true
    ) {
        val conceptHolder = wordContext.context.workingMemory.createDefHolder(value)
        completeVariable(variable, conceptHolder, episodicConcept, wordContext, markAsInside)
    }

    fun completeVariable(variable: CompletableVariable, valueHolder: ConceptHolder, episodicConcept: EpisodicConcept? = null, wordContext: WordContext = this.wordContext, markAsInside: Boolean = true) {
        variable.complete(valueHolder, wordContext, episodicConcept, markAsInside)
    }

    fun overwriteHolderWithMatchedVariable(variableName: String, concept: Concept) {
        val sourceVariable = variableSpace.getVariable(variableName)
        sourceVariable.overwriteResolvedHolder = concept
    }

    fun disambiguationResult(result: Boolean) {
        this.totalSuccessfulDisambiguations += 1
    }
    fun addDemon(demon: Demon) {
        this.demons.add(demon)
    }
    fun addDisambiguationDemon(demon: Demon) {
        this.disambiguations.add(demon)
    }
}

/*class LexicalConceptDisambiguateBuilder(val root: LexicalRootBuilder) {
    fun disambiguateUsingWord(word: String, heads: List<String>, direction: SearchDirection = SearchDirection.After) {
        val demon = DisambiguateUsingWord(word, matchConceptByHead(heads), direction, root.wordContext) {
            root.disambiguationResult(true)
        }
        root.addDisambiguationDemon(demon)
    }
    fun disambiguate(head: String, direction: SearchDirection = SearchDirection.After) {
        disambiguate(listOf(head), direction)
    }
    fun disambiguate(heads: List<String>, direction: SearchDirection = SearchDirection.After) {
        val demon = DisambiguateUsingMatch(matchConceptByHead(heads), direction, root.wordContext) {
            root.disambiguationResult(true)

        }
        root.addDisambiguationDemon(demon)
    }
}*/


class LexicalConceptBuilder(val root: LexicalRootBuilder, conceptName: String) {
    val concept = Concept(conceptName)
    var episodicConcept: EpisodicConcept? = null

    fun ignoreHolder() {
        root.wordContext.defHolder.addFlag(ParserFlags.Ignore)
    }

    fun slot(slotName: Fields, slotValue: String) {
        slot(slotName.fieldName, slotValue)
    }
    fun slot(slotName: String, slotValue: String) {
        concept.with(Slot(slotName, Concept(slotValue)))
    }
    fun slot(slotName: Fields, slotValue: String, initializer: LexicalConceptBuilder.() -> Unit) {
        slot(slotName.fieldName, slotValue, initializer)
    }
    fun slot(slotName: String, slotValue: String, initializer: LexicalConceptBuilder.() -> Unit) {
        val child = LexicalConceptBuilder(root, slotValue)
        child.apply(initializer)
        val c = child.build()
        concept.with(Slot(slotName, c))
    }
    fun build(): Concept {
        return concept
    }
    fun expectHead(slotName: String, variableName: String? = null, headValue: String, clearHolderOnCompletion: Boolean = false, markAsInside: Boolean = true, direction: SearchDirection = SearchDirection.After) {
        expectHead(slotName, variableName, listOf(headValue), clearHolderOnCompletion, markAsInside, direction)
    }
    fun expectHead(slotName: String, variableName: String? = null, headValues: List<String>, clearHolderOnCompletion: Boolean = false, markAsInside: Boolean = true, direction: SearchDirection = SearchDirection.After) {
        val variable = root.createVariable(slotName, variableName)
        concept.with(variable.slot())
        val demon = ExpectDemon(matchConceptByHead(headValues), direction, root.wordContext) {
            root.completeVariable(variable, it, this.episodicConcept, markAsInside = markAsInside)
            if (clearHolderOnCompletion) {
                it.value = null
            }
        }
        root.addDemon(demon)
    }

    /**
     * General find a concept from the concept sentence and extract out the specified concept value
     */
    fun expectConcept(slotName: String, variableName: String? = null, conceptField: String? = null, matcher: ConceptMatcher, direction: SearchDirection = SearchDirection.After) {
        val variable = root.createVariable(slotName, variableName)
        concept.with(variable.slot())
        val demon = ExpectDemon(matcher, direction, root.wordContext) {
            if (conceptField != null) {
                // FIXME support sub-concept matching
                val value = it.value?.value(conceptField)
                // FIXME will never complete if value initially empty...
                if (value != null) {
                    root.completeVariable(variable, it, this.episodicConcept)
                    it.addFlag(ParserFlags.Inside)
                }
            } else {
                root.completeVariable(variable, it, this.episodicConcept)
            }
        }
        root.addDemon(demon)
    }

    fun expectKind(slotName: String, variableName: String? = null, kinds: List<String>, direction: SearchDirection = SearchDirection.After) {
        val variable = root.createVariable(slotName, variableName)
        concept.with(variable.slot())
        val demon = ExpectDemon(matchConceptByKind(kinds), direction, root.wordContext) {
            root.completeVariable(variable, it, this.episodicConcept)
        }
        root.addDemon(demon)
    }

    fun varReference(slotName: String, variableName: String, expression: ConceptTransformer? = null) {
        val variableSlot = root.addVariableReference(slotName, variableName, expression)
        concept.with(variableSlot)
    }

    fun replaceWordContextWithCurrent(variableName: String) {
        root.overwriteHolderWithMatchedVariable(variableName, concept)
    }
}

fun lexicalConcept(wordContext: WordContext, headName: String, initializer: LexicalConceptBuilder.() -> Unit): LexicalConcept {
    val builder = LexicalRootBuilder(wordContext, headName)
    builder.root.apply(initializer)
    return builder.build()
}

class LexicalConcept(val wordContext: WordContext, val head: Concept, val demons: List<Demon>, val disambiguateDemons: List<Demon>) {
    init {
        wordContext.defHolder.value = head
    }
}
