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

import info.dgjones.barnable.concept.ConceptMatcher
import info.dgjones.barnable.grammar.buildSuffixDemon

/*
Disambiguation is the process of deciding which word sense is selected when a word/expression has two or more meanings in
the Lexicon. There are two ways that can be accomplished:

- Top-Down involves demons NOT belonging to the word expecting a specific sense, and the first one to match a meaning
will trigger that specific word sense to be selected.
- Bottom-Up involves disambiguation demons associated with the word sense searching the current context to decide which
sense is appropriate.

See: InDepth p180
 */
open class DisambiguationDemon(private val disambiguationHandler: DisambiguationHandler, highPriority: Boolean = false, wordContext: WordContext): Demon(wordContext, highPriority) {
    fun disambiguationCompleted() {
        disambiguationHandler.disambiguationMatchCompleted(this)
        active = false
    }
    fun disambiguationFailed() {
        disambiguationHandler.disambiguationMatchFailed(this)
        active = false
    }
}

class DisambiguateUsingWord(val word: String, val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, highPriority: Boolean = false, wordContext: WordContext, disambiguationHandler: DisambiguationHandler): DisambiguationDemon(disambiguationHandler, highPriority, wordContext) {
    override fun run() {
        searchContext(matcher, matchPreviousWord = word, direction = direction, wordContext = wordContext) {
            disambiguationCompleted()
        }
    }
    override fun description(): String {
        return "DisambiguateUsingWord word=$word"
    }
}

class DisambiguateUsingMatch(val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, private val distance: Int? = null, highPriority: Boolean = false, wordContext: WordContext, disambiguationHandler: DisambiguationHandler): DisambiguationDemon(disambiguationHandler, highPriority, wordContext) {
    override fun run() {
        searchContext(matcher, direction = direction, distance = distance, wordContext = wordContext) {
            disambiguationCompleted()
        }
    }
    override fun description(): String {
        return "DisambiguateUsingMatch"
    }
}

class DisambiguateUsingSentenceWordRegex(private val regex: Regex, highPriority: Boolean = false, wordContext: WordContext, disambiguationHandler: DisambiguationHandler): DisambiguationDemon(disambiguationHandler, highPriority, wordContext) {
    override fun run() {
        if (wordContext.word.matches(regex)) {
            disambiguationCompleted()
        } else {
            disambiguationFailed()
        }
    }
    override fun description(): String {
        return "DisambiguateUsingWordRegex word=$wordContext.word regex=$regex"
    }
}


/* Decide which one of the wordHandlers is going to be run. Use disambiguation demons associated with each wordHandler
 to resolve which one. The first wordHandler to resolve all of their disambiguation demons wins.
 If all lexical options fail, then apply the fallbackOption when present.
 */
class DisambiguationHandler(val wordContext: WordContext, private val lexicalOptions: List<LexicalItem>, private val agenda: Agenda) {
    private var disambiguationsByWordHandler = mutableMapOf<LexicalItem, MutableList<Demon>>()
    private var resolvedTo: LexicalItem? = null
    private val fallbackLexicalOption: LexicalItem? = lexicalOptions.firstOrNull { it.handler.isFallbackHandler() }

    fun startDisambiguations() {
        lexicalOptions.forEach { lexicalItem ->
            val wordHandler = lexicalItem.handler
            val disambiguationDemons = wordHandler.disambiguationDemons(wordContext, this)
            disambiguationsByWordHandler[lexicalItem] = disambiguationDemons.toMutableList()
        }
        val noDisambiguationNeeded = disambiguationsByWordHandler.keys.filter { disambiguationsByWordHandler[it]?.isEmpty() ?: true }
        when {
            noDisambiguationNeeded.size == 1  && fallbackLexicalOption == null -> {
                resolveTo(noDisambiguationNeeded.first())
            }
            noDisambiguationNeeded.size > 1 -> {
                println("ERROR Disambiguation failed - multiple wordHandlers do not need disambiguation $noDisambiguationNeeded for ${wordContext.word}")
            }
            else -> {
                spawnDisambiguationDemons()
            }
        }
    }

    private fun spawnDisambiguationDemons() {
        disambiguationsByWordHandler.forEach { (wordHandler, disambiguationDemons) ->
            spawnDemons(disambiguationDemons)
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
    private fun buildSuffixDemons(lexicalItem: LexicalItem) =
        lexicalItem.morphologies.map { it.suffix }.mapNotNull { buildSuffixDemon(it, wordContext) }

    private fun spawnDemons(demons: List<Demon>) {
        demons.forEach { spawnDemon(wordContext.wordIndex, it) }
    }

    private fun spawnDemon(index: Int, demon: Demon) {
        agenda.withDemon(index, demon)
        println("Spawning demon: $demon")
    }

    /**
     * Once all demons associated with a word handler are completed/met then resolve disambigation for this word handler
     */
    fun disambiguationMatchCompleted(demon: Demon) {
        if (resolvedTo != null) {
            println("Ignored disambiguation complete for ${demon} as already complete")
            return
        }
        disambiguationsByWordHandler.keys.firstOrNull() { disambiguationsByWordHandler[it]?.contains(demon)?:false}?.let { lexicalItem ->
            disambiguationsByWordHandler[lexicalItem]?.let { itemDemons ->
                demon.deactivate()
                itemDemons.remove(demon)
                if (itemDemons.isEmpty()) {
                    resolveTo(lexicalItem)
                }
            }
        }
    }
    fun disambiguationMatchFailed(demon: Demon) {
        if (resolvedTo != null) {
            println("Ignored disambiguation complete for ${demon} as already complete")
            return
        }
        disambiguationsByWordHandler.keys.firstOrNull() { disambiguationsByWordHandler[it]?.contains(demon)?:false}?.let { lexicalItem ->
            disambiguationsByWordHandler[lexicalItem]?.let { itemDemons ->
                itemDemons.forEach { it.deactivate()}
                itemDemons.remove(demon)
                if (itemDemons.isEmpty()) {
                    if (fallbackLexicalOption != null) {
                        resolveTo(fallbackLexicalOption)
                    } else {
                        println("No fallbackLexicalOption as last disambiguation failed")
                    }
                }
            }
        }
    }
}