class LexicalRootBuilder(val wordContext: WordContext, val headName: String) {
    val root = LexicalConceptBuilder(this, headName)

    val demons = mutableListOf<Demon>()
    val variableSlots = mutableListOf<Slot>()
    val completedSlots = mutableListOf<Slot>()
    val completedConceptHolders = mutableListOf<ConceptHolder>()

    fun build(): LexicalConcept {
        return LexicalConcept(wordContext, root.build(), demons)
    }

    // FIXME shouldn't associate this state that lives on beyond build() to builder
    // should create new holder instead
    fun createVariable(slotName: String, variableName: String? = null): Slot {
        val name = "*VAR."+(variableName ?: wordContext.context.workingMemory.nextVariableIndex()) +"*"
        val slot = Slot(slotName, Concept(name))
        variableSlots.add(slot)
        return slot
    }
    fun completeVariable(variableSlot: Slot, valueHolder: ConceptHolder) {
        val variableName = variableSlot.value?.name ?: return
        completedConceptHolders.add(valueHolder)
        val completeVariableSlots = variableSlots.filter { it.value?.name == variableName }
        //FIXME can multiple demons update the same value here?
        completeVariableSlots.forEach { it.value = valueHolder.value }
        completedSlots.addAll(completeVariableSlots)
        variableSlots.removeAll(completeVariableSlots)
        if (variableSlots.isEmpty()) {
            completedConceptHolders.forEach { it.addFlag(ParserFlags.Inside)  }
        }
    }
    fun addDemon(demon: Demon) {
        this.demons.add(demon)
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
    fun expectHead(slotName: String, variableName: String? = null, headValue: String, direction: SearchDirection = SearchDirection.After) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = ExpectDemon(matchConceptByHead(headValue), direction, root.wordContext) {
            root.completeVariable(variableSlot, it)
        }
        root.addDemon(demon)
    }
    fun expectKind(slotName: String, variableName: String? = null, kinds: List<String>, direction: SearchDirection = SearchDirection.After) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val demon = ExpectDemon(matchConceptByKind(kinds), direction, root.wordContext) {
            root.completeVariable(variableSlot, it)
        }
        root.addDemon(demon)
    }
    fun expectPrep(slotName: String, variableName: String? = null, preps: Collection<Preposition>, matcher: ConceptMatcher, direction: SearchDirection = SearchDirection.After) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
        val matchers = matchAll(listOf(
            matchPrepIn(preps.map { it.name }),
            matcher
        ))
        val demon = PrepDemon(matchers, SearchDirection.After, root.wordContext) {
            root.completeVariable(variableSlot, it)
        }
        root.addDemon(demon)
    }
    fun varReference(slotName: String, variableName: String) {
        val variableSlot = root.createVariable(slotName, variableName)
        concept.with(variableSlot)
    }
    fun physicalObject(name: String, kind: String, initializer: LexicalConceptBuilder.() -> Unit)  {
        val child = LexicalConceptBuilder(root, PhysicalObjectKind.PhysicalObject.name)
        child.slot("name", name)
        child.slot("kind", kind)
        child.apply(initializer)
        val c = child.build()
    }
    fun food(kindOfFood: String, name: String = kindOfFood, initializer: LexicalConceptBuilder.() -> Unit)  {
        val child = LexicalConceptBuilder(root, PhysicalObjectKind.Food.name)
        child.slot("name", name)
        child.slot("kind", kindOfFood)
        child.apply(initializer)
        val c = child.build()
    }
}

fun lexicalConcept(wordContext: WordContext, headName: String, initializer: LexicalConceptBuilder.() -> Unit): LexicalConcept {
    val builder = LexicalRootBuilder(wordContext, headName)
    builder.root.apply(initializer)
    return builder.build()
}

class LexicalRoot(val wordContext: WordContext, val head: Concept)

class LexicalConcept(val wordContext: WordContext, val head: Concept, val demons: List<Demon>) {
    init {
        wordContext.defHolder.value = head
    }
}