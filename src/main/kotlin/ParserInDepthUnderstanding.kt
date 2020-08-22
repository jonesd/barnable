
fun buildInDepthUnderstandingLexicon(): Lexicon {
    val lexicon = Lexicon();
    lexicon.addMapping(WordPerson(buildHuman("John", "", Gender.Male)))
    lexicon.addMapping(WordPick())
    lexicon.addMapping(WordIgnore(EntryWord("up")))
    lexicon.addMapping(WordIgnore(EntryWord("the")))
    lexicon.addMapping(WordBall())
    lexicon.addMapping(WordAnd())
    lexicon.addMapping(WordDrop())
    lexicon.addMapping(WordIt())
    lexicon.addMapping(WordIn())
    lexicon.addMapping(WordBox())

    lexicon.addMapping(WordPerson(buildHuman("Mary", "", Gender.Female)))
    lexicon.addMapping(WordGive())
    lexicon.addMapping(WordIgnore(EntryWord("a").and("an")))
    lexicon.addMapping(WordBook())

    lexicon.addMapping(WordPerson(buildHuman("Fred", "", Gender.Male)))
    lexicon.addMapping(WordTell())
    // FIXME not sure whether should ignore that - its a subordinate conjunction and should link
    // the Mtrans from "told" to the Ingest of "eats"
    lexicon.addMapping(WordIgnore(EntryWord("that")))
    lexicon.addMapping(WordEats())
    lexicon.addMapping(WordLobster())

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

    // FIXME only for QA
    lexicon.addMapping(WordWho())

    return lexicon
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
fun buildIngest(actor: Concept, thing:Concept): Concept {
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
    Location,
    BodyPart
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
        //FIXME not sure if this model matches box/ball structure
        wordContext.defHolder.value = Concept("Location")
            .with(Slot("Type", Concept("Residence")))
            .with(Slot("name", Concept("Home")))
        return listOf()
    }
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

enum class FoodKind {
    Lobster
}

fun buildFood(kindOfFood: String, name: String = kindOfFood): Concept {
    return Concept(PhysicalObjectKind.Food.name)
        .with(Slot("kind", Concept(kindOfFood)))
        .with(Slot("name", Concept(name)))
}

class WordLobster(): WordHandler(EntryWord("lobster")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildFood(FoodKind.Lobster.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
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
}

class SaveObjectDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val o = wordContext.def()
        if (o != null) {
            wordContext.context.mostRecentObject = o
            active = false
        }
    }
}

class WordPick(): WordHandler(EntryWord("pick").and("picked")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val nextWordIsUp = "up".equals(wordContext.context.nextWord)
        val pickUp = object : Demon(wordContext) {
            var humanHolder: ConceptHolder? = null
            var objectHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet() || !nextWordIsUp) {
                    active = false
                } else {
                    val humanConcept = humanHolder?.value
                    val objectConcept = objectHolder?.value
                    if (humanConcept != null && objectConcept != null) {
                        humanHolder?.addFlag(ParserFlags.Inside)
                        objectHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = buildGrasp(humanConcept, objectConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            pickUp.humanHolder = it
        }
        val objectAfter = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name), SearchDirection.After, wordContext) {
            pickUp.objectHolder = it
        }

        return listOf(pickUp, humanBefore, objectAfter)
    }
}

class WordDrop(): WordHandler(EntryWord("drop").and("dropped")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val dropped = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var thingHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value
                    val thingConcept = thingHolder?.value
                    val toConcept = toHolder?.value
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        thingHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = buildPTrans(actorConcept, thingConcept, toConcept, buildPropel(gravity, thingConcept))
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            dropped.actorHolder = it
        }
        val thing = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name), SearchDirection.After, wordContext) {
            dropped.thingHolder = it
        }
        val matchers = matchAll(listOf(
            matchPrepIn(setOf(Preposition.In.name, Preposition.Into.name, Preposition.On.name)),
            matchConceptByHead(setOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
        ))
        var to = PrepDemon(matchers, SearchDirection.After, wordContext) {
            dropped.toHolder = it
        }

        return listOf(dropped, humanBefore, thing, to)
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
        searchContext(matcher, matchNever(), SearchDirection.After, wordContext) {
            if (it.value != null) {
                active = false
                action(it)
            }
        }
    }
}

