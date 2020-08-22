class LexicalRootBuilder(val wordContext: WordContext, val headName: String) {
    val root = LexicalConceptBuilder(this, headName)

    fun build(): LexicalConcept {
        return LexicalConcept(wordContext, root.build())
    }
}

class LexicalConceptBuilder(val root: LexicalRootBuilder, conceptName: String) {
    val concept = Concept(conceptName)

    fun slot(slotName: String, slotValue: String) {
        concept.with(Slot(slotName, Concept(slotValue)))
    }
    fun slot(slotName: String, slotValue: String, initializer: LexicalConceptBuilder.() -> Unit) {
        val child = LexicalConceptBuilder(root, slotValue)
        child.apply(initializer)
        val c = child.build()
        concept.with(Slot(slotName, c))
    }
    fun build(): Concept {
        return concept
    }
}

fun lexicalConcept(wordContext: WordContext, headName: String, initializer: LexicalConceptBuilder.() -> Unit): LexicalConcept {
    val builder = LexicalRootBuilder(wordContext, headName)
    builder.root.apply(initializer)
    return builder.build()
}

class LexicalRoot(val wordContext: WordContext, val head: Concept)

class LexicalConcept(val wordContext: WordContext, val head: Concept) {
}