package org.dgjones.au.parser

import org.dgjones.au.narrative.Acts
import org.dgjones.au.nlp.*

class TextProcessor(val textModel: TextModel, val lexicon: Lexicon) {
    val workingMemory = WorkingMemory()
    var agenda = Agenda()
    var qaMode = false
    var qaMemory = WorkingMemory()

    fun runProcessor(): WorkingMemory {

        textModel.paragraphs.forEach { processParagraph(it) }
        return workingMemory
    }

    fun processParagraph(paragraphModel: TextParagraph) {
        paragraphModel.sentences.forEach { processSentence(it) }
    }

    fun processSentence(sentence: TextSentence) {
        agenda = Agenda()
        val context = SentenceContext(sentence, workingMemory, qaMode)
        println(sentence.text.toUpperCase())
        var index = 0
        do {
            val element = sentence.elements[index]
            var word = element.token.toLowerCase()
            val wordHandlerWithSuffix = lexicon.findWordHandler(word) ?: WordHandlerWithSuffix(listOf(WordUnknown(word)))
            var expression = listOf(word)

            // FIXME what about expression and single word with matching first word?
            if (wordHandlerWithSuffix.wordHandlers[0].word.expression.size > 1) {
                if (doesExpressionMatch(sentence.elements, index, wordHandlerWithSuffix.wordHandlers[0].word.expression)) {
                    expression = wordHandlerWithSuffix.wordHandlers[0].word.expression
                }
            }
            word = expression.joinToString(" ")
            val wordContext = WordContext(context.wordContexts.size, element, word, workingMemory.createDefHolder(), context)
            context.pushWord(wordContext)

            runWord(index, word, wordHandlerWithSuffix.wordHandlers, wordHandlerWithSuffix.suffix, wordContext)
            runDemons(context)
            index += expression.size
        } while (index < sentence.elements.size)
        endSentence(context)
    }

    private fun doesExpressionMatch(elements: List<WordElement>, baseIndex: Int, expression: List<String>): Boolean {
        var matched = true
        expression.forEachIndexed { index, s ->
            if (!elements[baseIndex + index].token.toLowerCase().equals(s)) {
                matched = false
            }
        }
        return matched
    }

    fun processQuestion(sentence: TextSentence): String {
        qaMode = true
        qaMemory = WorkingMemory()
        try {
            processSentence(sentence)
        } catch (e: NoMentionOfCharacter) {
            return "No mention of a character named ${e.characterName}."
        }

        val acts = qaMemory.concepts.filter{ isActHead(it) }
        if (acts.isEmpty()) {
            return "I don't know."
        }
        //FIXME assume who - should be generated from demon/node?
        val c = acts[0]
        val closestMatch = this.findMatchingConceptFromWorking(c)
        if (closestMatch != null) {
            if (c.name == Acts.ATRANS.name) {
                if (closestMatch.name == Acts.ATRANS.name) {
                    if (c.value("actor")?.valueName("firstName") == "who") {
                        return closestMatch.value("actor")?.valueName("firstName") ?: "missing"
                    } else if (c.value("from")?.valueName("firstName") == "who") {
                        return closestMatch.value("from")?.valueName("firstName") ?:"missing"
                    } else if (c.value("to")?.valueName("firstName") == "who") {
                        return closestMatch.value("to")?.valueName("firstName") ?:  "missing"
                    }
                }
            }
        }
        return "I don't know."
    }

    fun isActHead(concept: Concept): Boolean {
        val names = Acts.values().map { it.name }
        return names.contains(concept.name)
    }

     fun findMatchingConceptFromWorking(whoAct: Concept): Concept? {
         println("finding best match for $whoAct")
         workingMemory.concepts.filter{ isActHead(it)}.forEach {
             // FIXME hack....
                 if (it.name == whoAct.name) {
                     return it
                 }
         }
         return null
     }

    private fun endSentence(context: SentenceContext) {
        val memory = if (qaMode) qaMemory else workingMemory
        promteDefsToWorkingMemory(context, memory)
    }

    private fun promteDefsToWorkingMemory(context: SentenceContext, memory: WorkingMemory) {
        val defs = context.wordContexts
            .filter { !it.defHolder.hasFlag(ParserFlags.Inside) }
            .filter { !it.defHolder.hasFlag(ParserFlags.Ignore) }
            .mapNotNull { it.def()}

        println("Sentence Result:")
        defs.forEach { println(it) }
        memory.concepts.addAll(defs)
    }

