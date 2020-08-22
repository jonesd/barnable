class ConceptBuilder {
    fun build(): Concept {
        // TODO
        return Concept("TODO")
    }
}

fun concept(initializer: ConceptBuilder.() -> Unit): ConceptBuilder {
    return ConceptBuilder().apply(initializer)
}