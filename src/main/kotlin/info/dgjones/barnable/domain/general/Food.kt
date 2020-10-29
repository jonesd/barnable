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

import info.dgjones.barnable.parser.*

/**
 * List of common foods that are stored as a kind of PhysicalObject
 */
enum class Foods(override val title: String, override val kind: PhysicalObjectKind = PhysicalObjectKind.Food): PhysicalObjectDefinitions {
    Lobster("lobster"),
    Sugar("sugar")
}

fun buildGeneralFoodLexicon(lexicon: Lexicon) {
    Foods.values().forEach { lexicon.addMapping(PhysicalObjectWord(it)) }
}
