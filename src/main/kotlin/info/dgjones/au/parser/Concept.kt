package info.dgjones.au.parser

data class Concept(val name: String) {
    private val slots = mutableMapOf<String, Slot>()

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

    override fun toString(): String {
        return printIndented(0)
        return "($name ${slots.values.map{it.toString()}.joinToString(separator = "\n  ") { it }})"
    }

    fun printIndented(indent: Int = 1): String {
        val indentString = " ".repeat(indent * 2)
        val continuedString = " ".repeat((indent + 1) * 2)
        return "($name ${slots.values.map{it.printIndented(indent + 1)}.joinToString(separator = "\n$indentString") { it }})"
    }
}

class Slot(val name: String, var value: Concept? = null) {
    constructor(field: Fields, value: Concept? = null): this(field.fieldName, value)

    override fun toString(): String {
        return "$name $value}"
    }
    fun printIndented(indent: Int = 1): String {
        val indentString = " ".repeat(indent * 2)
        return "$indentString$name ${value?.printIndented(indent)}"
    }
}
