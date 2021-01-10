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

package info.dgjones.barnable.parser

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.concept.matchUnresolvedVariables
import info.dgjones.barnable.episodic.EpisodicMemory
import info.dgjones.barnable.grammar.buildSuffixDemon
import info.dgjones.barnable.domain.general.GeneralConcepts
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.nlp.*

fun buildTextModel(text: String): TextModel {
    return NaiveTextModelBuilder(text).buildModel()
}

fun runTextProcess(text: String, lexicon: Lexicon = buildInDepthUnderstandingLexicon()): TextProcessor {
    val textModel = buildTextModel(text)
    val textProcessor = TextProcessor(textModel, lexicon)
    textProcessor.runProcessor()
    return textProcessor
}

class TextProcessor(private val textModel: TextModel, val lexicon: Lexicon) {
    val workingMemory = WorkingMemory()
    val episodicMemory = EpisodicMemory()
    private var agenda = Agenda()
    private var qaMode = false
    private var qaMemory = WorkingMemory()

    fun runProcessor(): WorkingMemory {
        textModel.paragraphs.forEach { processParagraph(it) }
        return workingMemory
    }

    private fun processParagraph(paragraphModel: TextParagraph) {
        paragraphModel.sentences.forEach { processSentence(it) }
        endParagraph()
    }

    private fun endParagraph() {
        println("End of Paragraph - Working Memory - Clear Characters")
        episodicMemory.dumpMemory()
        workingMemory.charactersRecent.clear()
    }

    private fun processSentence(sentence: TextSentence) {
        agenda = Agenda()
        val context = SentenceContext(sentence, workingMemory, episodicMemory, qaMode)
        println(sentence.text.toUpperCase())
        val splitter = TextSplitter(lexicon)
        val wordUnits = splitter.split(sentence).units

        wordUnits.forEachIndexed { index, wordUnit ->
            val unitText = wordUnit[0].textFragment()
            val wordContext = WordContext(index, unitText, workingMemory.createDefHolder(), context)
            context.pushWord(wordContext)
            context.sentenceComplete = (index == wordUnits.lastIndex)
            runWord(wordUnit, wordContext)
            agenda.runDemons()
        }
        endSentence(context)
    }

    fun processQuestion(sentence: TextSentence): MutableList<Concept> {
        qaMode = true
        qaMemory = WorkingMemory()
        processSentence(sentence)
        return qaMemory.concepts
    }

    private fun endSentence(context: SentenceContext) {
        val memory = if (qaMode) qaMemory else workingMemory
        clearUnresolvedVariables(context)
        promoteDefsToWorkingMemory(context, memory)
        println(memory)
        context.episodicMemory.dumpMemory()
    }

    private fun clearUnresolvedVariables(context: SentenceContext) {
        context.wordContexts.forEach { it.defHolder.value?.replaceSlotValues(matchUnresolvedVariables(), null) }
    }

    private fun promoteDefsToWorkingMemory(context: SentenceContext, memory: WorkingMemory) {
        val defs = context.wordContexts
            .filter { !it.defHolder.hasFlag(ParserFlags.Inside) }
            .filter { !it.defHolder.hasFlag(ParserFlags.Ignore) }
            .mapNotNull { it.def() }

        println("Sentence Result:")
        defs.forEach { println(it) }
        memory.concepts.addAll(defs)
    }

    private fun runWord(lexicalItems: List<LexicalItem>, wordContext: WordContext) {
        println("")
        println("${lexicalItems[0].textFragment().toUpperCase()} ==>")
        if (lexicalItems[0].morphologies.size > 1) {
            println("  Recognized phrase: ${lexicalItems[0].handler.word.expression.joinToString(" ").toUpperCase()}")
        }
        if (lexicalItems[0].morphologies[0].suffix.isNotEmpty()) {
            println("  Recognized word: ${lexicalItems[0].handler.word.expression.joinToString(" ").toUpperCase()}")
            println("  Recognized suffix: ${lexicalItems[0].morphologies[0].suffix.toUpperCase()}")
        }

        DisambiguationHandler(wordContext, lexicalItems, agenda).startDisambiguations()

        println("Adding to *working-memory*")
        println("DEF.${wordContext.defHolder.instanceNumber} = ${wordContext.def()}")
        println("Current working memory")
        wordContext.context.wordContexts.forEachIndexed { index, wordContext ->
            println("--- ${wordContext.word} ==> ${wordContext.defHolder.value}")
        }
    }
}

data class WordContext(
    val wordIndex: Int,
    val word: String,
    val defHolder: ConceptHolder,
    val context: SentenceContext
) {
    private var totalDemons = 0

    fun previousWord() =
        if (wordIndex > 0) context.wordContexts[wordIndex - 1].word else null

    fun def() = defHolder.value
    fun isDefSet() = def() != null
    fun nextDemonIndex(): Int {
        val index = totalDemons
        totalDemons += 1
        return index
    }
}

class SentenceContext(
    val sentence: TextSentence,
    val workingMemory: WorkingMemory,
    val episodicMemory: EpisodicMemory,
    val qaMode: Boolean = false
) {
    var mostRecentObject: Concept? = null
    var mostRecentCharacter: Concept? = null
    var localCharacter: Concept? = null
    var sentenceComplete = false
    val wordContexts = mutableListOf<WordContext>()

    fun pushWord(wordContext: WordContext) {
        wordContexts.add(wordContext)
    }

    fun sentenceWordAtWordIndex(wordIndex: Int): String {
        return wordContexts[wordIndex].word
    }

    fun defHolderAtWordIndex(wordIndex: Int): ConceptHolder {
        return wordContexts[wordIndex].defHolder
    }
}

class WorkingMemory {
    private var totalConceptHolders = 0
    private var totalVariables = 0
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

open class WordHandler(val word: EntryWord) {
    open fun build(wordContext: WordContext): List<Demon> {
        return listOf()
    }

    open fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf()
    }

    /* Mark when handler should be used if all disambiguation demons fail */
    open fun isFallbackHandler(): Boolean = false
}

open class EntryWord(val word: String, val expression: List<String> = listOf(word), val noSuffix: Boolean = false) {
    private val pastWords = mutableListOf<String>()
    private val extras = mutableListOf<String>()

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

data class ConceptHolder(val instanceNumber: Int, var value: Concept? = null) {
    private val flags = mutableSetOf<ParserFlags>()

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

class WordIgnore(word: EntryWord) : WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(IgnoreDemon(wordContext))
    }
}

class IgnoreDemon(wordContext: WordContext) : Demon(wordContext) {
    override fun run() {
        wordContext.defHolder.addFlag(ParserFlags.Ignore)
        active = false
    }

    override fun description(): String {
        return "Ignore word=${wordContext.word}"
    }
}

class WordUnknown(word: String) : WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, GeneralConcepts.UnknownWord.name) {
            ignoreHolder()
            slot("word", wordContext.word)
        }
        return lexicalConcept.demons
    }

    override fun isFallbackHandler(): Boolean = true
}
