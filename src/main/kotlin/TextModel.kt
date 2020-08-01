class TextModelBuilder(val content: String) {
    val nlp = EditorialNLP(content)

    fun buildModel(): List<TextSentence> {
        val sentencesText = nlp.detectSentences()
        return sentencesText.map { generateSentenceMappings(it) }
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

class TextSentence(val text: String, val elements: List<WordElement>) {

}

class WordElement(val token: String, val tag: String, val lemma: String, val chunk: String)

class TextModel()