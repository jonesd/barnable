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

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.concept.ScaleConcepts
import info.dgjones.barnable.grammar.addModifierMappings
import info.dgjones.barnable.grammar.defaultModifierTargetMatcher
import info.dgjones.barnable.grammar.stateActMatcher
import info.dgjones.barnable.parser.Lexicon

/**
 * Relative age of the entity
 */
enum class AgeFields(override val fieldName: String): Fields {
    Age("age")
}

private val greaterAge = listOf("ancient", "elderly", "old")
private val lesserAge = listOf("young")

fun buildGeneralAgeLexicon(lexicon: Lexicon) {
    val matcher = defaultModifierTargetMatcher()
    addModifierMappings(AgeFields.Age, ScaleConcepts.LessThanNormal.name, lesserAge,
        matcher, lexicon)
    addModifierMappings(AgeFields.Age, ScaleConcepts.GreaterThanNormal.name, greaterAge,
        matcher, lexicon)
}


