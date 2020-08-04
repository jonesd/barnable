class TextModelBuilder(val content: String) {
    val nlp = EditorialNLP(content)

    fun buildModel(): TextModel {
        val sentencesText = nlp.detectSentences()
        val sentences = sentencesText.map { generateSentenceMappings(it) }
        return TextModel(content, sentences)
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

class WordElement(val token: String, val tag: String, val lemma: String, val chunk: String) {
    val missingLemma = "O"

    fun lemmaOrToken(): String = if (lemma == missingLemma) token else lemma

}

class TextModel(val content: String, val sentences: List<TextSentence>)