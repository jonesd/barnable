/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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