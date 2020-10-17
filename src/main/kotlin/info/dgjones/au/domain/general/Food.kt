package info.dgjones.au.domain.general

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.CoreFields
import info.dgjones.au.concept.Slot
import info.dgjones.au.parser.*

// FIXME generate food word handlers
enum class Foods {
    Lobster,
    Sugar
}

fun buildGeneralFoodLexicon(lexicon: Lexicon) {
    Foods.values().forEach { lexicon.addMapping(FoodWord(it)) }
}

class FoodWord(private val food: Foods):  WordHandler(EntryWord(food.name)) {
    override fun build(wordContext: WordContext): List<Demon> =
        buildLexicalPhysicalObject(PhysicalObjectKind.Food.name, food.name, wordContext).demons
}

fun buildFood(kindOfFood: Foods, name: String = kindOfFood.name): Concept {
    return Concept(PhysicalObjectKind.Food.name)
        .with(Slot(CoreFields.Kind, Concept(kindOfFood.name)))
        .with(Slot(CoreFields.Name, Concept(name)))
}
