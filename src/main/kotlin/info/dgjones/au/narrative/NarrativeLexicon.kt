package info.dgjones.au.narrative

import info.dgjones.au.domain.general.buildGeneralDomainLexicon
import info.dgjones.au.parser.*

fun buildInDepthUnderstandingLexicon(): Lexicon {
    val lexicon = Lexicon()
    buildEnglishGrammarLexicon(lexicon)
    buildGeneralDomainLexicon(lexicon)
    buildNumberLexicon(lexicon)

    lexicon.addMapping(WordPerson(buildHuman("John", "", Gender.Male.name)))
    lexicon.addMapping(WordPickUp())
    lexicon.addMapping(WordIgnore(EntryWord("up")))
    lexicon.addMapping(WordBall())
    lexicon.addMapping(WordDrop())
    lexicon.addMapping(WordIn())
    lexicon.addMapping(WordBox())

    // Titles
    lexicon.addMapping(TitleWord("Mr", Gender.Male))
    lexicon.addMapping(TitleWord("Mr.", Gender.Male))
    lexicon.addMapping(TitleWord("Mrs", Gender.Female))
    lexicon.addMapping(TitleWord("Mrs.", Gender.Female))
    lexicon.addMapping(TitleWord("Miss", Gender.Female))
    lexicon.addMapping(TitleWord("Ms", Gender.Female))
    lexicon.addMapping(TitleWord("Ms.", Gender.Female))

    lexicon.addMapping(WordPerson(buildHuman("Mary", "", Gender.Female.name)))
    lexicon.addMapping(WordGive())
    lexicon.addMapping(WordBook())
    lexicon.addMapping(WordTree())

    lexicon.addMapping(WordPerson(buildHuman("Fred", "", Gender.Male.name)))
    lexicon.addMapping(WordTell())
    // FIXME not sure whether should ignore that - its a subordinate conjunction and should link
    // the Mtrans from "told" to the Ingest of "eats"
    lexicon.addMapping(WordIgnore(EntryWord("that")))
    lexicon.addMapping(WordEats())

    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Female.name), "woman"))
    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Female.name), "man"))
    lexicon.addMapping(WordPronounPossessive("her", Gender.Female))
    lexicon.addMapping(WordPronounPossessive("his", Gender.Male))

    // Modifiers
    lexicon.addMapping(ModifierWord("red", "colour"))
    lexicon.addMapping(ModifierWord("black", "colour"))
    lexicon.addMapping(ModifierWord("fat", "weight", "GT-NORM"))
    lexicon.addMapping(ModifierWord("thin", "weight", "LT-NORM"))
    lexicon.addMapping(ModifierWord("old", "age", "GT-NORM"))
    lexicon.addMapping(ModifierWord("young", "age", "LT-NORM"))

    lexicon.addMapping(WordYesterday())

    // pronoun
    lexicon.addMapping(WordPerson(buildHuman("Ann", "", Gender.Female.name)))
    lexicon.addMapping(WordPerson(buildHuman("Anne", "", Gender.Female.name)))
    lexicon.addMapping(PronounWord("he", Gender.Male))
    lexicon.addMapping(PronounWord("she", Gender.Female))

    lexicon.addMapping(WordHusband())
    lexicon.addMapping(WordWife())

    // Locations
    lexicon.addMapping(WordHome())

    lexicon.addMapping(WordGo())
    lexicon.addMapping(WordKiss())
    lexicon.addMapping(WordPerson(buildHuman("Bill", "", Gender.Male.name)))
    lexicon.addMapping(WordHungry())
    lexicon.addMapping(WordWalk())
    lexicon.addMapping(WordKnock())

    lexicon.addMapping(WordLunch())

    // Divorce2
    lexicon.addMapping(WordPerson(buildHuman("George", "", Gender.Male.name)))

    // Disambiguate
    lexicon.addMapping(WordMeasureQuantity())
    lexicon.addMapping(WordMeasureObject())

    // FIXME only for QA
    lexicon.addMapping(WordWho())

    return lexicon
}

