package info.dgjones.au.narrative

import info.dgjones.au.concept.Fields

enum class MealFields(override val fieldName: String): Fields {
    MEAL("MopMeal"),
    EVENT_LUNCH("EventLunch"),
    EATER_A("eater-a"),
    EATER_B("eater-b")
}