package info.dgjones.au.qa

import info.dgjones.au.concept.*
import info.dgjones.au.domain.general.Human

class AnswerGenerator() {
    fun generateHumanList(humans: List<Concept>): String {
        return humans.map { humanDescription(it)}.joinToString()
    }

    fun humanDescription(human: Concept): String {
        return human.valueName(Human.FIRST_NAME) ?: human.valueName(Human.LAST_NAME) ?: human.valueName(CoreFields.INSTANCE) ?: "Human"
    }
}