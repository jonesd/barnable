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
        val context = SentenceContext(sentence, workingMemory)
        sentence.elements.forEachIndexed { index, element ->
            var word = element.token
            val wordHandler = lexicon.findWordHandler(word) ?: WordUnknown(word)
            val wordContext = WordContext(index, element, word, ConceptHolder(), context)
            context.pushWord(wordContext)

            runWord(index, word, wordHandler, wordContext)
            runDemons(context)
        }
        endSentence(context)
    }

    private fun endSentence(context: SentenceContext) {
        promteDefsToWorkingMemory(context)
    }

    private fun promteDefsToWorkingMemory(context: SentenceContext) {
        val defs = context.wordContexts.mapNotNull { it.def() }
            .filter { !it.flags.contains(ParserFlags.Inside) }
            .filter { !it.flags.contains(ParserFlags.Ignore) }
        println("Sentence Result:")
        defs.forEach { println(it) }
        workingMemory.concepts.addAll(defs)
    }

    private fun runWord(index: Int, word: String, wordHandler: WordHandler, wordContext: WordContext) {
        println("---------------------------------------")
        println("Processing word: $word")
        println("---------------------------------------")
        val wordDemons = wordHandler.build(wordContext)
        wordDemons.forEach { agenda.withDemon(index, it) }
        println("con = ${wordContext.def()}")
    }

    // Run each active demon. Repeat again if any were fired
    fun runDemons(context: SentenceContext) {
        do {
            var fired = false
            agenda.activeDemons().forEach {
                println("running demon $it")
                it.run()
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

class WordContext(val wordIndex: Int, val wordElement: WordElement, val word: String, val defHolder: ConceptHolder, val context: SentenceContext) {
    fun def() = defHolder.value
    fun isDefSet() = def() != null
}

class SentenceContext(val sentence: TextSentence, val working: WorkingMemory) {
    var currentWord: String = ""
    var nextWord: String = ""
    var previousWord: String = ""
    var currentWordIndex = -1;
    // FIXME var currentDemon: Demon? = null
    var mostRecentObject: Concept? = null
    var mostRecentCharacter: Human? = null
    var localCharacter: Human? = null
    val workingMemory = working
    val wordContexts = mutableListOf<WordContext>()

    fun pushWord(wordContext: WordContext) {
        wordContexts.add(wordContext)
        currentWord = wordContext.word
        currentWordIndex += 1
        previousWord = if (currentWordIndex > 0) sentence.elements[currentWordIndex - 1].token.toLowerCase() else ""
        nextWord = if (currentWordIndex < sentence.elements.size - 1) sentence.elements[currentWordIndex + 1].token.toLowerCase() else ""
    }

    fun defAtWordIndex(wordIndex: Int): Concept? {
        return wordContexts[wordIndex].def()
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

    val demons = mutableListOf<Demon>()

    fun withDemon(index: Int, demon: Demon) {
        println("adding demon=$demon")
        demons.add(demon)
    }

    fun activeDemons(): List<Demon> {
        return demons.filter { it.active }
    }
}

class Lexicon() {
    val wordMappings: MutableMap<String, WordHandler> = mutableMapOf()

    fun addMapping(handler: WordHandler) {
        wordMappings.put(handler.word, handler)
    }

    fun findWordHandler(word: String): WordHandler? {
        return wordMappings[word.toLowerCase()]
    }
}


open class WordHandler(val word: String) {
    open fun build(wordContext: WordContext): List<Demon> {
        return listOf()
    }
}

open class Demon(val wordContext: WordContext) {
    var active = true
    var children = mutableListOf<Demon>()

    open fun run() {
        // Without an implementation - just deactivate
        active = false
    }

    // Runtime comment to understand processing
    open fun comment(): DemonComment {
        return DemonComment("demon: $this", "")
    }

    open fun addChild(child: Demon) {
        children.add(child)
    }

    override fun toString(): String {
        return "{demon=${super.toString()}, active=$active}"
    }
}

class DemonComment(val test: String, val act: String)

class ConceptHolder() {
    var value: Concept? = null
}

open class Concept(val name: String) {
    val flags = mutableListOf<ParserFlags>()
    val kinds = mutableListOf<String>()
    var prepobj: Prep? = null

    override fun toString(): String {
        return Gson().toJson(this)
    }

    fun isKind(kind: String): Boolean {
        return kinds.contains(kind)
    }

    fun addFlag(flag: ParserFlags) {
        flags.add(flag)
    }
}

enum class Preposition {
    In,
    Into,
    On
}

class Prep(val value: String): Concept("prep")

enum class ParserKinds {
    Conjunction
}

class ConjunctionAnd(): Concept("and") {
    init {
        kinds.add(ParserKinds.Conjunction.name)
    }
}

enum class ParserFlags() {
    Inside, // Concept has been nested in another object
    Ignore // Concept has been processed and can be ignored
}

class WordIgnore(word: String): WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(IgnoreDemon(wordContext))
    }
}

class IgnoreDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        wordContext.def()?.flags?.add(ParserFlags.Ignore)
        active = false
    }
}

class WordUnknown(word: String): WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(UnknownDemon(wordContext))
    }
}

class UnknownDemon(wordContext: WordContext): Demon(wordContext) {
    override fun toString(): String {
        return "Unknown word=${wordContext.word}"
    }
}

