class TextProcessor(val textModel: TextModel, val lexicon: Lexicon) {
    val workingMemory = WorkingMemory()
    var agenda = Agenda()
    var qaMode = false
    var qaMemory = WorkingMemory()

    fun runProcessor(): WorkingMemory {
        textModel.sentences.forEach { processSentence(it) }
        return workingMemory
    }

    fun processSentence(sentence: TextSentence) {
        agenda = Agenda()
        val context = SentenceContext(sentence, workingMemory, qaMode)
        sentence.elements.forEachIndexed { index, element ->
            var word = element.token.toLowerCase()
            val wordHandler = lexicon.findWordHandler(word) ?: WordUnknown(word)
            val wordContext = WordContext(index, element, word, workingMemory.createDefHolder(), context)
            context.pushWord(wordContext)

            runWord(index, word, wordHandler, wordContext)
            runDemons(context)
        }
        endSentence(context)
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

    private fun runWord(index: Int, word: String, wordHandler: WordHandler, wordContext: WordContext) {
        println("---------------------------------------")
        println("Processing word: $word")
        println("---------------------------------------")
        val wordDemons = wordHandler.build(wordContext)
        wordDemons.forEach { agenda.withDemon(index, it) }
        println("con${wordContext.defHolder.instanceNumber} = ${wordContext.def()}")
    }

    // Run each active demon. Repeat again if any were fired
    fun runDemons(context: SentenceContext) {
        do {
            var fired = false
            // Use most recently recreated first
            agenda.activeDemons().reversed().forEach {
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
    var currentWord: String = ""
    var nextWord: String = ""
    var previousWord: String = ""
    var currentWordIndex = -1;
    // FIXME var currentDemon: Demon? = null
    var mostRecentObject: Concept? = null
    var mostRecentCharacter: Concept? = null
    var localCharacter: Concept? = null
    val wordContexts = mutableListOf<WordContext>()

    fun pushWord(wordContext: WordContext) {
        wordContexts.add(wordContext)
        currentWord = wordContext.word
        currentWordIndex += 1
        previousWord = if (currentWordIndex > 0) sentence.elements[currentWordIndex - 1].token.toLowerCase() else ""
        nextWord = if (currentWordIndex < sentence.elements.size - 1) sentence.elements[currentWordIndex + 1].token.toLowerCase() else ""
    }

    fun defHolderAtWordIndex(wordIndex: Int): ConceptHolder {
        return wordContexts[wordIndex].defHolder
    }
}

class WorkingMemory() {
    var totalConceptHolders = 0;
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
}

open class ReasoningScript(val name: String) {

}

class NoMentionOfCharacter(val characterName: String): Exception()

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
        handler.word.entries().forEach { wordMappings.put(it.toLowerCase(), handler)}
    }

    fun findWordHandler(word: String): WordHandler? {
        return wordMappings[word.toLowerCase()]
    }
}

open class WordHandler(val word: EntryWord) {
    open fun build(wordContext: WordContext): List<Demon> {
        return listOf()
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

    // Runtime comment to understand processing
    open fun comment(): DemonComment {
        return DemonComment("demon: $this", "")
    }

    open fun addChild(child: Demon) {
        children.add(child)
    }

    override fun toString(): String {
        return "{demon${wordContext.defHolder.instanceNumber}/${demonIndex}=${super.toString()}, active=$active}"
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
    private val slots = mutableMapOf<String,Slot>()

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

enum class Preposition {
    In,
    Into,
    On,
    To,
    With
}

fun withPrepObj(concept: Concept, prep: Concept) {
    concept.with(Slot(SL.PrepObject.name, prep))
}
fun buildPrep(preposition: String): Concept {
    return Concept("prep")
        .with(Slot("is", Concept(preposition)))
}

enum class ParserKinds {
    Conjunction
}

enum class Conjunction {
    And
}

fun withConjunctionObj(concept: Concept, conjunction: Concept) {
    concept.with(Slot("conjObj", conjunction))
}

fun buildConjunction(conjunction: String): Concept {
    return Concept("conjunction")
        .with(Slot("is", Concept(conjunction)))
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
}

class WordUnknown(word: String): WordHandler(EntryWord(word)) {
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

enum class SL {
    PrepObject
}

fun matchPrepIn(preps: Collection<String>): (Concept?) -> Boolean {
    return { c -> preps.contains(c?.value(SL.PrepObject)?.valueName("is")) }
}

fun matchConceptByHead(kind: String): (Concept?) -> Boolean {
    return { c -> c?.name == kind }
}

fun matchConceptByKind(kinds: Collection<String>): (Concept?) -> Boolean {
    return { c -> kinds.contains(c?.valueName("kind")) }
}

fun matchConceptByKind(kind: String): (Concept?) -> Boolean {
    return { c -> c?.valueName("kind") == kind }
}

fun matchConceptByHead(kinds: Collection<String>): (Concept?) -> Boolean {
    return { c -> kinds.contains(c?.name)}
}

inline fun matchAny(matchers: List<(Concept?) -> Boolean>): (Concept?) -> Boolean {
    return { c -> matchers.any { it(c) }}
}

inline fun matchAll(matchers: List<(Concept?) -> Boolean>): (Concept?) -> Boolean {
    return { c -> matchers.all { it(c) }}
}

inline fun matchConjunction(): (Concept?) -> Boolean {
    return matchConceptByHead(ParserKinds.Conjunction.name)
}

inline fun matchNever(): (Concept?) -> Boolean {
    return { c -> false}
}

inline fun matchAlways(): (Concept?) -> Boolean {
    return { c -> true}
}

class ExpectDemon(val matcher: (Concept?) -> Boolean, val direction: SearchDirection, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
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
}

class WordAnd(): WordHandler(EntryWord("and")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildConjunction(Conjunction.And.name)
        return listOf(IgnoreDemon(wordContext))
    }
}

class WordIt(): WordHandler(EntryWord("it")) {
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

class PrepDemon(val matcher: (Concept?) -> Boolean, val direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
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
            println("Prep found concept=$foundConcept for match=$matcher")
            active = false
        }
    }

    private fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing != null) {
            return existing
        }
        var defHolder = wordContext.context.defHolderAtWordIndex(index)
        var value = defHolder.value
        // if (isConjunction(value)) {
        //    return null
        //}
        if (matcher(value)) {
            return defHolder
        }
        return null
    }

    // private fun isConjunction(concept: Concept?): Boolean {
    //    return matchConceptByKind(ParserKinds.Conjunction.name)(concept)
    //}
}


fun searchContext(matcher: (Concept?) -> Boolean, abortSearch: (Concept?) -> Boolean = matchNever(), direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, action: (ConceptHolder) -> Unit) {
    var index = wordContext.wordIndex
    var found: ConceptHolder? = null

    fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing?.value != null) {
            return existing
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