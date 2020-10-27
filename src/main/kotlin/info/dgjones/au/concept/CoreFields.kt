package info.dgjones.au.concept

interface Fields {
    val fieldName: String
    fun fieldName(): String {
        return fieldName
    }
}

enum class CoreFields(override val fieldName: String): Fields {
    // Related episodic concept identity
    INSTANCE("instan"),
    Event("event"),
    Kind("kind"),
    Name("name"),
    Is("is"),
    // FIXME not sure where these should be stored
    Age("age"),
    Weight("weight")
}
