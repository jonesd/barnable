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

data class Concept(val name: String) {
    private val slots = mutableMapOf<String, Slot>()

    companion object {
        const val VARIABLE_PREFIX = "*VAR."
    }

    fun value(slotName: String): Concept? {
        return slot(slotName)?.value
    }
    fun value(slotName: Fields): Concept? {
        return value(slotName.fieldName)
    }
    fun valueName(slotName: String): String? {
        return value(slotName)?.name
    }
    fun valueName(slotName: Fields): String? {
        return value(slotName.fieldName)?.name
    }
    fun valueName(slotName: Fields, defaultValue: String): String {
        return value(slotName.fieldName)?.name ?: defaultValue
    }

    fun value(field: Fields, value: Concept?): Concept =
        value(field.fieldName, value)

    fun value(slotName: String, value: Concept?): Concept {
        var slot = slot(slotName)
        if (slot != null) {
            slot.value = value
        } else {
            slot = Slot(slotName, value)
            with(slot)
        }
        return this
    }

    fun slot(name:String): Slot? {
        return slots[name]
    }

    fun with(slot: Slot): Concept {
        // FIXME what if slot is already present?
        slots[slot.name] = slot
        return this
    }

    fun slots(): List<Slot> {
        return slots.values.toList()
    }

    fun find(matcher: ConceptMatcher): List<Concept> {
        val matches = mutableListOf<Concept>()
        findCollect(matcher, matches)
        return matches.toList()
    }

    private fun findCollect(matcher: ConceptMatcher, matched: MutableList<Concept>) {
        if (matcher(this)) {
            matched.add(this)
        } else {
            slots.forEach { it.value.value?.findCollect(matcher, matched)}
        }
    }

    fun replaceSlotValues(matcher: ConceptMatcher, replace: String?) {
        slots.forEach {
            it.value.replaceSlotValues(matcher, replace)
        }
    }

    // Key value for the concept
    fun selectKeyValue(vararg fieldNames: Fields) =
        selectKeyValue(fieldNames.map {it.fieldName()})

    fun selectKeyValue(fieldNames: List<String>) =
        fieldNames.map { valueName(it) }.firstOrNull { it != null && it.isNotBlank() } ?: name


    fun duplicateResolvedValue(): Concept? {
        if (isVariable()) {
            return null
        }
        val duplicatedConcept = Concept(name)
        slots.values.forEach { duplicatedConcept.with(it.duplicateResolvedValue()) }
        return duplicatedConcept
    }

    fun duplicateResolvedSlot(slotName: Fields): Slot {
        slot(slotName.fieldName)?.let {
            return it.duplicateResolvedValue()
        }
        return Slot(slotName)
    }

    fun isVariable() =
        name.startsWith(VARIABLE_PREFIX)

    override fun toString(): String {
        return printIndented(0)
    }

    fun printIndented(indent: Int = 1): String {
        val indentString = " ".repeat(indent * 2)
        return "($name ${slots.values.map{it.printIndented(indent + 1)}.joinToString(separator = "\n$indentString") { it }})"
    }
}

class Slot(val name: String, var value: Concept? = null) {
    constructor(field: Fields, value: Concept? = null): this(field.fieldName, value)

    fun copyValue(destination: Concept) {
        value?.let { destination.value(name, value) }
    }

    fun duplicateResolvedValue(): Slot {
        val duplicatedConcept = value?.duplicateResolvedValue()
        return Slot(name, duplicatedConcept)
    }

    fun replaceSlotValues(matcher: ConceptMatcher, replace: String?) {
        if (matcher(value)) {
            value = if (replace != null) Concept(replace) else null
        } else {
            value?.replaceSlotValues(matcher, replace)
        }
    }

    fun isVariable() =
        isConceptValueVariable(value?.name)

    override fun toString(): String {
        return "$name $value"
    }
    fun printIndented(indent: Int = 1): String {
        val indentString = " ".repeat(indent * 2)
        return "$indentString$name ${value?.printIndented(indent)}"
    }
}

fun createConceptVariable(variableName: String): Concept {
    val name = Concept.VARIABLE_PREFIX + variableName +"*"
    return Concept(name)
}

fun isConceptEmptyOrUnresolved(concept: Concept?): Boolean {
    return concept == null || isConceptValueEmptyOrUnresolved(concept.name)
}

fun isConceptResolved(concept: Concept?): Boolean = !isConceptEmptyOrUnresolved(concept)

fun isConceptValueEmptyOrUnresolved(name: String?): Boolean {
    return name == null || name.isBlank() || isConceptValueVariable(name)
}

fun isConceptValueVariable(name: String?) =
    name != null && name.startsWith(Concept.VARIABLE_PREFIX)

fun isConceptValueResolved(name: String?): Boolean = !isConceptValueEmptyOrUnresolved(name)

fun copyCompletedSlot(slotName: Fields, source: Concept, destination: Concept) {
    source.value(slotName)?.let {child ->
        if (isConceptResolved(child)) {
            destination.value(slotName, child)
        }
    }
}


