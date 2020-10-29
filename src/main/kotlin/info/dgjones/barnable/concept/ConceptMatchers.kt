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

import info.dgjones.barnable.grammar.Case

typealias ConceptMatcher = (Concept?) -> Boolean

fun matchConceptByHead(kind: String): ConceptMatcher {
    return { c -> c?.name == kind }
}

fun matchConceptByHead(kinds: Collection<String>): ConceptMatcher {
    return { c -> kinds.contains(c?.name)}
}

fun matchConceptByKind(kind: String): ConceptMatcher {
    return matchConceptValueName("kind", kind)
}

fun matchConceptByKind(kinds: Collection<String>): ConceptMatcher {
    return { c -> kinds.contains(c?.valueName("kind")) }
}

fun matchCase(match: Case): ConceptMatcher {
    return matchConceptValueName("case", match.name)
}

fun matchUnresovledVariables(): ConceptMatcher {
    return { c -> c?.isVariable() == true}
}

fun matchConceptValueName(slot: Fields, match: String): ConceptMatcher {
    return { c -> c?.valueName(slot) == match }
}

fun matchConceptValueName(slot: String, match: String): ConceptMatcher {
    return { c -> c?.valueName(slot) == match }
}

fun matchAny(matchers: List<ConceptMatcher>): ConceptMatcher {
    return { c -> matchers.any { it(c) }}
}

fun matchAll(matchers: List<ConceptMatcher>): ConceptMatcher {
    return { c -> matchers.all { it(c) }}
}

fun matchNever(): ConceptMatcher {
    return { _ -> false}
}

fun matchAlways(): ConceptMatcher {
    return { _ -> true}
}

class ConceptMatcherBuilder {
    var matchers = mutableListOf<ConceptMatcher>()
    fun with(matcher: ConceptMatcher): ConceptMatcherBuilder {
        matchers.add(matcher)
        return this
    }
    fun matchSetField(valueName: Fields, match: String?): ConceptMatcherBuilder {
        if (match != null && isConceptValueResolved(match) /* FIXME?match != null && match.isNotBlank()*/) {
            matchers.add(matchConceptValueName(valueName, match))
        }
        return this
    }
    fun matchAll(): ConceptMatcher {
        return matchAll(matchers)
    }
}