package info.dgjones.barnable.qa

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.domain.general.humanKeyValue

class AnswerGenerator {
    fun generateHumanList(humans: List<Concept>): String {
        return humans.joinToString(" and ") { humanKeyValue(it) }
    }
}