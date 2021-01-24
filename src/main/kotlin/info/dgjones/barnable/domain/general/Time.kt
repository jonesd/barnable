/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.parser.*

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
    Sunday,
    Later,
    After
}

// Word Sense

fun buildGeneralTimeLexicon(lexicon: Lexicon) {
    lexicon.addMapping(TimeWord(TimeConcepts.Yesterday, "yesterday"))
    lexicon.addMapping(TimeWord(TimeConcepts.Later, "later"))
    lexicon.addMapping(TimeWord(TimeConcepts.After, "after"))
}

private val timeSubjectMatcher = matchAny(listOf(
    matchConceptByKind(GeneralConcepts.Act.name),
    matchConceptByHeadOrGroup(MeteorologyConcept.Weather.name)
))

/*
Indicate that an action occurred in the past on the day before the current day.
 */
class TimeWord(val time: TimeConcepts, word: String): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actConcept = actHolder?.value
                    if (actConcept != null) {
                        wordContext.defHolder.value = Concept(time.name)
                        wordContext.defHolder.addFlag(ParserFlags.Inside)
                        actConcept.with(Slot(TimeFields.TIME, wordContext.def()))
                        active = false
                    }
                }
            }

            override fun description(): String {
                return "Modify Act before to be occur ${time.name}"
            }
        }
        val actDemon = ExpectDemon(timeSubjectMatcher, SearchDirection.Before, wordContext, highPriority = true) {
            demon.actHolder = it
        }

        return listOf(demon, actDemon)
    }
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
                return "Modify Act before to be occur Yesterday"
            }
        }
        val actDemon = ExpectDemon(matchConceptByKind(GeneralConcepts.Act.name), SearchDirection.Before, wordContext) {
            demon.actHolder = it
        }

        return listOf(demon, actDemon)
    }
}

class WordLater: WordHandler(EntryWord("later")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actConcept = actHolder?.value
                    if (actConcept != null) {
                        wordContext.defHolder.value = Concept(TimeConcepts.Later.name)
                        wordContext.defHolder.addFlag(ParserFlags.Inside)
                        actConcept.with(Slot(TimeFields.TIME, wordContext.def()))
                        active = false
                    }
                }
            }

            override fun description(): String {
                return "Modify Act before to be occur Later"
            }
        }
        val matcher = matchAny(listOf(
            matchConceptByKind(GeneralConcepts.Act.name),
            matchConceptByHeadOrGroup(MeteorologyConcept.Weather.name)
        ))
        val actDemon = ExpectDemon(matcher, SearchDirection.Before, wordContext) {
            demon.actHolder = it
        }

        return listOf(demon, actDemon)
    }
}