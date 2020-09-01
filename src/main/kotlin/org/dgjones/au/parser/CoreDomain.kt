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

/* Food */

fun LexicalConceptBuilder.food(kindOfFood: String, name: String = kindOfFood, initializer: LexicalConceptBuilder.() -> Unit)  {
    val child = LexicalConceptBuilder(root, PhysicalObjectKind.Food.name)
    child.slot("name", name)
    child.slot("kind", kindOfFood)
    child.apply(initializer)
    val c = child.build()
}

// FIXME generate food word handlers
enum class FoodKind {
    Lobster,
    Sugar
}

fun buildFood(kindOfFood: String, name: String = kindOfFood): Concept {
    return Concept(PhysicalObjectKind.Food.name)
        .with(Slot("kind", Concept(kindOfFood)))
        .with(Slot("name", Concept(name)))
}


class WordLobster : WordHandler(EntryWord("lobster")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildFood(FoodKind.Lobster.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}

class WordSugar(): WordHandler(EntryWord("sugar")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildFood(FoodKind.Sugar.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
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

fun buildHuman(firstName: String = "", lastName: String = "", gender: Gender? = null): Concept {
    return Concept(InDepthUnderstandingConcepts.Human.name)
        .with(Slot("firstName", Concept(firstName)))
        .with(Slot("lastName", Concept(lastName)))
        //FIXME empty concept doesn't seem helpful
        .with(Slot("gender", Concept(gender?.name ?: "")))
}
enum class Gender {
    Male,
    Female,
    Other
}

// Exercise 1
class WordPerson(val human: Concept, word: String = human.valueName("firstName")?:"unknown"): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        // Fixme - not sure about the load/reuse
        return listOf(LoadCharacterDemon(human, wordContext), SaveCharacterDemon(wordContext))
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
        wordContext.defHolder.value =  buildPhysicalObject(PhysicalObjectKind.Plant.name, "tree")
        return listOf(SaveObjectDemon(wordContext))
    }
}
