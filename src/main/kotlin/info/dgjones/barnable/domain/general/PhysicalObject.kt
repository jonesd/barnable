/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.parser.*

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

interface PhysicalObjectDefinitions {
    val title: String
    fun title(): String {
        return title
    }
    val kind: PhysicalObjectKind
    fun kind(): PhysicalObjectKind {
        return kind
    }
}

enum class PhysicalObjects(override val title: String, override val kind: PhysicalObjectKind): PhysicalObjectDefinitions {
    Ball("ball", PhysicalObjectKind.GameObject),
    Book("book", PhysicalObjectKind.PhysicalObject),
    Box("box", PhysicalObjectKind.Container),
    Tree("tree", PhysicalObjectKind.Plant),
}

fun LexicalConceptBuilder.physicalObject(name: String, kind: String, initializer: LexicalConceptBuilder.() -> Unit)  {
    val child = LexicalConceptBuilder(root, PhysicalObjectKind.PhysicalObject.name)
    child.slot(CoreFields.Name, name)
    child.slot(CoreFields.Kind, kind)
    child.apply(initializer)
    child.saveAsObject()
    child.build()
}

fun buildPhysicalObject(physicalObject: PhysicalObjectDefinitions): Concept =
    Concept(PhysicalObjectKind.PhysicalObject.name)
        .with(Slot(CoreFields.Name, Concept(physicalObject.title)))
        .with(Slot(CoreFields.Kind, Concept(physicalObject.kind.name)))

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

fun LexicalConceptBuilder.saveAsObject() {
    root.addDemon(SaveObjectDemon(root.wordContext))
}

// Word Senses

fun buildGeneralPhysicalObjectsLexicon(lexicon: Lexicon) {
    PhysicalObjects.values().forEach { lexicon.addMapping(PhysicalObjectWord(it)) }
}

class PhysicalObjectWord(private val physicalObject: PhysicalObjectDefinitions): WordHandler(EntryWord(physicalObject.title)) {
    override fun build(wordContext: WordContext): List<Demon> =
        buildLexicalPhysicalObject(physicalObject.kind.name, physicalObject.title, wordContext).demons
}
