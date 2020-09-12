package org.dgjones.au.narrative

import org.dgjones.au.parser.Fields

enum class MealFields(override val fieldName: String): Fields {
    MEAL("MopMeal"),
    EVENT_LUNCH("EventLunch"),
    EATER_A("eater-a"),
    EATER_B("eater-b")
}