    private fun runWord(index: Int, word: String, wordHandlers: List<WordHandler>, suffix: String?, wordContext: WordContext) {
        println("")
        println("${word.toUpperCase()} ==>")

        val disambiguationHandler = DisambiguationHandler(wordContext, wordHandlers, suffix, agenda).startDisambiguations()

        println("Adding to *working-memory*")
        println("DEF.${wordContext.defHolder.instanceNumber} = ${wordContext.def()}")
        println("Current working memory")
        wordContext.context.wordContexts.forEachIndexed { index, wordContext ->
            println("--- ${wordContext.word} ==> ${wordContext.defHolder?.value}")
        }
    }

    // Run each active demon. Repeat again if any were fired
    private fun runDemons(context: SentenceContext) {
        do {
            var fired = false
            // Use most recently recreated first
            agenda.activeDemons().reversed().forEach {
                it.run()
                if (!it.active) {
                    println("Killed demon=$it")
                    fired = true
                }
            }
        } while (fired)
    }
}

/* Decide which one of the wordHandlers is going to be run. Use disambiguation demons associated with each wordHandler
 to resolve which one. The first wordHandler to resolve all of their disambiguation demons wins.
 */
class DisambiguationHandler(val wordContext: WordContext, val wordHandlers: List<WordHandler>, val suffix: String?, val agenda: Agenda) {
    var disambiguationsByWordHandler = mutableMapOf<WordHandler, MutableList<Demon>>()
    var resolvedTo: WordHandler? = null

    fun startDisambiguations() {
        wordHandlers.forEach { wordHandler ->
            val disambiguationDemons = wordHandler.disambiguationDemons(wordContext, this)
            disambiguationsByWordHandler[wordHandler] = disambiguationDemons.toMutableList()
        }
        val noDisambiguationNeeded = disambiguationsByWordHandler.keys.filter { disambiguationsByWordHandler[it]?.isEmpty() ?: true }
        if (noDisambiguationNeeded.size == 1) {
            resolveTo(noDisambiguationNeeded.first())
        } else if (noDisambiguationNeeded.size > 1) {
            // FIXME what to do? let them all work in parallel?
            println("Disambiguation failed - multiple wordHandlers do not need disambiguation $noDisambiguationNeeded")
        } else {
            disambiguationsByWordHandler.forEach { wordHandler, disambiguationDemons ->
                spawnDemons(disambiguationDemons)
            }
        }
    }

    private fun resolveTo(wordHandler: WordHandler) {
        stopExtantDisambiguationDemons()
        if (disambiguationsByWordHandler.size > 1) {
            println("Disambiguated ${wordHandler.word.word} to $wordHandler - building Demons...")
        }
        resolvedTo = wordHandler
        buildWordDemons(wordHandler)
    }

    private fun stopExtantDisambiguationDemons() {
        disambiguationsByWordHandler.values.flatten().forEach { it.deactivate() }
    }

    private fun buildWordDemons(wordHandler: WordHandler){
        val suffixDemon = if (suffix != null) buildSuffixDemon(suffix, wordContext) else null
        val wordDemons = wordHandler.build(wordContext)
        val allDemons = wordDemons.toMutableList()
        if (suffixDemon != null) {
            allDemons.add(suffixDemon)
        }
        spawnDemons(allDemons)
    }

    private fun spawnDemons(demons: List<Demon>) {
        demons.forEach { spawnDemon(wordContext.wordIndex, it) }
    }

    private fun spawnDemon(index: Int, demon: Demon) {
        agenda.withDemon(index, demon)
        println("Spawning demon: $demon")
    }

    fun disambigationMatchCompleted(demon: Demon) {
        disambiguationsByWordHandler.forEach { wordHandler, disambiguationDemons ->
            if (disambiguationDemons.contains(demon)) {
                disambiguationDemons.remove(demon)
                if (disambiguationDemons.isEmpty()) {
                    resolveTo(wordHandler)
                }
            }
        }
    }
}

data class WordContext(val wordIndex: Int, val wordElement: WordElement, val word: String, val defHolder: ConceptHolder, val context: SentenceContext) {
    var totalDemons = 0

    fun def() = defHolder.value
    fun isDefSet() = def() != null
    fun nextDemonIndex(): Int {
        val index = totalDemons
        totalDemons += 1
        return index
    }
}

class SentenceContext(val sentence: TextSentence, val workingMemory: WorkingMemory, val qaMode: Boolean = false) {
    //var currentWord: String = ""
    //var nextWord: String = ""
    //var previousWord: String = ""
    //var currentWordIndex = -1;
    // FIXME var currentDemon: Demon? = null
    var mostRecentObject: Concept? = null
    var mostRecentCharacter: Concept? = null
    var localCharacter: Concept? = null
    val wordContexts = mutableListOf<WordContext>()

    fun pushWord(wordContext: WordContext) {
        wordContexts.add(wordContext)
        //currentWord = wordContext.word
        //currentWordIndex += 1
        //previousWord = if (currentWordIndex > 0) sentence.elements[currentWordIndex - 1].token.toLowerCase() else ""
        //nextWord = if (currentWordIndex < sentence.elements.size - 1) sentence.elements[currentWordIndex + 1].token.toLowerCase() else ""
    }

