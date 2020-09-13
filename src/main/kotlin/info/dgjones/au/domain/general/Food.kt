package info.dgjones.au.domain.general

import info.dgjones.au.narrative.PhysicalObjectKind
import info.dgjones.au.parser.*

// FIXME generate food word handlers
enum class Foods {
    Lobster,
    Sugar
}

fun buildGeneralFoodLexicon(lexicon: Lexicon) {
    Foods.values().forEach { lexicon.addMapping(KindFood(it)) }
}

class KindFood(private val food: Foods):  WordHandler(EntryWord(food.name)) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildFood(food, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}


fun buildFood(kindOfFood: Foods, name: String = kindOfFood.name): Concept {
    return Concept(PhysicalObjectKind.Food.name)
        .with(Slot("kind", Concept(kindOfFood.name)))
        .with(Slot("name", Concept(name)))
}

fun LexicalConceptBuilder.food(kindOfFood: String, name: String = kindOfFood, initializer: LexicalConceptBuilder.() -> Unit)  {
    val child = LexicalConceptBuilder(root, PhysicalObjectKind.Food.name)
    child.slot("name", name)
    child.slot("kind", kindOfFood)
    child.apply(initializer)
    val c = child.build()
}
