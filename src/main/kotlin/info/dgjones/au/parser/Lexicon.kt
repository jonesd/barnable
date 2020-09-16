package info.dgjones.au.parser

import info.dgjones.au.nlp.StansStemmer
import info.dgjones.au.nlp.WordMorphology
import info.dgjones.au.nlp.WordMorphologyBuilder

//interface BaseWord {
//    val word: String
//}
//
//data class SimpleWord(override val word: String): BaseWord
//data class WordSuffix(override val word: String, val root: String, val suffix: String): BaseWord


class Lexicon() {
    val wordMappings: MutableMap<String, MutableList<WordHandler>> = mutableMapOf()
    val stemmer = StansStemmer()

    val morphologicalMappings: MutableMap<String, MutableList<Pair<WordMorphology, WordHandler>>> = mutableMapOf()

    fun addMapping(handler: WordHandler) {
        addDirectMappings(handler)
        addMorphologyMappings(handler)
    }

    private fun addMorphologyMappings(handler: WordHandler) {
        // FIXME only using primary word...
        val entries = WordMorphologyBuilder(handler.word.word).build()
        entries.forEach {
            val key = it.full.toLowerCase()
            morphologicalMappings.putIfAbsent(key, mutableListOf())
            morphologicalMappings[key]?.add(Pair(it, handler))
        }
    }

    private fun addDirectMappings(handler: WordHandler) {
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

    // FIXME new implementation
    fun lookupWord(word: String): List<LexicalItem> {
        return directMatches(word) + morphologyMatches(word)
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