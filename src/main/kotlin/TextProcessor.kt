class TextProcessor(val textModel: TextModel, val lexicon: Lexicon) {
    val workingMemory = WorkingMemory()
    var agenda = Agenda()

    fun runProcessor() {
        textModel.sentences.forEach { processSentence(it) }
    }

    fun processSentence(sentence: TextSentence) {
        agenda = Agenda()
        val context = SentenceContext(sentence)
        sentence.elements.forEachIndexed { index, element ->
            var word = element.lemmaOrToken()
            println("word=$word")
            findMatchingDemons(word)?.forEach { agenda.withDemon(index, it) }
            runDemons(context)
        }
    }

    fun runDemons(context: SentenceContext) {

    }

    fun findMatchingDemons(word: String): List<Demon>? {
        return lexicon.createDemons(word)
    }
}

class SentenceContext(val sentence: TextSentence) {
    fun findConceptBefore(concepts: List<Concept>, demonPosition: Int) {
        for (i in demonPosition-1 downTo 0) {
            //FIXME if conceptMatches(concepts, sentence.elements[i])
        }
    }

    private fun conceptMatches(concepts: List<Concept>, word: WordElement): Boolean {
        // FIXME
        return true
    }
}

class WorkingMemory() {
    val reasoningScripts: MutableList<ReasoningScript> = mutableListOf()
    fun addReasoningScript(script: ReasoningScript) {
        reasoningScripts.add(script)
    }
}

open class ReasoningScript(val name: String) {

}

class Agenda() {

    val demons: MutableMap<Int, MutableList<Demon>> = mutableMapOf()

    fun withDemon(index: Int, demon: Demon) {
        val wordDemons = demons.getOrElse(index) {
            val values = mutableListOf<Demon>()
            demons.put(index, values)
            values
        }
    }
}

enum class Concept {
    Plan,
    Event,
    Human,
    Institution,
    Authority,
    Country,
    Product,
    EconomicQuantity,
    Occupation,
    Goal
}