enum class SearchDirection {
    After,
    Before
}

fun matchPrepIn(preps: Collection<String>): (Concept?) -> Boolean {
    return { c -> preps.contains(c?.prepobj?.value) }
}

fun matchConceptByKind(kind: String): (Concept?) -> Boolean {
    return { c -> c != null && c.isKind(kind) }
}

fun matchConceptByKind(kinds: Collection<String>): (Concept?) -> Boolean {
    return { c -> c != null && c.kinds.any{ it in kinds }}
}

inline fun <reified T> matchConceptByClass(): (Concept?) -> Boolean {
    return { c -> c != null && c is T }
}

inline fun matchAny(matchers: List<(Concept?) -> Boolean>): (Concept?) -> Boolean {
    return { c -> matchers.any { it(c) }}
}

inline fun matchAll(matchers: List<(Concept?) -> Boolean>): (Concept?) -> Boolean {
    return { c -> matchers.all { it(c) }}
}

inline fun matchConjunction(): (Concept?) -> Boolean {
    return matchConceptByKind(ParserKinds.Conjunction.name)
}

inline fun matchNever(): (Concept?) -> Boolean {
    return { c -> false}
}

inline fun matchAlways(): (Concept?) -> Boolean {
    return { c -> true}
}

class ExpectDemon(val matcher: (Concept?) -> Boolean, val direction: SearchDirection, wordContext: WordContext, val action: (Concept) -> Unit): Demon(wordContext) {
    var found: Concept? = null

    override fun run() {
        if (direction == SearchDirection.Before) {
            (wordContext.wordIndex - 1 downTo 0).forEach {
                found = updateFrom(found, wordContext, it)
            }
        } else {
            (wordContext.wordIndex + 1 until wordContext.context.wordContexts.size).forEach {
                found = updateFrom(found, wordContext, it)
            }
        }
        val foundConcept = found
        if (foundConcept != null) {
            action(foundConcept)
            println("Found concept=$foundConcept for match=$matcher")
            active = false
        }
    }

    private fun updateFrom(existing: Concept?, wordContext: WordContext, index: Int): Concept? {
        if (existing != null) {
            return existing
        }
        var value = wordContext.context.defAtWordIndex(index)
        if (isConjunction(value)) {
            return null
        }
        if (matcher(value)) {
            return value
        }
        return null
    }

    private fun isConjunction(concept: Concept?): Boolean {
        return matchConceptByKind(ParserKinds.Conjunction.name)(concept)
    }
}

class WordAnd(): WordHandler("and") {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = ConjunctionAnd()
        return listOf(IgnoreDemon(wordContext))
    }
}

class WordIt(): WordHandler("it") {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(FindObjectReferenceDemon(wordContext))
    }
}

class FindObjectReferenceDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        if (!wordContext.isDefSet()) {
            wordContext.defHolder.value = wordContext.context.mostRecentObject
            println("updated word=${wordContext.word} to def=${wordContext.def()}")
        }
        if (wordContext.isDefSet()) {
            active = false
        }
    }
}

class PrepDemon(val matcher: (Concept?) -> Boolean, val direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, val action: (Concept) -> Unit): Demon(wordContext) {
    var found: Concept? = null

    override fun run() {
        if (direction == SearchDirection.Before) {
            (wordContext.wordIndex - 1 downTo 0).forEach {
                found = updateFrom(found, wordContext, it)
            }
        } else {
            (wordContext.wordIndex + 1 until wordContext.context.wordContexts.size).forEach {
                found = updateFrom(found, wordContext, it)
            }
        }
        val foundConcept = found
        if (foundConcept != null) {
            action(foundConcept)
            println("Prep found concept=$foundConcept for match=$matcher")
            active = false
        }
    }

    private fun updateFrom(existing: Concept?, wordContext: WordContext, index: Int): Concept? {
        if (existing != null) {
            return existing
        }
        var value = wordContext.context.defAtWordIndex(index)
        // if (isConjunction(value)) {
        //    return null
        //}
        if (matcher(value)) {
            return value
        }
        return null
    }

    // private fun isConjunction(concept: Concept?): Boolean {
    //    return matchConceptByKind(ParserKinds.Conjunction.name)(concept)
    //}
}


fun searchContext(matcher: (Concept?) -> Boolean, abortSearch: (Concept?) -> Boolean = matchNever(), direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, action: (Concept) -> Unit) {
    var index = wordContext.wordIndex
    var found: Concept? = null

    fun updateFrom(existing: Concept?, wordContext: WordContext, index: Int): Concept? {
        if (existing != null) {
            return existing
        }
        var value = wordContext.context.defAtWordIndex(index)
        if (abortSearch(value)) {
            // FIXME should not search any farther in this direction
            return null
        }
        if (matcher(value)) {
            return value
        }
        return null
    }

    if (direction == SearchDirection.Before) {
        (wordContext.wordIndex - 1 downTo 0).forEach {
            found = updateFrom(found, wordContext, it)
        }
    } else {
        (wordContext.wordIndex + 1 until wordContext.context.wordContexts.size).forEach {
            found = updateFrom(found, wordContext, it)
        }
    }
    val foundConcept = found
    if (foundConcept != null) {
        action(foundConcept)
        println("Search found concept=$foundConcept for match=$matcher")
    }
}