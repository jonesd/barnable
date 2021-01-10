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

import info.dgjones.barnable.nlp.WordMorphology
import info.dgjones.barnable.nlp.WordMorphologyBuilder

/**
 * Lexicon holds all the mappings from defined words to handlers for processing them.
 * Word mappings need to be registered with the lexicon as part of initializations.
 * During parsing of a text, the lexicon will provide matching word handlers for the words
 * at the current head of the text. Handlers primary word and versions with suffixes will be
 * found.
 *
 * Exceptional handlers can be registered for "unregistered" words. Typically this is used to handle
 * multitude of cases with one handler, for example all numbers expressed with digits, or
 * telephone numbers, or when a separate datastore of words is to be looked up such as cities in the world.
 * The standard word disambiguation mechanism should be used to support many handlers of unregistered words.
 **/
class Lexicon {
    private val wordMappings: MutableMap<String, MutableList<WordHandler>> = mutableMapOf()
    private val morphologicalMappings: MutableMap<String, MutableList<Pair<WordMorphology, WordHandler>>> = mutableMapOf()
    private val unknownHandlers: MutableList<WordHandler> = mutableListOf()

    fun addMapping(handler: WordHandler) {
        addDirectMappingsForInitialWord(handler)
        addMorphologyMappingsForInitialWord(handler)
    }

    fun addUnknownHandler(handler: WordHandler) {
        unknownHandlers.add(handler)
    }

    // Return all matches from the lexicon for the word, either exact or with suffixes.
    // Expressions may be included, however, there are not necessarily matches for the remainder of the expression
    fun lookupInitialWord(word: String): List<LexicalItem> {
        return directMatches(word) + morphologyMatches(word)
    }

    // Return all matches from the lexicon for the word, either exact or with suffixes.
    // Expressions are NOT included in the resulting options
    fun lookupOnlySingleWords(word: String): List<LexicalItem> {
        return lookupInitialWord(word).filter { it.handler.word.expression.size == 1}
    }

    fun lookupNextEntry(wordStream: List<String>): List<LexicalItem> {
        if (wordStream.isEmpty()) {
            return listOf()
        }
        val word = wordStream.first()
        val firstWordMatches = lookupInitialWord(word)
        return if (firstWordMatches.isNotEmpty()) {
            firstWordMatches.mapNotNull { populateAndMatchExpression(it, wordStream) }
        } else {
            unregisteredHandlersForEntry(word)
        }
    }

    private fun populateAndMatchExpression(lexicalItem: LexicalItem, list: List<String>): LexicalItem? {
        val expressionSize = lexicalItem.handler.word.expression.size
        if (expressionSize == 1 ) {
            return lexicalItem
        }
        val remainderMorphologies = expressionMatchesRemainder(list,  lexicalItem)
        return if (remainderMorphologies.size == expressionSize - 1) {
            LexicalItem(lexicalItem.morphologies + remainderMorphologies, lexicalItem.handler)
        } else {
            null
        }
    }

    private fun addMorphologyMappingsForInitialWord(handler: WordHandler) {
        if (handler.word.noSuffix) {
            return
        }
        val entries = wordMorphologies(handler.word.word)
        entries.forEach {
            val key = it.full.toLowerCase()
            morphologicalMappings.putIfAbsent(key, mutableListOf())
            morphologicalMappings[key]?.add(Pair(it, handler))
        }
    }

    private fun unregisteredHandlersForEntry(word: String): List<LexicalItem> {
        val lexicalItems = unknownHandlers.map { LexicalItem(listOf(WordMorphology(word)), it) }.toMutableList()
        lexicalItems += LexicalItem(listOf(WordMorphology(word)), WordUnknown(word))
        return lexicalItems
    }

    private fun wordMorphologies(word: String): List<WordMorphology> =
        WordMorphologyBuilder(word).build()

    private fun addDirectMappingsForInitialWord(handler: WordHandler) {
        handler.word.entries().forEach {
            val key = it.toLowerCase()
            wordMappings.putIfAbsent(key, mutableListOf())
            wordMappings[key]?.add(handler)
        }
    }

    private fun wordHandlersFor(word: String): List<WordHandler> {
        return wordMappings[word.toLowerCase()] ?: listOf()
    }

    private fun directMatches(word: String): List<LexicalItem> {
        return wordHandlersFor(word).map { LexicalItem(listOf(WordMorphology(word)), it) }
    }

    private fun morphologyMatches(word: String): List<LexicalItem> {
        val morphologyMatches = morphologicalMappings[word.toLowerCase()] ?: listOf()
        return morphologyMatches.map { LexicalItem(listOf(it.first), it.second) }
    }

    private fun expressionMatchesRemainder(list: List<String>, lexicalItem: LexicalItem): List<WordMorphology> {
        val expression = lexicalItem.handler.word.expression
        return (1 until expression.size).mapNotNull {
            if (it < list.size) expressionSubsequentMatch(expression[it], list[it]) else null
        }
    }

    private fun expressionSubsequentMatch(expressionWord: String, sentenceWord: String):WordMorphology? {
        if (expressionWord.equals(sentenceWord, ignoreCase = true)) {
            return WordMorphology(expressionWord)
        } else {
            wordMorphologies(expressionWord).forEach {
                if (it.full.equals(sentenceWord, ignoreCase = true)) {
                    return it
                }
            }
            return null
        }
    }
}

data class LexicalItem(val morphologies: List<WordMorphology>, val handler: WordHandler) {
    fun textFragment(): String = morphologies.joinToString(separator = " ") { it.full }
}