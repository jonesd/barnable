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

import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.parser.*

enum class GrammarFields(override val fieldName: String): Fields {
    Aspect("aspect"),
    Case("case"),
    Voice("voice")
}

enum class Clause {
    Boundary
}
enum class ParserKinds {
    Conjunction
}

enum class Case {
    Subjective,
    Objective,
    Possessive,
    Vocative
}

enum class Aspect {
    Progressive
}

enum class Voice {
    // https://en.wiktionary.org/wiki/active_voice#English
    // Fred kicked the ball.
    Active,

    // https://en.wiktionary.org/wiki/passive_voice#English
    // The ball was kicked by Fred
    Passive
}

/* Word senses */

fun buildEnglishGrammarLexicon(lexicon: Lexicon) {
    buildGrammarConjunctionLexicon(lexicon)
    buildGrammarModifierLexicon(lexicon)
    buildGrammarPronounLexicon(lexicon)
    buildGrammarPropositionLexicon(lexicon)
    buildGrammarPunctuationLexicon(lexicon)

    lexicon.addMapping(WordHave())
    lexicon.addMapping(WordIgnore(EntryWord("a").and("an")))
    lexicon.addMapping(WordIgnore(EntryWord("the")))
}

/*
Progressive form of have can indicate a social interaction.
eg "having lunch"
 */
class WordHave: WordHandler(EntryWord("have")) {
    override fun build(wordContext: WordContext): List<Demon> {
        println("FIXME implement have")
        return super.build(wordContext)
    }
    //FIXME InDepth pp303 - having?
}
