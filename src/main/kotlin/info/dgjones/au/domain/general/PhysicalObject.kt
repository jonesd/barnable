package info.dgjones.au.domain.general

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.LexicalConceptBuilder
import info.dgjones.au.concept.Slot
import info.dgjones.au.narrative.PhysicalObjectKind
import info.dgjones.au.parser.*


fun LexicalConceptBuilder.physicalObject(name: String, kind: String, initializer: LexicalConceptBuilder.() -> Unit)  {
    val child = LexicalConceptBuilder(root, PhysicalObjectKind.PhysicalObject.name)
    child.slot("name", name)
    child.slot("kind", kind)
    child.apply(initializer)
    val c = child.build()
    // FIXME add physicalObject
}

fun buildPhysicalObject(kind: String, name: String): Concept {
    return Concept(PhysicalObjectKind.PhysicalObject.name)
        .with(Slot("kind", Concept(kind)))
        .with(Slot("name", Concept(name)))
}

// Word Senses

fun buildGeneralPhysicalObjectsLexicon(lexicon: Lexicon) {
    // FIXME use catalagoue of physical objects
    // FIXME support containers
    lexicon.addMapping(WordBook())
    lexicon.addMapping(WordTree())
}

class WordBook: WordHandler(EntryWord("book")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildPhysicalObject(PhysicalObjectKind.Book.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}

class WordTree: WordHandler(EntryWord("tree")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildPhysicalObject(PhysicalObjectKind.Plant.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}
