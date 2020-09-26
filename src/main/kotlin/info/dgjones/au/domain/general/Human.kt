package info.dgjones.au.domain.general

import info.dgjones.au.concept.*
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

enum class Human(override val fieldName: String): Fields {
    CONCEPT("Human"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    GENDER("gender");
}

enum class Gender {
    Male,
    Female,
    Other
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

// Word Senses

fun buildGeneralHumanLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordPerson(buildHuman("Fred", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("John", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("Mary", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Ann", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Anne", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Bill", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("George", "", Gender.Male.name)))
}

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
        // FIXME return listOf(LoadCharacterDemon(human, wordContext), SaveCharacterDemon(wordContext))
    }
}
