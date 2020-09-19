package info.dgjones.au.nlp

class NaiveTextModelBuilder(val content: String) {
    fun buildModel(): TextModel {
        return NaiveTokenizer().tokenizeText(content)
    }
}

data class TextModel(val content: String, val paragraphs: List<TextParagraph>) {
    fun initialSentence(): TextSentence {
        return paragraphs[0].sentences[0]
    }
}

data class TextParagraph(val content: String, val sentences: List<TextSentence>)

data class TextSentence(val text: String, val elements: List<WordElement>)

data class WordElement(val token: String, val tag: String, val lemma: String, val chunk: String) {
    val missingLemma = "O"

    fun lemmaOrToken(): String = if (lemma == missingLemma) token else lemma
}

