package org.dgjones.au.parser

import org.dgjones.au.narrative.InDepthUnderstandingConcepts

interface Fields {
    val fieldName: String
    fun fieldName(): String {
        return fieldName
    }
}

enum class CoreFields(override val fieldName: String): Fields {
    INSTANCE("instan");
}

enum class Human(override val fieldName: String): Fields {
    CONCEPT("Human"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    GENDER("gender");
}

fun characterMatcher(human: Concept): ConceptMatcher =
    characterMatcher(human.valueName(Human.FIRST_NAME), human.valueName(Human.LAST_NAME), human.valueName(Human.GENDER))

fun characterMatcher(firstName: String?, lastName: String?, gender: String?): ConceptMatcher =
    ConceptMatcherBuilder()
        .with(matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
        .matchSetField(Human.FIRST_NAME, firstName)
        .matchSetField(Human.LAST_NAME, lastName)
        .matchSetField(Human.GENDER, gender)
        .matchAll()

fun characterMatcherWithInstance(human: Concept): ConceptMatcher =
    ConceptMatcherBuilder()
        .with(characterMatcher(human))
        .matchSetField(CoreFields.INSTANCE, human.valueName(CoreFields.INSTANCE))
        .matchAll()

