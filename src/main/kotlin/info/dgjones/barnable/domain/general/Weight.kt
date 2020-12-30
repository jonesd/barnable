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
 * Relative weight of an entity
 */
enum class WeightFields(override val fieldName: String): Fields {
    Weight("weight")
}

private val greaterWeight = listOf("fat", "heavy", "obese", "overweight")
private val lesserWeight =  listOf("thin", "underweight")

fun buildGeneralWeightLexicon(lexicon: Lexicon) {
    val matcher = defaultModifierTargetMatcher()
    addModifierMappings(WeightFields.Weight, ScaleConcepts.LessThanNormal.name, lesserWeight,
        matcher, lexicon)
    addModifierMappings(WeightFields.Weight, ScaleConcepts.GreaterThanNormal.name, greaterWeight,
        matcher, lexicon)
}