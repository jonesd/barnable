package info.dgjones.au.qa

import info.dgjones.au.concept.Concept
import info.dgjones.au.domain.general.humanKeyValue

class AnswerGenerator {
    fun generateHumanList(humans: List<Concept>): String {
        return humans.joinToString(" and ") { humanKeyValue(it) }
    }
}