enum class SatisfactionGoal {
    `S-Sex`,
    `S-Hunger`,
    `S-Thirst`
}
enum class DeltaGoal {
    `D-Know`,
    `D-Proximity`,
    `D-ControlSomeone`,
    `D-ControlSomething`
}
enum class EntertainmentGoal {
    `E-Company`,
    `E-Travel`,
    `E-Exercise`
}
enum class AchievementGoal {
    `A-Good-Job`,
    `A-Skill`
}
enum class PreservationGoal {
    `P-Health`,
    `P-Comfort`,
    `P-Appearance`,
    `P-Finances`
}

enum class InDepthUnderstandingConcepts {
    Act,
    Human,
    PhysicalObject,
    Setting,
    Location,
    Goal,
    Plan,
    Ref,
    UnknownWord
}

enum class TimeConcepts {
    Yesterday,
    Tomorrow,
    Afternoon,
    Morning,
    Evening,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday
}
enum class BodyParts {
    Legs,
    Fingers
}
enum class Force {
    Gravity
}

enum class PhysicalObjectKind() {
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

// FIXME how to define this? force
val gravity = Concept("gravity")

class WordBall(): WordHandler(EntryWord("ball")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildPhysicalObject(PhysicalObjectKind.GameObject.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}

class WordBox(): WordHandler(EntryWord("box")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildPhysicalObject(PhysicalObjectKind.Container.name, word.word)
        return listOf()
    }
}

class WordHome(): WordHandler(EntryWord("home")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "Location") {
            slot("type", "Residence")
            slot("name", "Home")
        }
        return lexicalConcept.demons
    }
}

open class ConceptAccessor(val concept: Concept)

class HumanAccessor(concept: Concept): ConceptAccessor(concept) {
    fun isCompatible(): Boolean {
        return concept.name == InDepthUnderstandingConcepts.Human.name
    }
    fun firstName(): String? {
        return concept.valueName("firstName")
    }
}

//FIXME should use pick and calculate picked...
class WordPickUp(): WordHandler(EntryWord("picked", listOf("picked", "up"))/*.and("picked")*/) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "GRASP") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", variableName = "thing", headValue = InDepthUnderstandingConcepts.PhysicalObject.name)
            slot("instr", "MOVE") {
                varReference("actor", "actor")
                slot("thing", BodyParts.Fingers.name)
                varReference("to", "thing")
                slot("kind", InDepthUnderstandingConcepts.Act.name)
            }
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
    fun description(): String {
        return "Picked Up"
    }
}

