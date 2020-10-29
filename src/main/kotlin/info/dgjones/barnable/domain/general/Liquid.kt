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

import info.dgjones.barnable.parser.Lexicon

enum class Liquids(override val title: String, override val kind: PhysicalObjectKind = PhysicalObjectKind.Liquid): PhysicalObjectDefinitions {
    CocaCola("Coca-Cola"),
    Coffee("coffee"),
    Coke("coke"),
    Soda("soda"),
    Tea("tea"),
    Water("water"),
    Wine("wine")
}

fun buildGeneralLiquidLexicon(lexicon: Lexicon) {
    Liquids.values().forEach { lexicon.addMapping(PhysicalObjectWord(it)) }
}
