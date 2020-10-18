package info.dgjones.au.qa

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.CoreFields
import info.dgjones.au.concept.matchConceptByHead
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.nlp.TextModel
import info.dgjones.au.episodic.EpisodicMemory
import info.dgjones.au.parser.TextProcessor

class QuestionProcessor(val textProcessor: TextProcessor) {
    val answerGenerator = AnswerGenerator()

    fun question(questionModel: TextModel): QuestionProcessorResult {
        // FIXME can we not share access to episodic memory (or get copy)

        val sentenceResult = textProcessor.processQuestion(questionModel.initialSentence())

        val answer = generateAnswer(sentenceResult, textProcessor.episodicMemory)
        return QuestionProcessorResult(sentenceResult, answer)
    }

    fun generateAnswer(workingConcepts: List<Concept>, epdisodicMemory: EpisodicMemory): String {
        if (workingConcepts.isNotEmpty() && workingConcepts[0].name == "WhoAnswer") {
            return generateWhoAnswer(workingConcepts[0], epdisodicMemory)
        }
        return "FIXME Do not know how to handle result"
    }

    fun generateWhoAnswer(whoQuestion: Concept, epdisodicMemory: EpisodicMemory): String {
        val specifiedCharacter = whoQuestion.value("actor")
        val event = whoQuestion.value("act")
        if (event == null) {
            return "ERROR: Could not understand question"
        }
        val humans = event.find(matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
        val whoMatches = humans.filter { it.valueName(CoreFields.INSTANCE) != specifiedCharacter?.valueName(CoreFields.INSTANCE) }
        return if (whoMatches.isNotEmpty()) answerGenerator.generateHumanList(whoMatches) else "No one"
    }
}

class QuestionProcessorResult(val sentenceResult: List<Concept>, val answer: String)