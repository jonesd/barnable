// FIXME using Gson serialization for toString...
import com.google.gson.Gson

class TextProcessor(val textModel: TextModel, val lexicon: Lexicon) {
    val workingMemory = WorkingMemory()
    var agenda = Agenda()

    fun runProcessor(): WorkingMemory {
        textModel.sentences.forEach { processSentence(it) }
        return workingMemory
    }

    fun processSentence(sentence: TextSentence) {
        agenda = Agenda()
        val context = SentenceContext(sentence)
        sentence.elements.forEachIndexed { index, element ->
            var word = element.lemmaOrToken()
            val wordHandler = lexicon.findWordHandler(word) ?: WordIgnore(word)
            context.pushWord(WordContext(wordHandler, element))

            runWord(index, word, wordHandler)
            runDemons(context)
        }
        endSentence(context)
    }

    private fun endSentence(context: SentenceContext) {
        val defs = context.wordContexts.mapNotNull { it.wordHandler.def }
            .filter { !it.flags.contains(ParserFlags.Internal) && !it.flags.contains(ParserFlags.Ignore) }
        println("Sentence Result:")
        defs.forEach { println(it)
        }
        workingMemory.concepts.addAll(defs)
    }

    private fun runWord(index: Int, word: String, wordHandler: WordHandler) {
        println("word=$word")
        val wordDemons = wordHandler.build()
        wordDemons.forEach { agenda.withDemon(index, it) }
        println("con = ${wordHandler.def}")
    }

    // Run each active demon. Repeat again if any were fired
    fun runDemons(context: SentenceContext) {
        do {
            var fired = false
            agenda.activeDemons().forEach {
                it.run(context)
                if (!it.active) {
                    println("Killed demon=$it")
                    fired = true
                }
            }
        } while (fired)
    }

    fun findWordHandler(word: String): WordHandler? {
        return lexicon.findWordHandler(word)
    }
}

class WordContext(val wordHandler: WordHandler, val wordElement: WordElement)

class SentenceContext(val sentence: TextSentence) {
    var currentWord: String = ""
    // FIXME var currentDemon: Demon? = null
    var mostRecentObject: Concept? = null
    var mostRecentCharacter: Human? = null
    var localCharacter: Human? = null
    // FIXME val workingMemory: WorkingMemory
    val wordContexts = mutableListOf<WordContext>()

    fun pushWord(wordContext: WordContext) {
        wordContexts.add(wordContext)
        currentWord = wordContext.wordHandler.word
    }

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
    val concepts = mutableListOf<Concept>()

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

    fun activeDemons(): List<Demon> {
        return demons.flatMap { it.value }.filter { it.active }
    }
}

class Lexicon() {
    val wordMappings: MutableMap<String, WordHandler> = mutableMapOf()

    fun addMapping(handler: WordHandler) {
        wordMappings.put(handler.word, handler)
    }

    fun findWordHandler(word: String): WordHandler? {
        return wordMappings[word]
    }
}


open class WordHandler(val word: String) {
    var def: Concept? = null

    open fun build(): List<Demon> {
        return listOf()
    }
}

open class Demon(val wordHandler: WordHandler) {
    var active = true

    open fun run(context: SentenceContext) {
        // Without an implementation - just deactivate
        active = false
    }

    // Runtime comment to understand processing
    open fun comment(): DemonComment {
        return DemonComment("demon: $this", "")
    }
}

class DemonComment(val test: String, val act: String)

open class Concept(val name: String) {
    val flags = mutableListOf<ParserFlags>()

    override fun toString(): String {
        return Gson().toJson(this)
    }
}

enum class ParserFlags() {
    Internal, // Concept has been nested in another object
    Ignore // Concept has been processed and can be ignored
}

class WordIgnore(word: String): WordHandler(word) {
    override fun build(): List<Demon> {
        return listOf(IgnoreDemon(this))
    }
}

class IgnoreDemon(wordHandler: WordHandler): Demon(wordHandler) {
    fun run(wordHandler: WordHandler,  working: WorkingMemory, context: SentenceContext) {
        wordHandler.def?.flags?.add(ParserFlags.Ignore)
        active = false
    }
}