class WordDrop(): WordHandler(EntryWord("drop").and("dropped")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "PTRANS") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", variableName = "thing", headValue = InDepthUnderstandingConcepts.PhysicalObject.name)
            varReference("from", "actor")
            expectPrep("to", preps = setOf(Preposition.In, Preposition.Into, Preposition.On), matcher = matchConceptByHead(setOf(
                InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
            )
            slot("instr", "PROPEL") {
                slot("actor", Force.Gravity.name)
                varReference("thing", "thing")
                slot("kind", InDepthUnderstandingConcepts.Act.name)
            }
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

// InDepth pp307
class WordKnock(): WordHandler(EntryWord("knock")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "PROPEL") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", variableName = "thing", headValue = InDepthUnderstandingConcepts.PhysicalObject.name)
            expectPrep("to", preps = setOf(Preposition.On), matcher = matchConceptByHead(setOf(
                InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
            )
            slot("instr", "PROPEL") {
                slot("actor", Force.Gravity.name)
                varReference("thing", "thing")
                slot("kind", InDepthUnderstandingConcepts.Act.name)
            }
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordIn(): WordHandler(EntryWord("in")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildPrep(Preposition.In.name)

        val matcher = matchConceptByHead(setOf(InDepthUnderstandingConcepts.PhysicalObject.name, InDepthUnderstandingConcepts.Setting.name))
        val addPrepObj = InsertAfterDemon(matcher, wordContext) {
            if (wordContext.isDefSet()) {
                val itValue = it.value
                val holderValue = wordContext.defHolder.value
                if (itValue != null && holderValue != null) {
                    withPrepObj(itValue, holderValue)
                    wordContext.defHolder.addFlag(ParserFlags.Inside)
                    println("Updated with prepobj concept=${it}")
                }
            }
        }
        return listOf(addPrepObj)
    }
}

class WordGive(): WordHandler(EntryWord("give").and("gave")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "ATRANS") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", headValue = InDepthUnderstandingConcepts.PhysicalObject.name)
            varReference("from", "actor")
            expectHead("to", headValue = "Human")
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordKiss(): WordHandler(EntryWord("kiss")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "ATTEND") {
            expectHead("actor", headValue = "Human", direction = SearchDirection.Before)
            slot("thing", PhysicalObjectKind.PhysicalObject.name) {
                slot("kind", PhysicalObjectKind.BodyPart.name)
                slot("name", "lips")
            }
            expectHead("to", headValue = "Human")
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordTell(): WordHandler(EntryWord("tell").past("told")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "MTRANS") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectKind("thing", kinds = listOf(InDepthUnderstandingConcepts.Act.name, InDepthUnderstandingConcepts.Goal.name, InDepthUnderstandingConcepts.Plan.name))
            varReference("from", "actor")
            expectHead("to", headValue = "Human")
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordGo(): WordHandler(EntryWord("go").past("went")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "PTRANS") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("to", headValue = "Location")
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordWalk(): WordHandler(EntryWord("walk")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "PTRANS") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("to", headValue = "Location")
            slot("instr", "MOVE") {
                varReference("actor", "actor")
                slot("thing", BodyParts.Legs.name)
                slot("kind", "Act")
            }
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordHungry(): WordHandler(EntryWord("hungry")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = Concept(SatisfactionGoal.`S-Hunger`.name)
            .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Goal.name)))
        return listOf()
    }
}

// InDepth
class WordLunch(): WordHandler(EntryWord("lunch")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "M-Meal") {
            expectHead("eater-a", headValue = "Human", direction = SearchDirection.Before)
            expectPrep("eater-b", preps = listOf(Preposition.With), matcher=matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
            slot("event", "EV-LUNCH") // FIXME find associated event

            // FIXMEslot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordEats(): WordHandler(EntryWord("eats")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, Acts.INGEST.name) {
            expectHead("actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", headValue = PhysicalObjectKind.Food.name)
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordMeasureQuantity() : WordHandler(EntryWord("measure")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "Quantity") {
            // FIXME also support "a measure of ..." = default to 1 unit
            expectHead("amount", headValue = "number", direction = SearchDirection.Before)
            slot("unit", "measure")
            expectHead("of", headValues = listOf(PhysicalObjectKind.Liquid.name, PhysicalObjectKind.Food.name))
        }
        return lexicalConcept.demons
    }

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingWord("of", matchConceptByHead(listOf(PhysicalObjectKind.Food.name, PhysicalObjectKind.Liquid.name)), SearchDirection.After, wordContext, disambiguationHandler)
        )
    }
}

class WordMeasureObject(): WordHandler(EntryWord("measure")) {
    override fun build(wordContext: WordContext): List<Demon> {
        // FIXME not sure how to model this...
        val lexicalConcept = lexicalConcept(wordContext, "ATRANS") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", headValue = InDepthUnderstandingConcepts.PhysicalObject.name)
            varReference("from", "actor")
            //expectHead("to", headValue = "Human")
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, null, wordContext, disambiguationHandler),
            DisambiguateUsingMatch(matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name), SearchDirection.After, null, wordContext, disambiguationHandler)
        )
    }
}

