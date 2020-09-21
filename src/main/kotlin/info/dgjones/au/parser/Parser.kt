package info.dgjones.au.parser

import info.dgjones.au.narrative.Acts
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.nlp.*

fun buildTextModel(text: String): TextModel {
    return NaiveTextModelBuilder(text).buildModel()
}

fun runTextProcess(text: String, lexicon: Lexicon = buildInDepthUnderstandingLexicon()): TextProcessor {
        val textModel = buildTextModel(text)
        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()
        return textProcessor
}

class TextProcessor(val textModel: TextModel, val lexicon: Lexicon) {
    val workingMemory = WorkingMemory()
    val episodicMemory = EpisodicMemory()
    var agenda = Agenda()
    var qaMode = false
    var qaMemory = WorkingMemory()

    fun runProcessor(): WorkingMemory {

        textModel.paragraphs.forEach { processParagraph(it) }
        return workingMemory
    }

    fun processParagraph(paragraphModel: TextParagraph) {
        paragraphModel.sentences.forEach { processSentence(it) }
        endParagraph()
    }

    private fun endParagraph() {
        println("End of Paragraph - Working Memory - Clear Characters")
        episodicMemory.dumpMemory()
        workingMemory.charactersRecent.clear()
    }

    fun processSentence(sentence: TextSentence) {
        agenda = Agenda()
        val context = SentenceContext(sentence, workingMemory, episodicMemory, qaMode)
        println(sentence.text.toUpperCase())
        val splitter = TextSplitter(lexicon)
        val wordUnits = splitter.split(sentence).units
        wordUnits.forEachIndexed { index, wordUnit ->
            val unitText = wordUnit[0].textFragment()
            val wordContext = WordContext(index, unitText, workingMemory.createDefHolder(), context)
            context.pushWord(wordContext)
            runWord(wordUnit, wordContext)
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
        promoteDefsToWorkingMemory(context, memory)
        println(memory)
        context.episodicMemory.dumpMemory()
    }

    private fun promoteDefsToWorkingMemory(context: SentenceContext, memory: WorkingMemory) {
        val defs = context.wordContexts
            .filter { !it.defHolder.hasFlag(ParserFlags.Inside) }
            .filter { !it.defHolder.hasFlag(ParserFlags.Ignore) }
            .mapNotNull { it.def()}

        println("Sentence Result:")
        defs.forEach { println(it) }
        memory.concepts.addAll(defs)
    }

    private fun runWord(lexicalItems: List<LexicalItem>, wordContext: WordContext) {
        println("")
        println("${lexicalItems[0].textFragment().toUpperCase()} ==>")

        val disambiguationHandler = DisambiguationHandler(wordContext, lexicalItems, agenda).startDisambiguations()

        println("Adding to *working-memory*")
        println("DEF.${wordContext.defHolder.instanceNumber} = ${wordContext.def()}")
        println("Current working memory")
        wordContext.context.wordContexts.forEachIndexed { index, wordContext ->
            println("--- ${wordContext.word} ==> ${wordContext.defHolder.value}")
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
class DisambiguationHandler(val wordContext: WordContext, val lexicalOptions: List<LexicalItem>, val agenda: Agenda) {
    var disambiguationsByWordHandler = mutableMapOf<LexicalItem, MutableList<Demon>>()
    var resolvedTo: LexicalItem? = null

    fun startDisambiguations() {
        lexicalOptions.forEach { lexicalItem ->
            val wordHandler = lexicalItem.handler
            val disambiguationDemons = wordHandler.disambiguationDemons(wordContext, this)
            disambiguationsByWordHandler[lexicalItem] = disambiguationDemons.toMutableList()
        }
        val noDisambiguationNeeded = disambiguationsByWordHandler.keys.filter { disambiguationsByWordHandler[it]?.isEmpty() ?: true }
        if (noDisambiguationNeeded.size == 1) {
            resolveTo(noDisambiguationNeeded.first())
        } else if (noDisambiguationNeeded.size > 1) {
            // FIXME what to do? let them all work in parallel?
            println("Disambiguation failed - multiple wordHandlers do not need disambiguation $noDisambiguationNeeded")
        } else {
            disambiguationsByWordHandler.forEach { (wordHandler, disambiguationDemons) ->
                spawnDemons(disambiguationDemons)
            }
        }
    }

    private fun resolveTo(lexicalItem: LexicalItem) {
        stopExtantDisambiguationDemons()
        if (disambiguationsByWordHandler.size > 1) {
            println("Disambiguated ${lexicalItem.textFragment()} to ${lexicalItem.handler} - building Demons...")
        }
        resolvedTo = lexicalItem
        buildWordDemons(lexicalItem)
    }

    private fun stopExtantDisambiguationDemons() {
        disambiguationsByWordHandler.values.flatten().forEach { it.deactivate() }
    }

    private fun buildWordDemons(lexicalItem: LexicalItem){
        val suffixDemons = buildSuffixDemons(lexicalItem)
        val wordDemons = lexicalItem.handler.build(wordContext)
        val allDemons = wordDemons + suffixDemons
        spawnDemons(allDemons)
    }

    // Currently build a suffix demon for each suffix of the expression
    // FIXME should we remove duplicates?
    private fun buildSuffixDemons(lexicalItem: LexicalItem) =
        lexicalItem.morphologies.map { it.suffix }.mapNotNull { buildSuffixDemon(it, wordContext) }

    private fun spawnDemons(demons: List<Demon>) {
        demons.forEach { spawnDemon(wordContext.wordIndex, it) }
    }

    private fun spawnDemon(index: Int, demon: Demon) {
        agenda.withDemon(index, demon)
        println("Spawning demon: $demon")
    }

    fun disambiguationMatchCompleted(demon: Demon) {
        disambiguationsByWordHandler.forEach { (wordHandler, disambiguationDemons) ->
            if (disambiguationDemons.contains(demon)) {
                disambiguationDemons.remove(demon)
                if (disambiguationDemons.isEmpty()) {
                    resolveTo(wordHandler)
                }
            }
        }
    }
}

data class WordContext(val wordIndex: Int, val word: String, val defHolder: ConceptHolder, val context: SentenceContext) {
    var totalDemons = 0

    fun def() = defHolder.value
    fun isDefSet() = def() != null
    fun nextDemonIndex(): Int {
        val index = totalDemons
        totalDemons += 1
        return index
    }
}

class SentenceContext(val sentence: TextSentence, val workingMemory: WorkingMemory, val episodicMemory: EpisodicMemory, val qaMode: Boolean = false) {
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

class WorkingMemory {
    var totalConceptHolders = 0
    var totalVariables = 0
    val concepts = mutableListOf<Concept>()

    val charactersRecent = mutableListOf<Concept>()
    fun findCharacterByGender(matchGender: String): Concept? {
        // Find most recent match. Rely on knowledge mechanisms
        // to correct tentative match
        // InDepth p182
        return charactersRecent.firstOrNull { it.valueName("gender") == matchGender }
    }

    fun addCharacter(human: Concept) {
        markAsRecentCharacter(human)
    }

    fun markAsRecentCharacter(human: Concept) {
        charactersRecent.remove(human)
        charactersRecent.add(0, human)
    }

    fun createDefHolder(concept: Concept? = null): ConceptHolder {
        val holder = ConceptHolder(totalConceptHolders)
        if (concept != null) {
            holder.value = concept
        }
        totalConceptHolders += 1
        return holder
    }

    fun nextVariableIndex(): Int {
        val index = totalVariables
        totalVariables += 1
        return index
    }

    override fun toString(): String {
        return "WORKING MEMORY\ncharactersRecent=$charactersRecent"
    }
}

open class ReasoningScript(val name: String)

class NoMentionOfCharacter(val characterName: String): Exception()

class Agenda {

    val demons = mutableListOf<Demon>()

    fun withDemon(index: Int, demon: Demon) {
        demons.add(demon)
    }

    fun activeDemons(): List<Demon> {
        return demons.filter { it.active }
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

enum class ParserFlags {
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
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.UnknownWord.name) {
            ignoreHolder()
            slot("word", wordContext.word)
        }
        return lexicalConcept.demons
    }
}