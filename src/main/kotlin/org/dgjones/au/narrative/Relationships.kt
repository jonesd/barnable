package org.dgjones.au.narrative

import org.dgjones.au.parser.CoreFields
import org.dgjones.au.parser.Fields

enum class Relationships(override val fieldName: String): Fields {
    Name("Relationship")
}

enum class Marriage(override val fieldName: String): Fields {
    Concept("R-Marriage"),
    Wife("wife"),
    Husband("husband")
}