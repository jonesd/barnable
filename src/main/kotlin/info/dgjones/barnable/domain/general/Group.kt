package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.Fields

enum class GroupConcept {
    `*multiple*`
}

enum class GroupFields(override val fieldName: String): Fields {
    GroupInstances("group-instances")
}
