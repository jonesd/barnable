package org.dgjones.au.parser

import org.dgjones.au.narrative.*

/* Physical Objects */

fun LexicalConceptBuilder.physicalObject(name: String, kind: String, initializer: LexicalConceptBuilder.() -> Unit)  {
    val child = LexicalConceptBuilder(root, PhysicalObjectKind.PhysicalObject.name)
    child.slot("name", name)
    child.slot("kind", kind)
    child.apply(initializer)
    val c = child.build()
}

/* Humans */

fun LexicalConceptBuilder.human(firstName: String = "", lastName: String = "", gender: Gender? = null, initializer: LexicalConceptBuilder.() -> Unit): Concept {
    val child = LexicalConceptBuilder(root, InDepthUnderstandingConcepts.Human.name)
    child.slot("firstName", firstName)

    return Concept(InDepthUnderstandingConcepts.Human.name)
        .with(Slot("firstName", Concept(firstName)))
        .with(Slot("lastName", Concept(lastName)))
        //FIXME empty concept doesn't seem helpful
        .with(Slot("gender", Concept(gender?.name ?: "")))
}

fun buildHuman(firstName: String? = "", lastName: String? = "", gender: String? = null): Concept {
    return Concept(InDepthUnderstandingConcepts.Human.name)
        .with(Slot("firstName", Concept(firstName ?: "")))
        .with(Slot("lastName", Concept(lastName ?: "")))
        //FIXME empty concept doesn't seem helpful
        .with(Slot("gender", Concept(gender ?: "")))
}
enum class Gender {
    Male,
    Female,
    Other
}


// Exercise 1
class WordPerson(val human: Concept, word: String = human.valueName(Human.FIRST_NAME)?:"unknown"): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            // FIXME not sure about defaulting to ""
            slot(Human.FIRST_NAME, human.valueName(Human.FIRST_NAME) ?: "")
            lastName(Human.LAST_NAME)
            //slot(Human.LAST_NAME, human.valueName(Human.LAST_NAME) ?: "")
            slot(Human.GENDER, human.valueName(Human.GENDER)?: "")
            checkCharacter(CoreFields.INSTANCE.fieldName)
        }
        return lexicalConcept.demons
        // Fixme - not sure about the load/reuse
        // FIXMEreturn listOf(LoadCharacterDemon(human, wordContext), SaveCharacterDemon(wordContext))
    }
}

fun buildPhysicalObject(kind: String, name: String): Concept {
    return Concept(PhysicalObjectKind.PhysicalObject.name)
        .with(Slot("kind", Concept(kind)))
        .with(Slot("name", Concept(name)))
}

class WordBook(): WordHandler(EntryWord("book")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildPhysicalObject(PhysicalObjectKind.Book.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}

class WordTree(): WordHandler(EntryWord("tree")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildPhysicalObject(PhysicalObjectKind.Plant.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}
