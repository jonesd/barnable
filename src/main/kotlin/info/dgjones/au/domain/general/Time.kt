package info.dgjones.au.domain.general

import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

enum class TimeFields(override val fieldName: String): Fields {
    TIME("time")
}
enum class TimeConcepts {
    Past,
    Yesterday,
    Tomorrow,
    Afternoon,
    Morning,
    Evening,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday
}

class WordYesterday: WordHandler(EntryWord("yesterday")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actConcept = actHolder?.value
                    if (actConcept != null) {
                        wordContext.defHolder.value = Concept(TimeConcepts.Yesterday.name)
                        wordContext.defHolder.addFlag(ParserFlags.Inside)
                        actConcept.with(Slot(TimeFields.TIME, wordContext.def()))
                        active = false
                    }
                }
            }

            override fun description(): String {
                return "Yesterday"
            }
        }
        val actDemon = ExpectDemon(matchConceptByKind(InDepthUnderstandingConcepts.Act.name), SearchDirection.Before, wordContext) {
            demon.actHolder = it
        }

        return listOf(demon, actDemon)
    }
}
