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
    MultipleGroup,
    Values
}

enum class GroupFields(override val fieldName: String): Fields {
    GroupInstances("groupInstances"),
    Elements("elements"),
    ElementsType("elementsType")
}

class GroupAccessor(private val group: Concept) {
    val size: Int
        get() {
            val values = values()
            val index = values?.slotNames()?.mapNotNull { it.toIntOrNull() }?.maxOfOrNull { it }
            return if (index != null) index + 1 else 0
        }
    operator fun get(i: Int): Concept? {
        return values()?.value(i.toString())
    }
    fun concepts(): List<Concept> {
        return values()?.children()?.filterNotNull() ?: listOf<Concept>()
    }
    fun valueNames(): List<String> {
        return concepts().map { it.name }
    }
    fun elementType(): String? {
        return group.valueName(GroupFields.ElementsType)
    }
    fun add(concept: Concept):Boolean {
        if (elementType() == concept.name) {
            group.value(GroupFields.Elements)?.let { elements ->
                elements.value(elements.children().size.toString(), concept)
                return true
            }
        } else {
            print("ERROR element $concept does not match group type $group")
        }
        return false
    }

    init {
        check(group.name == GroupConcept.Group.name) { "should be group rather than ${group.name}"}
    }
    private fun values() = group.value(GroupFields.Elements)
}

/**
 * Build a group concept structure from the provided elements.
 * By default the element type of the group will be found from the first element,
 * however, it can be set/overridden as an optional parameter.
 */
fun buildGroup(elements: List<Concept>, elementType: Concept? = null): Concept {
    val root = Concept(GroupConcept.Group.name)
    val elementsField = Concept(GroupConcept.Values.name)
    root.with(Slot(GroupFields.Elements, elementsField))
    elements.forEachIndexed { index, element -> elementsField.with(Slot(index.toString(), element))}
    val t = elementType ?: (elements.firstOrNull()?.let { extractConceptHead.transform(it)})
    t?.let { root.value(GroupFields.ElementsType, t) }
    return root
}

fun addConceptToHomogenousGroup(group: Concept, concept: Concept): Boolean {
    return GroupAccessor(group).add(concept)
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