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

import info.dgjones.barnable.concept.Concept

/* ConceptList holds an ordered list of concepts
* There is no presumed element type.
* Compare with the "Group" of concepts that requires typed concepts as the elements
*/

enum class ListConcept {
    List
}

class ConceptListAccessor(private val list: Concept) {
    val size: Int
        get() {
            val index = list.slotNames().mapNotNull { it.toIntOrNull() }.maxOfOrNull { it }
            return if (index != null) index + 1 else 0
        }
    operator fun get(i: Int): Concept? {
        return list.value(i.toString())
    }
    fun concepts(): List<Concept> {
        return list.children().filterNotNull()
    }
    fun valueNames(): List<String> {
        return concepts().map { it.name }
    }
    fun add(concept: Concept) {
        list.value(size.toString(), concept)
    }
}

fun buildConceptList(elements: List<Concept>, rootName: String = ListConcept.List.name): Concept {
    val list = Concept(rootName)
    elements.forEachIndexed { index, element -> list.value(index.toString(), element)}
    return list
}