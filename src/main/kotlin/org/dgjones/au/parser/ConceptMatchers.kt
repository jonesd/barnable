package org.dgjones.au.parser

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
    return { c -> false}
}

fun matchAlways(): ConceptMatcher {
    return { c -> true}
}