class WordGive(): WordHandler(EntryWord("give").and("gave")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val gave = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var thingHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value
                    val thingConcept = thingHolder?.value
                    val toConcept = toHolder?.value
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        thingHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = buildATrans(actorConcept, thingConcept, actorConcept, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            gave.actorHolder = it
        }
        val thing = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name), SearchDirection.After, wordContext) {
            gave.thingHolder = it
        }
        val humanAfter = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.After, wordContext) {
            gave.toHolder = it
        }

        return listOf(gave, humanBefore, thing, humanAfter)
    }
}

class WordKiss(): WordHandler(EntryWord("kiss").and("kissing").and("kissed")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val kiss = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value
                    val toConcept = toHolder?.value
                    if (actorConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        // FIXME better representation....
                        val lips = buildPhysicalObject(PhysicalObjectKind.BodyPart.name, "lips")
                        wordContext.defHolder.value = buildAttend(actorConcept, lips, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            kiss.actorHolder = it
        }
        val humanAfter = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.After, wordContext) {
            kiss.toHolder = it
        }

        return listOf(kiss, humanBefore, humanAfter)
    }
}

class WordTell(): WordHandler(EntryWord("tell").past("told")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var thingHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value
                    val thingConcept = thingHolder?.value
                    val toConcept = toHolder?.value
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        thingHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = buildMTrans(actorConcept, thingConcept, actorConcept, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            demon.actorHolder = it
        }
        // fIXME unsure of this, really the "that" word to find the linked act?
        val actAfter = ExpectDemon(matchConceptByKind(listOf(InDepthUnderstandingConcepts.Act.name, InDepthUnderstandingConcepts.Goal.name, InDepthUnderstandingConcepts.Plan.name)), SearchDirection.After, wordContext) {
            demon.thingHolder = it
        }
        val humanAfter = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.After, wordContext) {
            demon.toHolder = it
        }

        return listOf(demon, humanBefore, actAfter, humanAfter)
    }
}

class EntryWord(val word: String) {
    val pastWords = mutableListOf<String>()
    val extras = mutableListOf<String>()

    fun entries() = listOf(listOf(word), pastWords, extras).flatten()

    fun past(word: String): EntryWord {
        pastWords.add(word)
        return this
    }
    fun and(word: String): EntryWord {
        extras.add(word)
        return this
    }
}

class WordGo(): WordHandler(EntryWord("go").past("went")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var locationHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value
                    val locationConcept = locationHolder?.value
                    if (actorConcept != null && locationConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        locationHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = buildPTrans(actorConcept, null, locationConcept, null)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            demon.actorHolder = it
        }
        // FIXME should this be a LOCATION (Was physical location)
        val locationAfter = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Location.name), SearchDirection.After, wordContext) {
            demon.locationHolder = it
        }

        return listOf(demon, humanBefore, locationAfter)
    }
}

class WordWalk(): WordHandler(EntryWord("walk").past("walked")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var locationHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value
                    val locationConcept = locationHolder?.value
                    if (actorConcept != null && locationConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        locationHolder?.addFlag(ParserFlags.Inside)
                        val instr = buildMove(actorConcept, Concept(BodyParts.Legs.name))
                        wordContext.defHolder.value = buildPTrans(actorConcept, null, locationConcept, instr)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            demon.actorHolder = it
        }
        // FIXME should this be a LOCATION (Was physical location)
        val locationAfter = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Location.name), SearchDirection.After, wordContext) {
            demon.locationHolder = it
        }

        return listOf(demon, humanBefore, locationAfter)
    }
}

class WordHungry(): WordHandler(EntryWord("hungry")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = Concept(SatisfactionGoal.`S-Hunger`.name)
            .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Goal.name)))
        return listOf()
    }
}

class WordEats(): WordHandler(EntryWord("eats")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var foodHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value
                    val thingConcept = foodHolder?.value
                    if (actorConcept != null && thingConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        foodHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = buildIngest(actorConcept, thingConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, wordContext) {
            demon.actorHolder = it
        }
        val food = ExpectDemon(matchConceptByHead(PhysicalObjectKind.Food.name), SearchDirection.After, wordContext) {
            demon.foodHolder = it
        }

        return listOf(demon, humanBefore, food)
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
        if (!wordContext.isDefSet()) {
            wordContext.defHolder.value = buildHuman("", "", Gender.Male)
        }
        return listOf()
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

class WordWho(): WordHandler(EntryWord("who")) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(WhoDemon(wordContext));
    }
}

class WhoDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        //FIXME not sure what to use for placeholder
        wordContext.defHolder.value = buildHuman("who", "who", Gender.Male)
    }
}