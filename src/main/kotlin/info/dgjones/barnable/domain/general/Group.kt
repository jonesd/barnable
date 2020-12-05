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

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.parser.*

/* Group concepts hold a list of other concept of the same head value */

enum class GroupConcept {
    Group,
    MultipleGroup
}

enum class GroupFields(override val fieldName: String): Fields {
    GroupInstances("groupInstances"),
    Elements("elements"),
    ElementsType("elementsType"),
    Next("next")
}

fun buildGroup(elements: List<Concept>): Concept {
    val root = Concept(GroupConcept.Group.name)
    val elementsField = Concept("values")
    root.with(Slot(GroupFields.Elements, elementsField))
    elements.forEachIndexed { index, element -> elementsField.with(Slot(index.toString(), element))}
    elements.firstOrNull()?.let { root.value(GroupFields.ElementsType, extractConceptHead.transform(it)) }
    return root
}

fun addConceptToHomogenousGroup(group: Concept, concept: Concept): Boolean {
    if (group.valueName(GroupFields.ElementsType) == concept.name) {
        group.value(GroupFields.Elements)?.let { elements ->
            elements.value(elements.children().size.toString(), concept)
            return true
        }
    } else {
        print("ERROR element $concept does not match group type $group")
    }
    return false
}

fun LexicalConceptBuilder.addToGroup(matcher: ConceptMatcher, direction: SearchDirection = SearchDirection.After,) {
    val demon = AddToGroupDemon(matcher, direction, root.wordContext) { elementHolder ->
        elementHolder.value = null
        elementHolder.addFlag(ParserFlags.Inside)
    }
    root.addDemon(demon)
}

/* Find the matcher element and add it to the predecessor group. Pass the found element as the demon resulting action */
class AddToGroupDemon(val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, matchNever(), direction = direction, wordContext = wordContext) { elementHolder ->
            elementHolder.value?.let { element ->
                searchContext(matchConceptByHead(GroupConcept.Group.name), matchNever(), direction = SearchDirection.Before, wordContext = wordContext) { groupHolder ->
                    groupHolder.value?.let { group ->
                        if (addConceptToHomogenousGroup(group, element)) {
                            active = false
                            println("Added $element to group $group")
                            action(elementHolder)
                        }
                    }
                }
            }
        }
    }
    override fun description(): String {
        return "Match concept and add to the predecessor Group, assuming head type matches"
    }
}