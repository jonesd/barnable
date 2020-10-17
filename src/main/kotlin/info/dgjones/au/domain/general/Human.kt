package info.dgjones.au.domain.general

import info.dgjones.au.concept.*
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

enum class HumanConcept {
    Human
}

enum class HumanFields(override val fieldName: String): Fields {
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    GENDER("gender")
}

enum class Gender {
    Male,
    Female,
    Neutral
}

fun characterMatcher(human: Concept): ConceptMatcher =
    characterMatcher(human.valueName(HumanFields.FIRST_NAME),
        human.valueName(HumanFields.LAST_NAME),
        human.valueName(HumanFields.GENDER),
        human.valueName(RoleThemeFields.RoleTheme.fieldName)
    )

fun characterMatcher(firstName: String?, lastName: String?, gender: String?, roleTheme: String?): ConceptMatcher =
    ConceptMatcherBuilder()
        .with(matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
        .matchSetField(HumanFields.FIRST_NAME, firstName)
        .matchSetField(HumanFields.LAST_NAME, lastName)
        .matchSetField(RoleThemeFields.RoleTheme, roleTheme)
        .matchSetField(HumanFields.GENDER, gender)
        .matchAll()

fun characterMatcherWithInstance(human: Concept): ConceptMatcher =
    ConceptMatcherBuilder()
        .with(characterMatcher(human))
        .matchSetField(CoreFields.INSTANCE, human.valueName(CoreFields.INSTANCE))
        .matchAll()


fun LexicalConceptBuilder.human(firstName: String = "", lastName: String = "", gender: Gender? = null, initializer: LexicalConceptBuilder.() -> Unit): Concept {
    val child = LexicalConceptBuilder(root, InDepthUnderstandingConcepts.Human.name)
    child.slot(HumanFields.FIRST_NAME, firstName)

    return Concept(InDepthUnderstandingConcepts.Human.name)
        .with(Slot(HumanFields.FIRST_NAME, Concept(firstName)))
        .with(Slot(HumanFields.LAST_NAME, Concept(lastName)))
        //FIXME empty concept doesn't seem helpful
        .with(Slot(HumanFields.GENDER, Concept(gender?.name ?: "")))
}

fun buildHuman(firstName: String? = "", lastName: String? = "", gender: String? = null): Concept {
    return Concept(InDepthUnderstandingConcepts.Human.name)
        .with(Slot(HumanFields.FIRST_NAME, Concept(firstName ?: "")))
        .with(Slot(HumanFields.LAST_NAME, Concept(lastName ?: "")))
        //FIXME empty concept doesn't seem helpful
        .with(Slot(HumanFields.GENDER, Concept(gender ?: "")))
}

fun humanKeyValue(human: Concept) =
    human.selectKeyValue(HumanFields.FIRST_NAME, HumanFields.LAST_NAME, RoleThemeFields.RoleTheme)

// Word Senses

fun buildGeneralHumanLexicon(lexicon: Lexicon) {
    lexicon.addMapping(WordPerson(buildHuman("Ann", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Anne", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Bill", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("Fred", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("George", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("Jane", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("John", "", Gender.Male.name)))
    lexicon.addMapping(WordPerson(buildHuman("Mary", "", Gender.Female.name)))
}

class WordPerson(val human: Concept, word: String = human.valueName(HumanFields.FIRST_NAME)?:"unknown"): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            // FIXME not sure about defaulting to ""
            slot(HumanFields.FIRST_NAME, human.valueName(HumanFields.FIRST_NAME) ?: "")
            lastName(HumanFields.LAST_NAME)
            //slot(Human.LAST_NAME, human.valueName(Human.LAST_NAME) ?: "")
            slot(HumanFields.GENDER, human.valueName(HumanFields.GENDER)?: "")
            checkCharacter(CoreFields.INSTANCE.fieldName)
        }
        return lexicalConcept.demons
    }
}

fun LexicalConceptBuilder.lastName(slotName: Fields, variableName: String? = null) {
    val variableSlot = root.createVariable(slotName, variableName)
    concept.with(variableSlot)
    val demon = LastNameDemon(root.wordContext) {
        if (it != null) {
            root.completeVariable(variableSlot, it, root.wordContext, this.episodicConcept)
        }
    }
    root.addDemon(demon)
}

class LastNameDemon(wordContext: WordContext, val action: (Concept?) -> Unit): Demon(wordContext) {
    override fun run() {
        val matcher = matchConceptByHead(InDepthUnderstandingConcepts.UnknownWord.name)
        searchContext(matcher, matchNever(), direction = SearchDirection.After, distance = 1, wordContext = wordContext) {
            it.value?.let {
                val lastName = it.value("word")
                active = false
                action(lastName)
            }
        }
    }
    override fun description(): String {
        return "If an unknown word immediately follows,\nThen assume it is a character's last name\nand update character information."
    }
}