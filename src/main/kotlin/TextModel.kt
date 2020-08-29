class TextModelBuilder(val content: String) {
    val nlp = EditorialNLP()

    fun buildModel(): TextModel {
        val paragraphTexts = NaiveTokenizer().splitIntoParagraphs(content)
        val paragraphModels = paragraphTexts.mapNotNull {
            val sentencesText = nlp.detectSentences(it)
            val sentences = sentencesText.map { generateSentenceMappings(it) }
            if (sentences.isNotEmpty()) TextParagraph(it, sentences) else null
        }
        return TextModel(content, paragraphModels)
    }

    private fun generateSentenceMappings(sentence: String): TextSentence {
        val tokens = nlp.tokenize(sentence)
        var tags = nlp.tags(tokens);
        val entities = nlp.namedEntityRecognition(tokens)
        var lemmas = nlp.lemmas(tokens, tags)
        val chunking = nlp.chunking(tokens, tags)
        val wordElements = (0 until tokens.size).map { WordElement(tokens[it], tags[it], lemmas[it], chunking[it])}
        return TextSentence(sentence, wordElements)
    }
}

class NaiveTextModelBuilder(val content: String) {
    fun buildModel(): TextModel {
        return NaiveTokenizer().tokenizeText(content)
    }
}

data class TextModel(val content: String, val paragraphs: List<TextParagraph>)
data class TextParagraph(val content: String, val sentences: List<TextSentence>)
data class TextSentence(val text: String, val elements: List<WordElement>)

data class WordElement(val token: String, val tag: String, val lemma: String, val chunk: String) {
    val missingLemma = "O"

    fun lemmaOrToken(): String = if (lemma == missingLemma) token else lemma
}

