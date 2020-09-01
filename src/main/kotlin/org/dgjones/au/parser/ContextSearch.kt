package org.dgjones.au.parser

enum class SearchDirection {
    After,
    Before
}

typealias ConceptMatcher = (Concept?) -> Boolean

fun matchConceptByHead(kind: String): ConceptMatcher {
    return { c -> c?.name == kind }
}

fun matchConceptByHead(kinds: Collection<String>): ConceptMatcher {
    return { c -> kinds.contains(c?.name)}
}

fun matchConceptByKind(kind: String): ConceptMatcher {
    return { c -> c?.valueName("kind") == kind }
}

fun matchConceptByKind(kinds: Collection<String>): ConceptMatcher {
    return { c -> kinds.contains(c?.valueName("kind")) }
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

/* Fundamental search function to navigate the sentence for concepts or words. If a match is found, then the action is called with the result */
fun searchContext(matcher: ConceptMatcher, abortSearch: ConceptMatcher = matchNever(), matchPreviousWord: String? = null, direction: SearchDirection = SearchDirection.Before, wordContext: WordContext, action: (ConceptHolder) -> Unit) {
    var index = wordContext.wordIndex
    var found: ConceptHolder? = null

    fun isMatchWithSentenceWord(index: Int): Boolean {
        return (matchPreviousWord != null && index >= 0 && wordContext.context.sentenceWordAtWordIndex(index) == matchPreviousWord)
    }
    fun updateFrom(existing: ConceptHolder?, wordContext: WordContext, index: Int): ConceptHolder? {
        if (existing?.value != null) {
            return existing
        }
        if (matchPreviousWord != null && !isMatchWithSentenceWord(index - 1)) {
            // failed to include match on provies sentence word
            return null
        }
        val defHolder = wordContext.context.defHolderAtWordIndex(index)
        var value = defHolder.value
        if (abortSearch(value)) {
            // FIXME should not search any farther in this direction
            return null
        }
        if (matcher(value)) {
            return defHolder
        }
        return null
    }

    if (direction == SearchDirection.Before) {
        (wordContext.wordIndex - 1 downTo 0).forEach {
            found = updateFrom(found, wordContext, it)
        }
    } else {
        (wordContext.wordIndex + 1 until wordContext.context.wordContexts.size).forEach {
            found = updateFrom(found, wordContext, it)
        }
    }
    val foundConcept = found
    if (foundConcept?.value != null) {
        action(foundConcept)
        println("Search found concept=$foundConcept for match=$matcher")
    }
}