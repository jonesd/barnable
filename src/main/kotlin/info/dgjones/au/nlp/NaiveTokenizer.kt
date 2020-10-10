package info.dgjones.au.nlp

class NaiveTokenizer {
    private val abbreviations = setOf("dr", "mr", "mrs",
    "ave", "blvd", "cyn", "dr", "ln", "rd", "st",
    "no", "tel", "temp", "vet", "vs", "misc", "min", "max", "est", "dept",
    "apt", "appt", "approx")

    private val endOfLineMarkers = setOf('.', '!', '?')
    private val endOfClauseMarkers = setOf(',', ';', ':')

    fun tokenizeText(documentText: String): TextModel {
        val paragraphsText = splitIntoParagraphs(documentText)
        val paragraphs = paragraphsText.map { tokenizeParagraph(it) }
        return TextModel(documentText, paragraphs)
    }

    fun tokenizeParagraph(paragraphText: String): TextParagraph {
        return splitTextIntoWordElements(paragraphText)
    }

    private fun splitTextIntoWordElements(paragraphText: String): TextParagraph {
        val words = splitTextIntoWords(paragraphText)
        val wordsBySentence = mutableListOf(mutableListOf<String>())
        words.forEach {
            wordsBySentence.last().add(it)
            if (isWordMarksEndOfSentence(it)) {
                wordsBySentence.add(mutableListOf())
            }
        }
        val sentenceModels = wordsBySentence.mapNotNull { if (it.size > 0) createTextSentence(it) else null }
        return TextParagraph(paragraphText, sentenceModels)
    }

    private fun createTextSentence(words: MutableList<String>): TextSentence {
        val text = words.joinToString(" ")
        return TextSentence(text, words.map { WordElement(it, "", it /*stemmer.stemWord(it)*/, "") })
    }

    private fun isWordMarksEndOfSentence(it: String): Boolean {
        return it.length == 1 && endOfLineMarkers.contains(it.first())
    }

    fun splitTextIntoWords(paragraphText: String): List<String> {
        val units = paragraphText.split("""(?U)\s+""".toRegex())
        val words = mutableListOf<String>()
        units.filter { it.isNotEmpty() }.forEach {
            if (endOfClauseMarkers.contains(it.last())) {
                words.add(it.dropLast(1))
                words.add(it.last().toString())
            } else {
                words.add(it)
            }
        }
        val words2 = mutableListOf<String>()
        words.forEach {
            if (it.endsWith(".") && isAbbreviation(it)) {
                words2.add(it)
            } else if (endOfLineMarkers.contains(it.last())) {
                words2.add(it.dropLast(1))
                words2.add(it.last().toString())
            } else {
                words2.add(it)
            }
        }
        return words2.map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun isAbbreviation(unit: String): Boolean {
        val unitWithoutPeriod = unit.substringBeforeLast(".").toLowerCase()
        return abbreviations.contains(unitWithoutPeriod)
    }

    // Splits text into paragraphs for the simple case of end-of-line markers indicating
    // a paragraph break.
    private fun splitIntoParagraphs(text: String): List<String> {
        return text.lines().map { it.trim() }.filter { it.isNotBlank() }
    }
}