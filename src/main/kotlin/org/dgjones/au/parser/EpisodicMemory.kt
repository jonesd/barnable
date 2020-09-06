package org.dgjones.au.parser

class EpisodicMemory {
    val concepts = mutableListOf<Concept>()

    fun addConcept(c: Concept) {
        recentUseOf(c)
    }

    fun search(matcher: ConceptMatcher): Concept? {
        val found = concepts.firstOrNull { matcher(it) }
        if (found != null) {
            recentUseOf(found)
        }
        return found
    }

    private fun recentUseOf(c: Concept) {
        concepts.remove(c)
        concepts.add(0, c)
    }

    fun concepts(): List<Concept> {
        return concepts.toList()
    }

    override fun toString(): String {
        return "EpisodicMemory $concepts"
    }

    // FIXME other kind of elements
    // FIXME Recency for access - should move used element to head of list
    // FIXME use a linked list
}