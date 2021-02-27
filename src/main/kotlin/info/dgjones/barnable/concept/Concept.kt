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

data class Concept(var name: String) {
    // Use linked map to preserve addition order - allowing simple "list" order
    private var slots = linkedMapOf<String, Slot>()

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

    fun slotOrCreate(name:String): Slot {
        var slot = slots[name]
        if (slot == null) {
            slot = Slot(name, null)
            with(slot)
        }
        return slot
    }

    fun with(slot: Slot): Concept {
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
        if (matcher.matches(this)) {
            matched.add(this)
        } else {
            slots.forEach { it.value.value?.findCollect(matcher, matched)}
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Concept

        return name == other.name &&
                slots.equals(other.slots)
    }

    fun containsRooted(other: Concept): Boolean {
        if (this === other) return true

        return other.isRootedSubtreeOf(this)
    }

    fun isRootedSubtreeOf(supertree: Concept): Boolean {
        if (this === supertree) return true
        return name == supertree.name &&
            slots.all { subtreeEntry ->
                val superTreeSlot = supertree.slot(subtreeEntry.key)
                subtreeEntry.value.isRootedSubtreeOf(superTreeSlot)}
    }

    /* Replace state for this root concept with the concept argumunt, which must include this within it.
    * An attempt is made to preserve the original hierachical nature */
    fun shareStateFrom(concept: Concept) {
        concept.duplicateChildMatch(this)
        name = concept.name
        val originalSlots = slots
        slots = concept.slots
        concept.slots = originalSlots
    }

    fun duplicateChildMatch(child: Concept): Concept? {
        slots.forEach {
            val slotConcept = it.value.value
            if (slotConcept === child) {
                val duplicated = Concept(child.name)
                child.slots.forEach { childSlot ->
                    duplicated.value(childSlot.key, childSlot.value.value)
                }
                it.value.value = duplicated
                return duplicated
            } else if (slotConcept != null) {
                val duplicated = slotConcept.duplicateChildMatch(child)
                if (duplicated != null) {
                    return duplicated
                }
            }
        }
        return null
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
        return printIndented(0, mutableSetOf())
    }

    fun printIndented(indent: Int = 1, parents: MutableSet<Slot>): String {
        val indentString = " ".repeat(indent * 2)
        return "($name ${slots.values.map{it.printIndented(indent + 1, parents)}.joinToString(separator = "\n$indentString") { it }})"
    }

    fun with(child: Concept?) {
        with(Slot(slots.size.toString(), child))
    }

    fun children(): List<Concept?> {
        return slots.map { (_, slot) -> slot.value }.toList()
    }

    fun slotNames(): List<String> {
        return slots.map { (name, _) -> name }.toList()
    }
}

data class Slot(val name: String, var value: Concept? = null) {
    constructor(field: Fields, value: Concept? = null): this(field.fieldName, value)

    fun copyValue(destination: Concept) {
        value?.let { destination.value(name, value) }
    }

    fun duplicateResolvedValue(): Slot {
        val duplicatedConcept = value?.duplicateResolvedValue()
        return Slot(name, duplicatedConcept)
    }

    fun replaceSlotValues(matcher: ConceptMatcher, replace: String?) {
        // FIXME review "recursive" references
        if (matcher.matches(value)) {
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
    fun printIndented(indent: Int = 1, parents: MutableSet<Slot>): String {
        val indentString = " ".repeat(indent * 2)
        if (parents.contains(this)) {
            return "RECURSIVE $name ${value?.name}...."
        }
        parents.add(this)
        return "$indentString$name ${value?.printIndented(indent, parents)}"
    }

    fun isRootedSubtreeOf(supertree: Slot?): Boolean {
        if (this === supertree) return true
        if (supertree == null) return false
        if (name != supertree.name) return false

        val subtreeValue = value
        val supertreeValue = supertree?.value
        return if (subtreeValue != null && supertreeValue != null) {
            return subtreeValue.isRootedSubtreeOf(supertreeValue)
        } else if (subtreeValue == null && supertreeValue == null) {
            true
        } else if (subtreeValue != null && supertreeValue == null) {
            false
        } else {
            true
        }
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


