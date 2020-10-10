package info.dgjones.au.concept

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

//    fun valueName(slotName: String, default: String = "unknown"): String {
//        return value(slotName)?.name ?: default
//    }
//    fun valueName(slotName: SL): String? {
//        return valueName(slotName.name)
//    }
    fun value(field: Fields, value: Concept?): Concept =
        value(field.fieldName, value)

    fun value(slotName: String, value: Concept?): Concept {
        var slot = slot(slotName)
        if (slot != null) {
            slot.value = value
        } else {
            slot = Slot(slotName, value)
            // FIXME not thread safe?
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

    fun duplicateResolvedValue(): Concept? {
        // FIXME how to stop graphs?
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

    private fun isVariable() =
        name.startsWith(VARIABLE_PREFIX)

    override fun toString(): String {
        return printIndented(0)
        //return "($name ${slots.values.map{it.toString()}.joinToString(separator = "\n  ") { it }})"
    }

    fun printIndented(indent: Int = 1): String {
        val indentString = " ".repeat(indent * 2)
        val continuedString = " ".repeat((indent + 1) * 2)
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
