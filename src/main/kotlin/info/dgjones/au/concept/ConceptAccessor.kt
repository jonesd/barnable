package info.dgjones.au.concept

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
