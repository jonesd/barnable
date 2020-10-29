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

package info.dgjones.barnable.concept

fun buildConceptPathAccessor(concept: Concept, targetSlotName: String): ConceptSlotAccessor? {
    val path = ConceptPathBuilder(concept, targetSlotName).build(concept)
    return if (path != null) {
        return conceptPathAccessor(concept, path)
    } else {
        null
    }
}

class ConceptPathBuilder(private val root: Concept, private val targetSlotName: String) {
    fun build(concept: Concept = root, path: List<String> = listOf()): List<String>? {
        if (targetSlotName.isEmpty()) {
            return path
        }
        concept.slots().forEach {
            if (it.name == targetSlotName) return path + it.name
        }
        concept.slots().forEach {
            val value = it.value
            if (value != null) {
                val resultPath = build(value, path + it.name)
                if (resultPath != null) {
                    return resultPath
                }
            }
        }
        return null
    }
}

typealias ConceptSlotAccessor = () -> Concept?

fun conceptPathAccessor(root: Concept?, targetSlotPath: List<String>): () -> Concept? =
    { targetSlotPath.fold(root) { concept, slotName -> concept?.value(slotName) } }
