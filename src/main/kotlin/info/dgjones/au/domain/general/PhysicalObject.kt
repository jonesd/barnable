package info.dgjones.au.domain.general

import info.dgjones.au.concept.*
import info.dgjones.au.parser.*

enum class PhysicalObjectKind {
    PhysicalObject,
    Container,
    GameObject,
    Book,
    Food,
    Liquid,
    Location,
    BodyPart,
    Plant, // Tree?
}

fun LexicalConceptBuilder.physicalObject(name: String, kind: String, initializer: LexicalConceptBuilder.() -> Unit)  {
    val child = LexicalConceptBuilder(root, PhysicalObjectKind.PhysicalObject.name)
    child.slot(CoreFields.Name, name)
    child.slot(CoreFields.Kind, kind)
    child.apply(initializer)
    child.saveAsObject()
    child.build()
}

fun buildPhysicalObject(kind: String, name: String): Concept {
    return Concept(PhysicalObjectKind.PhysicalObject.name)
        .with(Slot(CoreFields.Kind, Concept(kind)))
        .with(Slot(CoreFields.Name, Concept(name)))
}

fun buildLexicalPhysicalObject(kind: String, name: String,  wordContext: WordContext, initializer: (LexicalConceptBuilder.() -> Unit)? = null): LexicalConcept {
    val builder = LexicalRootBuilder(wordContext, PhysicalObjectKind.PhysicalObject.name)
    builder.root.apply {
        slot(CoreFields.Kind, kind)
        slot(CoreFields.Name, name)
        saveAsObject()
    }
    initializer?.let { builder.root.apply(initializer)}
    return builder.build()
}

// Word Senses

fun buildGeneralPhysicalObjectsLexicon(lexicon: Lexicon) {
    // FIXME use catalogue of physical objects
    // FIXME support containers
    lexicon.addMapping(WordBook())
    lexicon.addMapping(WordTree())
    lexicon.addMapping(WordBall())
    lexicon.addMapping(WordBox())
}

class WordBook: WordHandler(EntryWord("book")) {
    override fun build(wordContext: WordContext): List<Demon> =
        buildLexicalPhysicalObject(PhysicalObjectKind.Book.name, word.word, wordContext).demons
}

class WordTree: WordHandler(EntryWord("tree")) {
    override fun build(wordContext: WordContext): List<Demon> =
        buildLexicalPhysicalObject(PhysicalObjectKind.Plant.name, word.word, wordContext).demons
}

class WordBall: WordHandler(EntryWord("ball")) {
    override fun build(wordContext: WordContext): List<Demon> =
        buildLexicalPhysicalObject(PhysicalObjectKind.GameObject.name, word.word, wordContext).demons
}

class WordBox: WordHandler(EntryWord("box")) {
    override fun build(wordContext: WordContext): List<Demon> =
        buildLexicalPhysicalObject(PhysicalObjectKind.Container.name, word.word, wordContext).demons
}
