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
            val wordHandlerWithSuffix = lexicon.findWordHandler(word) ?: WordHandlerWithSuffix(WordUnknown(word))
            var expression = listOf(word)
            if (wordHandlerWithSuffix.wordHandler.word.expression.size > 1) {
                if (doesExpressionMatch(sentence.elements, index, wordHandlerWithSuffix.wordHandler.word.expression)) {
                    expression = wordHandlerWithSuffix.wordHandler.word.expression
                }
            }
            word = expression.joinToString(" ")
            val wordContext = WordContext(context.wordContexts.size, element, word, workingMemory.createDefHolder(), context)
            context.pushWord(wordContext)

            runWord(index, word, wordHandlerWithSuffix.wordHandler, wordHandlerWithSuffix.suffix, wordContext)
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

    private fun runWord(index: Int, word: String, wordHandler: WordHandler, suffix: String?, wordContext: WordContext) {
        println("")
        println("${word.toUpperCase()} ==>")
        val suffixDemon = if (suffix != null) buildSuffixDemon(suffix, wordContext) else null
        val wordDemons = wordHandler.build(wordContext)
        val allDemons = wordDemons.toMutableList()
        if (suffixDemon != null) {
            allDemons.add(suffixDemon)
        }
        val disambiguationHandler = DisambiguationHandler(wordContext, wordHandler, agenda, allDemons)
        val disambiguationDemons = wordHandler.disambiguationDemons(wordContext, disambiguationHandler)
        disambiguationHandler.startDisambiguation(disambiguationDemons)

        println("Adding to *working-memory*")
        println("DEF.${wordContext.defHolder.instanceNumber} = ${wordContext.def()}")
        println("Current working memory")
        wordContext.context.wordContexts.forEachIndexed { index, wordContext ->
            println("--- ${wordContext.word} ==> ${wordContext.defHolder?.value}")
        }
    }

    // Run each active demon. Repeat again if any were fired
    fun runDemons(context: SentenceContext) {
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

class DisambiguationHandler(val wordContext: WordContext, val wordHandler: WordHandler, val agenda: Agenda, val wordDemons: List<Demon>) {
    var outstandingDisambiguations = 0;

    fun startDisambiguation(disambigutionDemons: List<Demon>) {
        outstandingDisambiguations = disambigutionDemons.size
        if (outstandingDisambiguations == 0) {
            spawnDemons(wordDemons)
        } else {
            spawnDemons(disambigutionDemons)
        }
    }

    private fun spawnDemons(demons: List<Demon>) {
        demons.forEach { spawnDemon(wordContext.wordIndex, it) }
    }

    private fun spawnDemon(index: Int, demon: Demon) {
        agenda.withDemon(index, demon)
        println("Spawning demon: $demon")
    }

    fun result(success: Boolean) {
        if (success) {
            outstandingDisambiguations -= 1
            if (outstandingDisambiguations <= 0) {
                spawnDemons(wordDemons)
            }
        } else {
            // failed do not run wordDemons
            println("Disambiguation failure of ${wordContext.word} for $wordHandler")
        }

    }
}

class WordContext(val wordIndex: Int, val wordElement: WordElement, val word: String, val defHolder: ConceptHolder, val context: SentenceContext) {
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
    val wordMappings: MutableMap<String, WordHandler> = mutableMapOf()
    val stemmer = StansStemmer()

    fun addMapping(handler: WordHandler) {
        handler.word.entries().forEach {
            if (wordMappings.containsKey(it.toLowerCase())) {
                // FIXME support multiple entries ???
                throw IllegalStateException("mapping already exists = ${it.toLowerCase()}")
            }
            wordMappings[it.toLowerCase()] = handler
        }
    }

    private fun wordHandlerFor(word: String): WordHandler? {
        return wordMappings[word.toLowerCase()]
    }

    fun findWordHandler(word: String): WordHandlerWithSuffix? {
        var activeWord = word
        var wordHandler = wordHandlerFor(activeWord)
        if (wordHandler == null) {
            activeWord =  stemmer.stemWord(word)
            wordHandler = wordHandlerFor(activeWord)
        }
        // FIXME hacked up stemming
        var suffix: String? = null
        if (wordHandler == null) {
            if (word.endsWith("ed", ignoreCase = true)) {
                activeWord= word.substring(0, word.length - 2)
                suffix = "ed"
                wordHandler = wordHandlerFor(activeWord)
            } else if (wordHandler == null && word.endsWith("s", ignoreCase = true)) {
                activeWord = word.substring(0, word.length - 1)
                suffix = "s"
                wordHandler = wordHandlerFor(activeWord)
            }
        }
        println("word = $word ==> $activeWord = wordHandler")
        return if (wordHandler != null) {
            WordHandlerWithSuffix(wordHandler, suffix)
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

data class WordHandlerWithSuffix(val wordHandler: WordHandler, val suffix: String? = null)

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

class WordDisambiguator(val wordContext: WordContext) {
    val wordHandlers = mutableListOf<WordHandler>()

}

open class Demon(val wordContext: WordContext) {
    val demonIndex = wordContext.nextDemonIndex()
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
        return "{demon${wordContext.defHolder.instanceNumber}/${demonIndex}=${description()}, active=$active}"
    }

    open fun description(): String {
        return ""
    }
}

class DemonComment(val test: String, val act: String)

class ConceptHolder(val instanceNumber: Int) {
    var value: Concept? = null
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

enum class SearchDirection {
    After,
    Before
}

fun matchConceptByHead(kind: String): ConceptMatcher {
    return { c -> c?.name == kind }
}

fun matchConceptByKind(kinds: Collection<String>): ConceptMatcher {
    return { c -> kinds.contains(c?.valueName("kind")) }
}

fun matchConceptByKind(kind: String): ConceptMatcher {
    return { c -> c?.valueName("kind") == kind }
}

fun matchConceptByHead(kinds: Collection<String>): ConceptMatcher {
    return { c -> kinds.contains(c?.name)}
}

fun matchAny(matchers: List<ConceptMatcher>): ConceptMatcher {
    return { c -> matchers.any { it(c) }}
}

fun matchAll(matchers: List<ConceptMatcher>): ConceptMatcher {
    return { c -> matchers.all { it(c) }}
}

fun matchNever(): ConceptMatcher {
    return { c -> false}
}

fun matchAlways(): ConceptMatcher {
    return { c -> true}
}

typealias ConceptMatcher = (Concept?) -> Boolean

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

class DisambiguateUsingWord(val word: String, val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, wordContext: WordContext, val disambiguationHandler: DisambiguationHandler): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, matchPreviousWord = word, direction = direction, wordContext = wordContext) {
            disambiguationHandler.result(it != null)
            active = false
        }
    }
    override fun description(): String {
        return "DisambiguateUsingWord word=$word"
    }
}
class DisambiguateUsingMatch(val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, wordContext: WordContext, val disambiguationHandler: DisambiguationHandler): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, direction = direction, wordContext = wordContext) {
            disambiguationHandler.result(it != null)
            active = false
        }
    }
    override fun description(): String {
        return "DisambiguateUsingMatch"
    }
}

fun searchContext(matcher: ConceptMatcher, abortSearch: ConceptMatcher = matchNever(), matchPreviousWord: String? = null, direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, action: (ConceptHolder) -> Unit) {
    var index = wordContext.wordIndex
    var found: ConceptHolder? = null

    fun isMatchWithSentenceWord(index: Int): Boolean {
        return (matchPreviousWord != null && index >= 0 && wordContext.context.sentenceWordAtWordIndex(index) == matchPreviousWord)
    }
    fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing?.value != null) {
            return existing
        }
        if (matchPreviousWord != null && !isMatchWithSentenceWord(index - 1)) {
            // failed to include match on provies sentence word
            return null
        }
        val defHolder = wordContext.context.defHolderAtWordIndex(index)
        var value = defHolder.value
        if (abortSearch(value)) {
            // FIXME should not search any farther in this direction
            return null
        }
        if (matcher(value)) {
            return defHolder
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
    if (foundConcept?.value != null) {
        action(foundConcept)
        println("Search found concept=$foundConcept for match=$matcher")
    }
}
