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
import info.dgjones.barnable.parser.ConceptHolder
import info.dgjones.barnable.parser.ParserFlags
import info.dgjones.barnable.parser.WordContext

/**
 * Record the unique variables within a Concept tree and the related referencing slots
 */
class VariableSpace() {
    private val variables = mutableMapOf<String, CompletableVariable>()

    fun declareVariable(variable: CompletableVariable) {
        check(!variables.containsKey(variable.variableName)) {"Variable ${variable.variableName} already declared in space"}
        variables.put(variable.variableName, variable)
    }

    fun getVariable(name: String): CompletableVariable {
        return variables.getValue(name)
    }

    fun nextVariableIndex(): Int {
        return variables.size + 1
    }
}

class CompletableVariable(sourceFieldName: String, variableSpace: VariableSpace, variableName: String? = null, val sourceExpression: ConceptTransformer? = null) {
    val variableName = variableName ?: variableSpace.nextVariableIndex().toString()
    val sourceReference = createVariableReference(sourceFieldName, sourceExpression)
    private val dependentReferences = mutableListOf<VariableReference>()
    var overwriteResolvedHolder: Concept? = null

    constructor(sourceField: Fields, variableSpace: VariableSpace, variableName: String?, sourceExpression: ConceptTransformer?): this(sourceField.fieldName, variableSpace, variableName, sourceExpression)

    fun slot() = sourceReference.variableSlot

    fun addVariableReference(referenceSlotField: Fields, expression: ConceptTransformer? = null) =
        addVariableReference(referenceSlotField.fieldName, expression)

    fun addVariableReference(referenceSlotFieldName: String, expression: ConceptTransformer? = null): Slot {
        val reference = createVariableReference(referenceSlotFieldName, expression)
        dependentReferences.add(reference)
        return reference.variableSlot
    }

    private fun createVariableReference(slotName: String, expression: ConceptTransformer? = null): VariableReference {
        return VariableReference(slotName, this.variableName, expression)
    }

    fun complete(valueHolder: ConceptHolder, wordContext: WordContext, episodicConcept: EpisodicConcept? = null, markAsInside: Boolean = true) {
        if (sourceReference.isResolved()) return

        sourceReference.resolve(valueHolder, wordContext, episodicConcept, markAsInside)
        dependentReferences.forEach { it.resolve(valueHolder, wordContext, episodicConcept, markAsInside) }

        if (markAsInside) {
            valueHolder.addFlag(ParserFlags.Inside)
        }
        replaceHolderIfNecessary(valueHolder, wordContext)
    }

    private fun replaceHolderIfNecessary(valueHolder: ConceptHolder, wordContext: WordContext) {
        overwriteResolvedHolder?.let {
            valueHolder.value?.shareStateFrom(it)
            // FIXME assuming object...
            valueHolder.value?.let { v ->
                wordContext.context.mostRecentObject = v
            }
        }
    }
}

class VariableReference(slotName: String, variableName: String, private val expression: ConceptTransformer? = null) {
    val variableSlot = Slot(slotName, createConceptVariable(variableName))

    fun isResolved() = !variableSlot.isVariable()

    fun resolve(valueHolder: ConceptHolder, wordContext: WordContext, episodicConcept: EpisodicConcept? = null, markAsInside: Boolean = true) {
        if (isResolved()) return

        variableSlot.value = evaluateExpression(variableSlot, valueHolder.value)
        episodicConcept?.let { episodicConcept ->
            // FIXME not sure about this qa hack?
            if (!wordContext.context.qaMode) {
                wordContext.context.episodicMemory.episodicRoleCheck(episodicConcept, variableSlot)
            }
        }
    }

    private fun evaluateExpression(slot: Slot, value: Concept?): Concept? {
        if (value == null) {
            return null
        }
        return if (expression != null) expression.transform(value) else value
    }
}