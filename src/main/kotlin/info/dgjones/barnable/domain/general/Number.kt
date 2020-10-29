package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.parser.*

enum class NumberConcept {
    Number
}

enum class NumberFields(override val fieldName: String): Fields {
    Value("value")
}

// Word Sense

//FIXME introduce more flexible approach to quantity/numbers
fun buildGeneralNumberLexicon(lexicon: Lexicon) {
    addNumber("one", 1, lexicon)
    addNumber("two", 2, lexicon)
    addNumber("three", 3, lexicon)
    addNumber("four", 4, lexicon)
    addNumber("five", 5, lexicon)
    addNumber("six", 6, lexicon)
    addNumber("seven", 7, lexicon)
    addNumber("eight", 8, lexicon)
    addNumber("nine", 9, lexicon)
    addNumber("ten", 10, lexicon)
    addNumber("eleven", 11, lexicon)
    addNumber("twelve", 12, lexicon)
    addNumber("thirteen", 13, lexicon)
    addNumber("fourteen", 14, lexicon)
    addNumber("fifteen", 15, lexicon)
    addNumber("sixteen", 16, lexicon)
    addNumber("seventeen", 17, lexicon)
    addNumber("eighteen", 18, lexicon)
    addNumber("nineteen", 19, lexicon)
    addNumber("twenty", 20, lexicon)

    // FIXME support "twenty one" etc....

    addNumber("dozen", 12, lexicon)
}

private fun addNumber(word: String, value: Int, lexicon: Lexicon) {
    lexicon.addMapping(NumberHandler(value, word))
}

class NumberHandler(var value: Int, word: String): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, NumberConcept.Number.name) {
            slot(NumberFields.Value, value.toString())
        }.demons
}
