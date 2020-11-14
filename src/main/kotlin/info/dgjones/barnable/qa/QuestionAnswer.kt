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

package info.dgjones.barnable.qa

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.matchConceptByHead
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.nlp.TextModel
import info.dgjones.barnable.episodic.EpisodicMemory
import info.dgjones.barnable.parser.TextProcessor

class QuestionProcessor(val textProcessor: TextProcessor) {
    private val answerGenerator = AnswerGenerator()

    fun question(questionModel: TextModel): QuestionProcessorResult {
        val sentenceResult = textProcessor.processQuestion(questionModel.initialSentence())

        val answer = generateAnswer(sentenceResult, textProcessor.episodicMemory)
        return QuestionProcessorResult(sentenceResult, answer)
    }

    private fun generateAnswer(workingConcepts: List<Concept>, episodicMemory: EpisodicMemory): String {
        if (workingConcepts.isNotEmpty() && workingConcepts[0].name == "WhoAnswer") {
            return generateWhoAnswer(workingConcepts[0], episodicMemory)
        }
        return "FIXME Do not know how to handle result"
    }

    private fun generateWhoAnswer(whoQuestion: Concept, episodicMemory: EpisodicMemory): String {
        val specifiedCharacter = whoQuestion.value("actor")
        val event = whoQuestion.value("act") ?: return "ERROR: Could not understand question"
        val humans = event.find(matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
        val whoMatches = humans.filter { it.valueName(CoreFields.Instance) != specifiedCharacter?.valueName(CoreFields.Instance) }
        return if (whoMatches.isNotEmpty()) answerGenerator.generateHumanList(whoMatches) else "No one"
    }
}

class QuestionProcessorResult(val sentenceResult: List<Concept>, val answer: String)