    fun sentenceWordAtWordIndex(wordIndex: Int): String {
        return wordContexts[wordIndex].word
    }
    fun defHolderAtWordIndex(wordIndex: Int): ConceptHolder {
        return wordContexts[wordIndex].defHolder
    }
}

class WorkingMemory() {
    var totalConceptHolders = 0
    var totalVariables = 0
    val concepts = mutableListOf<Concept>()

    val reasoningScripts: MutableList<ReasoningScript> = mutableListOf()
    fun addReasoningScript(script: ReasoningScript) {
        reasoningScripts.add(script)
    }

    val charactersRecent = mutableListOf<Concept>()
    val characters = mutableMapOf<String, Concept>()

    fun findCharacter(firstName: String): Concept? {
        //FIXME should be fancier...
        return characters[firstName.toLowerCase()]
    }

    fun addCharacter(human: Concept) {
        characters[human.valueName("firstName", "unknown").toLowerCase()] = human
        markAsRecentCharacter(human)
    }

    fun markAsRecentCharacter(human: Concept) {
        charactersRecent.remove(human)
        charactersRecent.add(0, human)
    }

    fun createDefHolder(): ConceptHolder {
        val holder = ConceptHolder(totalConceptHolders)
        totalConceptHolders += 1
        return holder
    }

    fun nextVariableIndex(): Int {
        val index = totalVariables;
        totalVariables += 1
        return index
    }
}

open class ReasoningScript(val name: String) {

}

class NoMentionOfCharacter(val characterName: String): Exception()

class Agenda() {

    val demons = mutableListOf<Demon>()

    fun withDemon(index: Int, demon: Demon) {
        demons.add(demon)
    }

    fun activeDemons(): List<Demon> {
        return demons.filter { it.active }
    }
}

class Lexicon() {
    val wordMappings: MutableMap<String, MutableList<WordHandler>> = mutableMapOf()
    val stemmer = StansStemmer()

    fun addMapping(handler: WordHandler) {
        handler.word.entries().forEach {
            val key = it.toLowerCase()
            wordMappings.putIfAbsent(key, mutableListOf<WordHandler>())
            wordMappings[key]?.add(handler)
        }
    }

    private fun wordHandlersFor(word: String): List<WordHandler> {
        return wordMappings[word.toLowerCase()] ?: listOf<WordHandler>()
    }

    fun findWordHandler(word: String): WordHandlerWithSuffix? {
        var activeWord = word
        var wordHandlers = wordHandlersFor(activeWord)
        if (wordHandlers.isEmpty()) {
            activeWord = stemmer.stemWord(word)
            wordHandlers = wordHandlersFor(activeWord)
        }
        // FIXME hacked up stemming
        var suffix: String? = null
        if (wordHandlers.isEmpty()) {
            if (word.endsWith("ed", ignoreCase = true)) {
                activeWord= word.substring(0, word.length - 2)
                suffix = "ed"
                wordHandlers = wordHandlersFor(activeWord)
            } else if (wordHandlers.isEmpty() && word.endsWith("s", ignoreCase = true)) {
                activeWord = word.substring(0, word.length - 1)
                suffix = "s"
                wordHandlers = wordHandlersFor(activeWord)
            }
        }
        println("word = $word ==> $activeWord = wordHandlers")
        return if (wordHandlers.isNotEmpty()) {
            WordHandlerWithSuffix(wordHandlers, suffix)
        } else {
            null
        }
    }
}

open class WordHandler(val word: EntryWord) {
    open fun build(wordContext: WordContext): List<Demon> {
        return listOf()
    }
    open fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf()
    }
}

data class WordHandlerWithSuffix(val wordHandlers: List<WordHandler>, val suffix: String? = null)

open class EntryWord(val word: String, val expression: List<String> = listOf(word)) {
    val pastWords = mutableListOf<String>()
    val extras = mutableListOf<String>()

    fun entries() = listOf(listOf(word), pastWords, extras).flatten()

    fun past(word: String): EntryWord {
        pastWords.add(word)
        return this
    }
    fun and(word: String): EntryWord {
        extras.add(word)
        return this
    }
}

open class Demon(val wordContext: WordContext) {
    val demonIndex = wordContext.nextDemonIndex()
    var active = true
    var children = mutableListOf<Demon>()

    open fun run() {
        // Without an implementation - just deactivate
        active = false
    }

    /* Stop the Demon from being scheduled to run again.
    Will not stop a currently running demon */
    fun deactivate() {
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
        return "{demon${wordContext.defHolder.instanceNumber}/${demonIndex}=${description()}, active=$active}"
    }

