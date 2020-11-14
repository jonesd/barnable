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

package info.dgjones.barnable.narrative

import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.domain.general.Gender
import info.dgjones.barnable.domain.general.buildHuman
import info.dgjones.barnable.parser.*

//FIXME only tries to handle simple ConceptCompletion involving a character and action
class WordWho: WordHandler(EntryWord("who")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, "WhoAnswer") {
            expectHead("actor", headValue = "Human", direction = SearchDirection.After)
            // FIXME brittle hack for first test... perhaps this should be event matching
            expectHead("act", headValue = MopMeal.MopMeal.name, direction = SearchDirection.After)
        }.demons
}

class WhoDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {

        //FIXME not sure what to use for placeholder
        wordContext.defHolder.value = buildHuman("who", "who", Gender.Male.name)
    }
}

/*
Handler for Concept Completion retrieval heuristic used during Question/Answering phase
 */
/*
class CompletionQuestionDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null) {
            if (def.value(TimeFields.TIME) == null) {
                def.value(TimeFields.TIME.fieldName, Concept(TimeConcepts.Past.name))
                active = false
            }
        }
    }

    override fun description(): String {
        return "Suffix ED marks word sense as in the past"
    }
}*/