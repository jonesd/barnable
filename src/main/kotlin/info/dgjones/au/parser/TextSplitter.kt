package info.dgjones.au.parser

import info.dgjones.au.nlp.TextSentence
import info.dgjones.au.nlp.WordMorphology

/* Split up a text of words up into a list of LexicalItems.
The largest expression is selected for each split.
Each split may contain multiple options.
In case a word is not known, then an Unknown Word entry will be used.
 */
class TextSplitter(val lexicon: Lexicon) {
    fun split(text: TextSentence): TextSplitUnits {
        val words = text.elements.map { it.token }
        return split(words)
    }

    private fun split(words: List<String>): TextSplitUnits {
        var wordIndex = 0
        val split = mutableListOf<List<LexicalItem>>()
        while (wordIndex < words.size) {
            val entry = lexicon.lookupNextEntry(words.subList(wordIndex, words.size))
            val selectedEntries = if (entry.isNotEmpty()) selectByMostWords(entry) else unknownEntry(words[wordIndex])
            split.add(selectedEntries)
            wordIndex += wordsIncrement(selectedEntries)
        }
        return TextSplitUnits(split.toList())
    }

    private fun wordsIncrement(selectedEntries: List<LexicalItem>): Int =
        if (selectedEntries.isNotEmpty()) selectedEntries.first().handler.word.expression.size else 1

    private fun selectByMostWords(lexicalItems: List<LexicalItem>): List<LexicalItem> {
        if (lexicalItems.isEmpty()) {
            return listOf()
        }
        val maxExpressionSize = lexicalItems.maxOfOrNull { it.handler.word.expression.size }
        return lexicalItems.filter { it.handler.word.expression.size == maxExpressionSize }
    }

    private fun unknownEntry(word: String): List<LexicalItem> =
        listOf(LexicalItem(listOf(WordMorphology(word)), WordUnknown(word)))
}

data class TextSplitUnits(val units: List<List<LexicalItem>>)