    open fun description(): String {
        return ""
    }
}

class DemonComment(val test: String, val act: String)

data class ConceptHolder(val instanceNumber: Int, var value: Concept? = null) {
    val flags = mutableListOf<ParserFlags>()

    fun addFlag(flag: ParserFlags) {
        flags.add(flag)
    }

    fun hasFlag(flag: ParserFlags): Boolean {
        return flags.contains(flag)
    }
}

data class Concept(val name: String) {
    private val slots = mutableMapOf<String, Slot>()

    fun value(slotName: String): Concept? {
        return slot(slotName)?.value
    }
    fun value(slotName: SL): Concept? {
        return value(slotName.name)
    }
    fun valueName(slotName: String): String? {
        return value(slotName)?.name
    }
    fun valueName(slotName: String, default: String = "unknown"): String {
        return value(slotName)?.name ?: default
    }
    fun valueName(slotName: SL): String? {
        return valueName(slotName.name)
    }
    fun value(slotName: String, value: Concept?): Concept {
        var slot = slot(slotName)
        if (slot != null) {
            slot.value = value
        } else {
            slot = Slot(slotName, value)
            // FIXME not thread safe?
            with(slot)
        }
        return this
    }

    fun slot(name:String): Slot? {
        return slots[name]
    }

    fun with(slot: Slot): Concept {
        // FIXME what if slot is already present?
        slots[slot.name] = slot
        return this
    }

    override fun toString(): String {
        return printIndented(0)
        return "($name ${slots.values.map{it.toString()}.joinToString(separator = "\n  ") { it }})"
    }

    fun printIndented(indent: Int = 1): String {
        val indentString = " ".repeat(indent * 2)
        val continuedString = " ".repeat((indent + 1) * 2)
        return "($name ${slots.values.map{it.printIndented(indent + 1)}.joinToString(separator = "\n$indentString") { it }})"
    }
}

class Slot(val name: String, var value: Concept? = null) {
    override fun toString(): String {
        return "$name $value}"
    }
    fun printIndented(indent: Int = 1): String {
        val indentString = " ".repeat(indent * 2)
        return "$indentString$name ${value?.printIndented(indent)}"
    }
}

class DemonProducer() {

}

enum class ParserFlags() {
    Inside, // Concept has been nested in another object
    Ignore // Concept has been processed and can be ignored
}

class WordIgnore(word: EntryWord): WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(IgnoreDemon(wordContext))
    }
}

class IgnoreDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        wordContext.defHolder.addFlag(ParserFlags.Ignore)
        active = false
    }

    override fun description(): String {
        return "Ignore word=${wordContext.word}"
    }
}

class WordUnknown(word: String): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(UnknownDemon(wordContext))
    }
}

class UnknownDemon(wordContext: WordContext): Demon(wordContext) {
    override fun description(): String {
        return "Unknown word=${wordContext.word}"
    }
}

class ExpectDemon(val matcher: ConceptMatcher, val direction: SearchDirection, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    var found: ConceptHolder? = null

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
        if (foundConcept?.value != null) {
            action(foundConcept)
            println("Found concept=$foundConcept for match=$matcher")
            active = false
        }
    }

    private fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing != null) {
            return existing
        }
        val defHolder = wordContext.context.defHolderAtWordIndex(index)
        val value = defHolder.value
        if (isConjunction(value)) {
            return null
        }
        if (matcher(value)) {
            return defHolder
        }
        return null
    }

    private fun isConjunction(concept: Concept?): Boolean {
        return matchConceptByHead(ParserKinds.Conjunction.name)(concept)
    }

    override fun description(): String {
        return "ExpectDemon $direction $action"
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

open class DisambiguationDemon(val disambiguationHandler: DisambiguationHandler, wordContext: WordContext): Demon(wordContext) {
    fun disambiguationCompleted() {
        disambiguationHandler.disambigationMatchCompleted(this)
        active = false
    }
}

class DisambiguateUsingWord(val word: String, val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, wordContext: WordContext, disambiguationHandler: DisambiguationHandler): DisambiguationDemon(disambiguationHandler, wordContext) {
    override fun run() {
        searchContext(matcher, matchPreviousWord = word, direction = direction, wordContext = wordContext) {
            disambiguationCompleted()
        }
    }
    override fun description(): String {
        return "DisambiguateUsingWord word=$word"
    }
}

class DisambiguateUsingMatch(val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, wordContext: WordContext, disambiguationHandler: DisambiguationHandler): DisambiguationDemon(disambiguationHandler, wordContext) {
    override fun run() {
        searchContext(matcher, direction = direction, wordContext = wordContext) {
            disambiguationCompleted()
        }
    }
    override fun description(): String {
        return "DisambiguateUsingMatch"
    }
}


