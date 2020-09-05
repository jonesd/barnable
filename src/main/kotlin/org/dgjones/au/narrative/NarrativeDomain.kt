package org.dgjones.au.narrative

import org.dgjones.au.domain.general.buildGeneralDomainLexicon
import org.dgjones.au.parser.*

fun buildInDepthUnderstandingLexicon(): Lexicon {
    val lexicon = Lexicon()
    buildEnglishGrammarLexicon(lexicon)
    buildGeneralDomainLexicon(lexicon)
    buildNumberLexicon(lexicon)

    lexicon.addMapping(WordPerson(buildHuman("John", "", Gender.Male)))
    lexicon.addMapping(WordPickUp())
    lexicon.addMapping(WordIgnore(EntryWord("up")))
    lexicon.addMapping(WordBall())
    lexicon.addMapping(WordDrop())
    lexicon.addMapping(WordIn())
    lexicon.addMapping(WordBox())

    lexicon.addMapping(WordPerson(buildHuman("Mary", "", Gender.Female)))
    lexicon.addMapping(WordGive())
    lexicon.addMapping(WordBook())
    lexicon.addMapping(WordTree())

    lexicon.addMapping(WordPerson(buildHuman("Fred", "", Gender.Male)))
    lexicon.addMapping(WordTell())
    // FIXME not sure whether should ignore that - its a subordinate conjunction and should link
    // the Mtrans from "told" to the Ingest of "eats"
    lexicon.addMapping(WordIgnore(EntryWord("that")))
    lexicon.addMapping(WordEats())

    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Female), "woman"))
    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Female), "man"))

    // Modifiers
    lexicon.addMapping(ModifierWord("red", "colour"))
    lexicon.addMapping(ModifierWord("black", "colour"))
    lexicon.addMapping(ModifierWord("fat", "weight", "GT-NORM"))
    lexicon.addMapping(ModifierWord("thin", "weight", "LT-NORM"))
    lexicon.addMapping(ModifierWord("old", "age", "GT-NORM"))
    lexicon.addMapping(ModifierWord("young", "age", "LT-NORM"))

    lexicon.addMapping(WordYesterday())

    // pronoun
    lexicon.addMapping(WordPerson(buildHuman("Anne", "", Gender.Female)))
    lexicon.addMapping(PronounWord("he", Gender.Male))
    lexicon.addMapping(PronounWord("she", Gender.Female))
    lexicon.addMapping(WordHome())
    lexicon.addMapping(WordGo())
    lexicon.addMapping(WordKiss())
    lexicon.addMapping(WordPerson(buildHuman("Bill", "", Gender.Male)))
    lexicon.addMapping(WordHungry())
    lexicon.addMapping(WordWalk())
    lexicon.addMapping(WordKnock())

    lexicon.addMapping(WordLunch())

    // Divorce2
    lexicon.addMapping(WordPerson(buildHuman("George", "", Gender.Male)))

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
enum class Acts {
    ATRANS, // Transfer of Posession
    PROPEL, // Application of a physical Force
    PTRANS, // Transfer of physical location
    INGEST, // When an organism takes something from outside its environment and makes it internal
    EXPEL, // Opposite of INGEST
    MTRANS, // Transfer of mental information from one person to another
    MBUILD, // Thought process which create new conceptualizations from old ones
    MOVE, // Movement of a bodypart of some animate organism
    GRASP, //physical contacting an object
    SPEAK, // any vocalization
    ATTEND // directing a sense organ
}

enum class InDepthUnderstandingConcepts {
    Act,
    Human,
    PhysicalObject,
    Setting,
    Location,
    Goal,
    Plan
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

fun buildATrans(actor: Concept, thing: Concept, from: Concept, to: Concept): Concept {
    return Concept(Acts.ATRANS.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("from", from))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}
fun buildGrasp(actor: Concept, thing: Concept): Concept {
    return Concept(Acts.GRASP.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("instr", buildMove(actor, Concept(BodyParts.Fingers.name), thing)))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}
fun buildMove(actor: Concept, thing: Concept, to: Concept? = null): Concept {
    return Concept(Acts.MOVE.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}
fun buildIngest(actor: Concept, thing: Concept): Concept {
    return Concept(Acts.INGEST.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}
fun buildMTrans(actor: Concept, thing: Concept, from: Concept, to: Concept): Concept {
    return Concept(Acts.MTRANS.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("from", from))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}
fun buildPropel(actor: Concept, thing: Concept): Concept {
    return Concept(Acts.PROPEL.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}
fun buildPTrans(actor: Concept, thing: Concept?, to: Concept?, instr: Concept?): Concept {
    return Concept(Acts.PTRANS.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("to", to))
        .with(Slot("instr", instr))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
}
fun buildAttend(actor: Concept, thing: Concept?, to: Concept?): Concept {
    return Concept(Acts.ATTEND.name)
        .with(Slot("actor", actor))
        .with(Slot("thing", thing))
        .with(Slot("to", to))
        .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Act.name)))
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



class LoadCharacterDemon(val human: Concept, wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        if (!wordContext.isDefSet()) {
            val firstName = human.valueName("firstName", "missing")
            var character: Concept? = wordContext.context.workingMemory.findCharacter(firstName)
            if (character == null && wordContext.context.qaMode) {
                // FIXME try and get away from the qaMode test here
                throw NoMentionOfCharacter(firstName)
            }
            if (character == null) {
                character = human
                wordContext.context.workingMemory.addCharacter(human)
            }
            wordContext.defHolder.value = character
            println("Loaded character - $character")
            active = false
        }
    }
}

open class ConceptAccessor(val concept: Concept) {

}

class Human(concept: Concept): ConceptAccessor(concept) {
    fun isCompatible(): Boolean {
        return concept.name == InDepthUnderstandingConcepts.Human.name
    }
    fun firstName(): String? {
        return concept.valueName("firstName")
    }
}

class SaveCharacterDemon(wordContext: WordContext): Demon(wordContext){
    override fun run() {
        val character = wordContext.def()
        if (character != null && Human(character).isCompatible()) {
            wordContext.context.mostRecentCharacter = character
            if (wordContext.context.localCharacter != null) {
                wordContext.context.localCharacter = character
            }
            active = false
        } else {
            println("SaveCharacter failed as def = $character")
        }
    }

    override fun description(): String {
        return "SaveCharacter ${wordContext.def()}"
    }
}

class SaveObjectDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val o = wordContext.def()
        if (o != null) {
            wordContext.context.mostRecentObject = o
            active = false
        }
    }

    override fun description(): String {
        return "SaveObject def=${wordContext.def()}"
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

class InsertAfterDemon(val matcher: ConceptMatcher, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, matchNever(), direction = SearchDirection.After, wordContext = wordContext) {
            if (it.value != null) {
                active = false
                action(it)
            }
        }
    }

    override fun description(): String {
        return "InsertAfter $matcher"
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
            DisambiguateUsingMatch(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext, disambiguationHandler),
            DisambiguateUsingMatch(matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name), SearchDirection.After, wordContext, disambiguationHandler)
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

