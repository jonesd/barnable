/*
 * Copyright  2021 David G Jones
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
import info.dgjones.barnable.concept.LexicalConceptBuilder
import info.dgjones.barnable.domain.general.ColourFields
import info.dgjones.barnable.domain.general.Gender
import info.dgjones.barnable.parser.*

// FIXME DGJ 2021-07-01 not sure about the name for this concept
// FIXME how does this differ from group?
enum class Determiner(val title: String) {
    Each("each"),
    Every("every"),
    All("all")
}

enum class DeterminerFields (override val fieldName: String): Fields {
    Determiner("determiner")
}

fun buildGrammarDeterminerLexicon(lexicon: Lexicon) {
    Determiner.values().forEach {
        lexicon.addMapping(ModifierWord(it.title, DeterminerFields.Determiner))
    }
}
