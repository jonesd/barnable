package info.dgjones.au.parser

import info.dgjones.au.nlp.StansStemmer
import info.dgjones.au.nlp.WordMorphology
import info.dgjones.au.nlp.WordMorphologyBuilder

/* Lexicon holds all the mappings from defined words to handlers for processing them.
* Word mappings need to be registered with the lexicon as part of initializations.
* During parsing of a text, the lexicon will provide matching word handlers for the words
* at the current head of the text. Handlers primary word and versions with suffixes will be
* found.
* */
class Lexicon() {
    val wordMappings: MutableMap<String, MutableList<WordHandler>> = mutableMapOf()
    val stemmer = StansStemmer()

    val morphologicalMappings: MutableMap<String, MutableList<Pair<WordMorphology, WordHandler>>> = mutableMapOf()

    fun addMapping(handler: WordHandler) {
        addDirectMappingsForInitialWord(handler)
        addMorphologyMappingsForInitialWord(handler)
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

    fun lookupNextEntry(list: List<String>): List<LexicalItem> {
        if (list.isEmpty()) {
            return listOf()
        }
        val word = list.first()
        val firstWordMatches = lookupInitialWord(word)
        return firstWordMatches.map { populateAndMatchExpression(it, list) }.filterNotNull()
    }

    private fun populateAndMatchExpression(lexicalItem: LexicalItem, list: List<String>): LexicalItem? {
        val expressionSize = lexicalItem.handler.word.expression.size
        if (expressionSize == 1 ) {
            return lexicalItem
        }
        val remainderMorphologies = expressionMatchesRemainder(list,  lexicalItem)
        if (remainderMorphologies.size == expressionSize - 1) {
            return LexicalItem(lexicalItem.morphologies + remainderMorphologies, lexicalItem.handler)
        } else {
            return null
        }
    }

    private fun addMorphologyMappingsForInitialWord(handler: WordHandler) {
        val entries = wordMorphologies(handler.word.word)
        entries.forEach {
            val key = it.full.toLowerCase()
            morphologicalMappings.putIfAbsent(key, mutableListOf())
            morphologicalMappings[key]?.add(Pair(it, handler))
        }
    }

    private fun wordMorphologies(word: String): List<WordMorphology> =
        WordMorphologyBuilder(word).build()

    private fun addDirectMappingsForInitialWord(handler: WordHandler) {
        handler.word.entries().forEach {
            val key = it.toLowerCase()
            wordMappings.putIfAbsent(key, mutableListOf<WordHandler>())
            wordMappings[key]?.add(handler)
        }
    }

    private fun wordHandlersFor(word: String): List<WordHandler> {
        return wordMappings[word.toLowerCase()] ?: listOf<WordHandler>()
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
        return (1 until expression.size).map {
            if (it < list.size) expressionSubsequentMatch(expression[it], list[it]) else null
        }.filterNotNull()
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
    // first word of expression has matched, confirm that remaining words also match
    private fun isExpressionMatchesRemainder(list: List<String>, lexicalItem: LexicalItem): Boolean {
        val expression = lexicalItem.handler.word.expression
        return (1 until expression.size).all {
            it < list.size && isExpressionSubsequentMatch(expression[it], list[it])
        }
    }

    private fun isExpressionSubsequentMatch(expressionWord: String, sentenceWord: String):Boolean {
        return expressionWord.equals(sentenceWord, ignoreCase = true)
                || wordMorphologies(expressionWord).any { it.full.equals(sentenceWord, ignoreCase = true) }
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

data class LexicalItem(val morphologies: List<WordMorphology>, val handler: WordHandler)