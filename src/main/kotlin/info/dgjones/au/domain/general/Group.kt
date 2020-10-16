package info.dgjones.au.domain.general

import info.dgjones.au.concept.Fields

enum class GroupConcept {
    `*multiple*`
}

enum class GroupFields(override val fieldName: String): Fields {
    GroupInstances("group-instances")
}
