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

import info.dgjones.barnable.domain.general.GroupFields
import info.dgjones.barnable.grammar.Case

interface ConceptMatcher {
    fun matches(concept: Concept?): Boolean
}

fun matchGroupInstanceType(kind: String): ConceptMatcher {
    return matchConceptValueName(GroupFields.ElementsType, kind)
}

fun matchGroupInstanceType(kinds: Collection<String>): ConceptMatcher {
    return matchConceptValueName(GroupFields.ElementsType.fieldName, kinds)
}

fun matchConceptByHead(kind: String): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return c?.name == kind
        }

        override fun toString(): String {
            return "(c.name == $kind)"
        }
    }
}

fun matchConceptByHead(kinds: Collection<String>): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return kinds.contains(c?.name)
        }

        override fun toString(): String {
            return "(c.name anyOf $kinds)"
        }
    }
}

fun matchConceptByHeadOrGroup(kind: String): ConceptMatcher {
    return matchAny(listOf(matchConceptByHead(kind), matchGroupInstanceType(kind)))
}

fun matchConceptByHeadOrGroup(kinds: Collection<String>): ConceptMatcher {
    return matchAny(listOf(matchConceptByHead(kinds), matchGroupInstanceType(kinds)))
}

fun matchConceptByKind(kind: String): ConceptMatcher {
    return matchConceptValueName("kind", kind)
}

fun matchConceptByKind(kinds: Collection<String>): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return kinds.contains(c?.valueName("kind"))
        }

        override fun toString(): String {
            return "(c.kind anyOf $kinds)"
        }
    }
}

fun matchCase(match: Case): ConceptMatcher {
    return matchConceptValueName("case", match.name)
}

fun matchUnresolvedVariables(): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return c?.isVariable() == true
        }

        override fun toString(): String {
            return "(c.isVariable() == true)"
        }
    }
}

fun matchConceptHasSlotName(slot: Fields): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return c?.value(slot) != null
        }

        override fun toString(): String {
            return "(c.${slot.fieldName} != null)"
        }
    }
}

fun matchConceptValueName(slot: Fields, match: String): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return c?.valueName(slot) == match
        }

        override fun toString(): String {
            return "(c.${slot.fieldName} == $match)"
        }
    }
}

fun matchConceptValueName(slot: String, match: String): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return c?.valueName(slot) == match
        }

        override fun toString(): String {
            return "(c.${slot} == $match))"
        }
    }
}

fun matchConceptValueName(slot: String, matches: Collection<String>): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return matches.contains(c?.valueName(slot))
        }

        override fun toString(): String {
            return "(c.${slot} anyOf $matches)"
        }
    }
}

fun matchAny(matchers: List<ConceptMatcher>): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return matchers.any { it.matches(c) }
        }

        override fun toString(): String {
            val m = matchers.map { it.toString() }.joinToString("|")
            return "(anyOf $m))"
        }
    }
}

fun matchAll(matchers: List<ConceptMatcher>): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return matchers.all { it.matches(c) }
        }

        override fun toString(): String {
            val m = matchers.map { it.toString() }.joinToString("&")
            return "(allOf $m)"
        }
    }
}

fun matchNot(matcher: ConceptMatcher): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return !matcher.matches(c)
        }

        override fun toString(): String {
            return "(!${matcher.toString()})"
        }
    }
}

fun matchNever(): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return false
        }

        override fun toString(): String {
            return "false"
        }
    }
}

fun matchAlways(): ConceptMatcher {
    return object : ConceptMatcher {
        override fun matches(c: Concept?): Boolean {
            return true
        }

        override fun toString(): String {
            return "true"
        }
    }
}

class ConceptMatcherBuilder {
    var matchers = mutableListOf<ConceptMatcher>()
    fun with(matcher: ConceptMatcher): ConceptMatcherBuilder {
        matchers.add(matcher)
        return this
    }

    fun matchSetField(valueName: Fields, match: String?): ConceptMatcherBuilder {
        if (match != null && isConceptValueResolved(match)) {
            matchers.add(matchConceptValueName(valueName, match))
        }
        return this
    }

    fun matchAll(): ConceptMatcher {
        return matchAll(matchers)
    }
}