package info.dgjones.au.concept

interface Fields {
    val fieldName: String
    fun fieldName(): String {
        return fieldName
    }
}

enum class CoreFields(override val fieldName: String): Fields {
    // Related episodic concept identity
    INSTANCE("instan");
}