class WordYesterday(): WordHandler(EntryWord("yesterday")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actConcept = actHolder?.value
                    if (actConcept != null) {
                        wordContext.defHolder.value = Concept(TimeConcepts.Yesterday.name)
                        wordContext.defHolder.addFlag(ParserFlags.Inside)
                        actConcept.with(Slot("time", wordContext.def()))
                        active = false
                    }
                }
            }

            override fun description(): String {
                return "Yesterday"
            }
        }
        val actDemon = ExpectDemon(matchConceptByKind(InDepthUnderstandingConcepts.Act.name), SearchDirection.Before, wordContext) {
            demon.actHolder = it
        }

        return listOf(demon, actDemon)
    }
}

class ModifierWord(word: String, val modifier: String, val value: String = word): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var thingHolder: ConceptHolder? = null

            override fun run() {
                val thingConcept = thingHolder?.value
                if (thingConcept != null) {
                    thingConcept.with(Slot(modifier, Concept(value)))
                    //FIXME remove thingConcept.addModifier(modifier, value)
                    active = false
                }
            }

            override fun description(): String {
                return "ModifierWord $value"
            }
        }
        //FIXME list of kinds is not complete
        val thingDemon = ExpectDemon(matchConceptByHead(listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)), SearchDirection.After, wordContext) {
            demon.thingHolder = it
        }
        return listOf(demon, thingDemon)
    }
}

class WordPronounPossessive(word: String, val gender: Gender): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Ref.name) {
            ignoreHolder()
            slot("case", Case.Possessive.name)
            slot("gender", gender.name)
            findCharacter(CoreFields.INSTANCE.fieldName)
        }
        return lexicalConcept.demons
    }
}

class WordMan(word: String): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot("firstName", "")
            slot("lastName", "")
            slot("gender", Gender.Male.name)
        }
        return lexicalConcept.demons
    }
}

class PronounWord(word: String, val genderMatch: Gender): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        // FIXME partial implementation - also why not use demon
        wordContext.defHolder.addFlag(ParserFlags.Ignore)
        val localHuman = wordContext.context.localCharacter
        if (localHuman != null && localHuman.valueName("gender") == genderMatch.name) {
            wordContext.defHolder.value = localHuman
        } else {
            val mostRecentHuman = wordContext.context.mostRecentCharacter
            if (mostRecentHuman != null && mostRecentHuman.valueName("gender") == genderMatch.name) {
                wordContext.defHolder.value = mostRecentHuman
            }
        }
        if (!wordContext.isDefSet()) {
            val mostRecentHuman = wordContext.context.workingMemory.charactersRecent.firstOrNull { it.valueName("gender") == genderMatch.name }
            if (mostRecentHuman != null) {
                wordContext.defHolder.value = mostRecentHuman
            }
        }
        return listOf()
    }
}

class WordWife(): WordHandler(EntryWord("wife")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(Human.GENDER, Gender.Female.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Husband, gender = Gender.Male)
                nextChar("wife", relRole = "Wife")
                checkRelationship(CoreFields.INSTANCE, waitForSlots = listOf("husband", "wife"))
            }
            innerInstan("instan", observeSlot = "wife")
        }
        return lexicalConcept.demons
    }
}

class WordHusband(): WordHandler(EntryWord("husband")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(Human.GENDER, Gender.Male.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Wife, gender = Gender.Female)
                nextChar("husband", relRole = "Husband")
                checkRelationship(CoreFields.INSTANCE, waitForSlots = listOf("husband", "wife"))
            }
            innerInstan("instan", observeSlot = "husband")
        }
        return lexicalConcept.demons
    }
}

class TitleWord(word: String, val gender: Gender): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(Human.FIRST_NAME, "")
            lastName(Human.LAST_NAME)
            slot(Human.GENDER, gender.name)
            // FIXME include title
            checkCharacter(CoreFields.INSTANCE.fieldName)
        }
        return lexicalConcept.demons
        // Fixme - not sure about the load/reuse
        // FIXMEreturn listOf(LoadCharacterDemon(human, wordContext), SaveCharacterDemon(wordContext))
    }
    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(listOf(InDepthUnderstandingConcepts.UnknownWord.name)), SearchDirection.After, 1, wordContext, disambiguationHandler)
        )
    }
}