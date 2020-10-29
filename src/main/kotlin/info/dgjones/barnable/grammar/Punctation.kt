package info.dgjones.barnable.grammar

import info.dgjones.barnable.parser.*

fun buildGrammarPunctuationLexicon(lexicon: Lexicon) {
    lexicon.addMapping(PunctuationComma())
    lexicon.addMapping(PunctuationPeriod())
    lexicon.addMapping(PunctuationExclamation())
    lexicon.addMapping(PunctuationQuestion())
    lexicon.addMapping(PunctuationColon())
    lexicon.addMapping(PunctuationSemicolon())
}

class PunctuationComma: WordHandler(EntryWord(",", noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("NotYetImplemented: Punctuation $word.word")
        return super.build(wordContext)
    }
}

class PunctuationPeriod: WordHandler(EntryWord(".", noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("NotYetImplemented: Punctuation ${word.word}")
        return super.build(wordContext)
    }
}

class PunctuationExclamation: WordHandler(EntryWord("!", noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("NotYetImplemented: Punctuation ${word.word}")
        return super.build(wordContext)
    }
}

class PunctuationQuestion: WordHandler(EntryWord("?", noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("NotYetImplemented: Punctuation ${word.word}")
        return super.build(wordContext)
    }
}

class PunctuationColon: WordHandler(EntryWord(":", noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("NotYetImplemented: Punctuation ${word.word}")
        return super.build(wordContext)
    }
}

class PunctuationSemicolon: WordHandler(EntryWord(";", noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("NotYetImplemented: Punctuation ${word.word}")
        return super.build(wordContext)
    }
}