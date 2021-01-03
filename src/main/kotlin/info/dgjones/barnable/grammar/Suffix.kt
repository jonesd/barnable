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

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.Slot
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.parser.*

/* Suffix Daemons */

fun buildSuffixDemon(suffix: String, wordContext: WordContext): Demon? {
    return when (suffix) {
        "ed" -> SuffixEdDemon(wordContext)
        "s" -> PluralDemon(wordContext)
        "ing" -> SuffixIngDemon(wordContext)
        else -> null
    }
}

class SuffixEdDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        if (def != null && def.value(TimeFields.TIME) == null) {
            def.value(TimeFields.TIME.fieldName, Concept(TimeConcepts.Past.name))
            active = false
        }
    }

    override fun description(): String {
        return "Suffix ED marks word sense as in the past"
    }
}

class SuffixIngDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val def = wordContext.def()
        // Progressive Detection
        val previousWord = wordContext.previousWord()
        if (previousWord != null && formsOfBe.contains(previousWord.toLowerCase())) {
            def?.let {
                it.with(Slot(GrammarFields.Aspect, Concept(Aspect.Progressive.name)) )
                active = false
            }
        } else {
            active = false
        }
    }
    override fun description(): String {
        return "Suffix ING mark words sense as progressive when following be"
    }
}

// FIXME find a more useful location fo formsOfBe
private val formsOfBe = setOf("be", "am", "are", "is", "was", "were", "being", "been")

