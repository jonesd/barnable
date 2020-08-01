import opennlp.tools.chunker.ChunkerME
import opennlp.tools.chunker.ChunkerModel
import opennlp.tools.lemmatizer.DictionaryLemmatizer
import opennlp.tools.namefind.NameFinderME
import opennlp.tools.namefind.TokenNameFinderModel
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import opennlp.tools.util.Span
import java.io.File
import java.io.InputStream
import java.util.*


/* Apache OpenNLP */

class EditorialNLP(val text: String) {
    val modelPath = "~/code/opennlp/models-1.5"

    fun detectSentences(): Array<String> {
        val sentenceModelStream = modelFile("en-sent.bin");
        val model = SentenceModel(sentenceModelStream)
        val sdetector = SentenceDetectorME(model)
        val sentences = sdetector.sentDetect(text)
        print(sentences)
        return sentences;
    }

    fun tokenize(sentence: String): Array<String> {
        val tokenizeModelStream = modelFile("en-token.bin");
        val model = TokenizerModel(tokenizeModelStream)
        val tokenizer = TokenizerME(model)
        val tokens = tokenizer.tokenize(sentence)
        print(tokens)
        return tokens
    }

    fun namedEntityRecognition(text: String): List<Array<Span>> {
        val tokens = tokenize(text)
        return namedEntityRecognition(tokens)
    }

    fun namedEntityRecognition(tokens: Array<String>): List<Array<Span>> {
        val inputStreamNameFinder = modelFile("en-ner-person.bin").inputStream()
        val model = TokenNameFinderModel(inputStreamNameFinder)
        val nameFinderME = NameFinderME(model)
        val spans = Arrays.asList(nameFinderME.find(tokens))
        return spans
    }

    fun lemmas(text: String): Array<String> {
        val tokens = tokenize(text)
        val tags = tags(tokens)

        return lemmas(tokens, tags)
    }

    fun lemmas(tokens: Array<String>, tags: Array<String>): Array<String> {
        val dictLemmatizer = modelFile("en-lemmatizer.dict")
        val lemmatizer = DictionaryLemmatizer(
            dictLemmatizer
        )
        val lemmas = lemmatizer.lemmatize(tokens, tags)
        return lemmas
    }

    fun tags(tokens: Array<String>): Array<String> {
        val inputStreamPOSTagger = modelFile("en-pos-maxent.bin")
        val posModel = POSModel(inputStreamPOSTagger)
        val posTagger = POSTaggerME(posModel)
        val tags = posTagger.tag(tokens)
        return tags
    }

    fun chunking(sentence: String): Array<String> {
        val tokens = tokenize(sentence)
        val tags = tags(tokens)

        return chunking(tokens, tags)
    }

    fun chunking(
        tokens: Array<String>,
        tags: Array<String>
    ): Array<String> {
        val inputStreamChunker = modelFile("en-chunker.bin").inputStream()
        val chunkerModel = ChunkerModel(inputStreamChunker)
        val chunker = ChunkerME(chunkerModel)
        val chunks = chunker.chunk(tokens, tags)
        return chunks
    }

    private fun modelFile(filename: String): File {
        var path = if (modelPath.startsWith("~" + File.separator)) System.getProperty("user.home") + modelPath.substring(1) else modelPath
        return File(path, filename)
    }
}