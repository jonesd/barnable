
fun buildInDepthUnderstandingLexicon(): Lexicon {
    val lexicon = Lexicon();
    lexicon.addMapping(WordPerson(Human("John", "", Gender.Male)))
    lexicon.addMapping(WordPicked())
    lexicon.addMapping(WordIgnore("up"))
    lexicon.addMapping(WordIgnore("the"))
    lexicon.addMapping(WordBall())
    lexicon.addMapping(WordAnd())
    lexicon.addMapping(WordDropped())
    lexicon.addMapping(WordIt())
    lexicon.addMapping(WordIn())
    lexicon.addMapping(WordBox())

    lexicon.addMapping(WordPerson(Human("Mary", "", Gender.Female)))
    lexicon.addMapping(WordGave())
    lexicon.addMapping(WordIgnore("a"))
    lexicon.addMapping(WordBook())

    lexicon.addMapping(WordPerson(Human("Fred", "", Gender.Male)))
    lexicon.addMapping(WordTold())
    // FIXME not sure whether should ignore that - its a subordinate conjnuction and should link
    // the Mtrans from "told" to the Ingest of "eats"
    lexicon.addMapping(WordIgnore("that"))
    lexicon.addMapping(WordEats())
    lexicon.addMapping(WordLobster())

    lexicon.addMapping(WordPerson(Human("", "", Gender.Female), "woman"))
    lexicon.addMapping(WordPerson(Human("", "", Gender.Female), "man"))

    // Modifiers
    lexicon.addMapping(ModifierWord("red", "colour"))
    lexicon.addMapping(ModifierWord("black", "colour"))
    lexicon.addMapping(ModifierWord("fat", "weight", "GT-NORM"))
    lexicon.addMapping(ModifierWord("thin", "weight", "LT-NORM"))
    lexicon.addMapping(ModifierWord("old", "age", "GT-NORM"))
    lexicon.addMapping(ModifierWord("young", "age", "LT-NORM"))

    // pronoun
    lexicon.addMapping(WordPerson(Human("Anne", "", Gender.Female)))
    lexicon.addMapping(PronounWord("he", Gender.Male))
    lexicon.addMapping(PronounWord("He", Gender.Male))
    lexicon.addMapping(WordHome())
    lexicon.addMapping(WordWent())
    lexicon.addMapping(WordKiss())

    // FIXME only for QA
    lexicon.addMapping(WordWho())

    return lexicon
}

enum class Gender {
    Male,
    Female,
    Other
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
    Setting
}

open class Act(val act: Acts): Concept(act.toString()) {
    init {
        kinds.add(InDepthUnderstandingConcepts.Act.name)
        kinds.add(act.name)
    }
}
class ActAtrans(val actor: Human, val obj: PhysicalObject, val from: Human,val to: Human): Act(Acts.ATRANS)
class ActGrasp(val actor: Human, val obj: PhysicalObject): Act(Acts.GRASP) {
    val instr = ActMove(actor, Concept("fingers"), obj)
}
//FIXME obj could be FOOD?
class ActIngest(val actor: Human, val obj: Concept): Act(Acts.INGEST)
class ActMove(val actor: Human, val obj: Concept, val to: Concept): Act(Acts.MOVE)
class ActMtrans(val actor: Human, val obj: Concept, val from: Human, val to: Human): Act(Acts.MTRANS)
class ActPropel(val actor: Concept, val obj: Concept): Act(Acts.PROPEL)
class ActPtrans(val actor: Human, val thing: Concept?, val to: Concept?, val instr: Act?): Act(Acts.PTRANS)
class ActAttend(val actor: Human, val obj: Concept?, val to: Concept?): Act(Acts.ATTEND)
class Human(val firstName: String, val lastName: String = "", val gender: Gender): Concept(firstName) {
    init {
        kinds.add(InDepthUnderstandingConcepts.Human.name)
    }
}

enum class PhysicalObjectKind() {
    Container,
    GameObject,
    Book,
    Food,
    Location,
    BodyPart
}

// FIXME how to define this? force
val gravity = Concept("gravity")

open class PhysicalObject(kind: PhysicalObjectKind, name: String): Concept(name) {
    init {
        kinds.add(InDepthUnderstandingConcepts.PhysicalObject.name)
        kinds.add(kind.name)
    }
}

class WordBall(): WordHandler("ball") {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = PhysicalObject(PhysicalObjectKind.GameObject, "ball")
        return listOf(SaveObjectDemon(wordContext))
    }
}

class WordBox(): WordHandler("box") {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = PhysicalObject(PhysicalObjectKind.Container, "box")
        return listOf()
    }
}

class WordHome(): WordHandler("home") {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = Location("Home", "home")
        return listOf()
    }
}

// Exercise 1
class WordPerson(val human: Human, word: String = human.firstName.toLowerCase()): WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        // Fixme - not sure about the load/reuse
        return listOf(LoadCharacterDemon(human, wordContext), SaveCharacterDemon(wordContext))
    }
}

class WordBook(): WordHandler("book") {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = PhysicalObject(PhysicalObjectKind.GameObject, "book")
        return listOf(SaveObjectDemon(wordContext))
    }
}

class Food(foodKind: String, name: String): PhysicalObject(PhysicalObjectKind.Food, name) {
    init {
        kinds.add(foodKind)
    }
}

class Location(locationKind: String, name: String): PhysicalObject(PhysicalObjectKind.Location, name) {
    init {
        kinds.add(locationKind)
    }
}

class WordLobster(): WordHandler("lobster") {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = Food("Lobster", "lobster")
        return listOf(SaveObjectDemon(wordContext))
    }
}

class LoadCharacterDemon(val human: Human, wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        if (!wordContext.isDefSet()) {
            var character: Human? = wordContext.context.workingMemory.findCharacter(human.firstName)
            if (character == null && wordContext.context.qaMode) {
                // FIXME try and get away from the qaMode test here
                throw NoMentionOfCharacter(human.firstName)
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

class SaveCharacterDemon(wordContext: WordContext): Demon(wordContext){
    override fun run() {
        val character = wordContext.def()
        if (character is Human) {
            wordContext.context.mostRecentCharacter = character
            if (wordContext.context.localCharacter != null) {
                wordContext.context.localCharacter = character
            }
            active = false
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

class WordPicked(): WordHandler("picked") {
    override fun build(wordContext: WordContext): List<Demon> {
        val nextWordIsUp = "up".equals(wordContext.context.nextWord)
        val pickUp = object : Demon(wordContext) {
            var humanHolder: ConceptHolder? = null
            var objectHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet() || !nextWordIsUp) {
                    active = false
                } else {
                    val humanConcept = humanHolder?.value as? Human
                    val objectConcept = objectHolder?.value as? PhysicalObject
                    if (humanConcept != null && objectConcept != null) {
                        humanHolder?.addFlag(ParserFlags.Inside)
                        objectHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActGrasp(humanConcept, objectConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            pickUp.humanHolder = it
        }
        val objectAfter = ExpectDemon(matchConceptByClass<PhysicalObject>(), SearchDirection.After, wordContext) {
            pickUp.objectHolder = it
        }

        return listOf(pickUp, humanBefore, objectAfter)
    }
}

class WordDropped(): WordHandler("dropped") {
    override fun build(wordContext: WordContext): List<Demon> {
        val dropped = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var thingHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value as? Human
                    val thingConcept = thingHolder?.value
                    val toConcept = toHolder?.value
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        thingHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActPtrans(actorConcept, thingConcept, toConcept, ActPropel(gravity, thingConcept))
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            dropped.actorHolder = it
        }
        val thing = ExpectDemon(matchConceptByClass<PhysicalObject>(), SearchDirection.After, wordContext) {
            dropped.thingHolder = it
        }
        val matchers = matchAll(listOf(
            matchPrepIn(setOf(Preposition.In.name, Preposition.Into.name, Preposition.On.name)),
            matchConceptByKind(setOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
        ))
        var to = PrepDemon(matchers, SearchDirection.After, wordContext) {
            dropped.toHolder = it
        }

        return listOf(dropped, humanBefore, thing, to)
    }
}

class WordIn(): WordHandler("in") {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = Prep(Preposition.In.name)

        val matcher = matchConceptByKind(setOf(InDepthUnderstandingConcepts.PhysicalObject.name, InDepthUnderstandingConcepts.Setting.name))
        val addPrepObj = InsertAfterDemon(matcher, wordContext) {
            if (wordContext.isDefSet()) {
                wordContext.defHolder.addFlag(ParserFlags.Inside)
                it.value?.prepobj = wordContext.defHolder.value as? Prep
                println("Updated with prepobj concept=${it}")
            }
        }
        return listOf(addPrepObj)
    }
}

class InsertAfterDemon(val matcher: (Concept?) -> Boolean, wordContext: WordContext, val action: (ConceptHolder) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, matchNever(), SearchDirection.After, wordContext) {
            if (it.value != null) {
                active = false
                action(it)
            }
        }
    }
}

class WordGave(): WordHandler("gave") {
    override fun build(wordContext: WordContext): List<Demon> {
        val gave = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var thingHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value as? Human
                    val thingConcept = thingHolder?.value as? PhysicalObject
                    val toConcept = toHolder?.value as? Human
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        thingHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActAtrans(actorConcept, thingConcept, actorConcept, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            gave.actorHolder = it
        }
        val thing = ExpectDemon(matchConceptByClass<PhysicalObject>(), SearchDirection.After, wordContext) {
            gave.thingHolder = it
        }
        val humanAfter = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.After, wordContext) {
            gave.toHolder = it
        }

        return listOf(gave, humanBefore, thing, humanAfter)
    }
}

//FIXME kiss & kissed
class WordKiss(): WordHandler("kissed") {
    override fun build(wordContext: WordContext): List<Demon> {
        val kiss = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value as? Human
                    val toConcept = toHolder?.value as? Human
                    if (actorConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        // FIXME better representation....
                        val lips = PhysicalObject(PhysicalObjectKind.BodyPart, "lips")
                        wordContext.defHolder.value = ActAttend(actorConcept, lips, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            kiss.actorHolder = it
        }
        val humanAfter = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.After, wordContext) {
            kiss.toHolder = it
        }

        return listOf(kiss, humanBefore, humanAfter)
    }
}

class WordTold(): WordHandler("told") {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var thingHolder: ConceptHolder? = null
            var toHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value as? Human
                    val thingConcept = thingHolder?.value
                    val toConcept = toHolder?.value as? Human
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        thingHolder?.addFlag(ParserFlags.Inside)
                        toHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActMtrans(actorConcept, thingConcept, actorConcept, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            demon.actorHolder = it
        }
        // fIXME unsure of this, really the "that" word to find the linked act?
        val actAfter = ExpectDemon(matchConceptByKind(InDepthUnderstandingConcepts.Act.name), SearchDirection.After, wordContext) {
            demon.thingHolder = it
        }
        val humanAfter = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.After, wordContext) {
            demon.toHolder = it
        }

        return listOf(demon, humanBefore, actAfter, humanAfter)
    }
}

// FIXME really GO and WENT is past
class WordWent(): WordHandler("went") {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var locationHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value as? Human
                    val locationConcept = locationHolder?.value as? PhysicalObject
                    if (actorConcept != null && locationConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        locationHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActPtrans(actorConcept, null, locationConcept, null)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            demon.actorHolder = it
        }
        // FIXME should this be a LOCATION
        val locationAfter = ExpectDemon(matchConceptByClass<PhysicalObject>(), SearchDirection.After, wordContext) {
            demon.locationHolder = it
        }

        return listOf(demon, humanBefore, locationAfter)
    }
}

class WordEats(): WordHandler("eats") {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actorHolder: ConceptHolder? = null
            var foodHolder: ConceptHolder? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actorHolder?.value as? Human
                    val thingConcept = foodHolder?.value as? PhysicalObject
                    if (actorConcept != null && thingConcept != null) {
                        actorHolder?.addFlag(ParserFlags.Inside)
                        foodHolder?.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActIngest(actorConcept, thingConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            demon.actorHolder = it
        }
        val food = ExpectDemon(matchConceptByKind(PhysicalObjectKind.Food.name), SearchDirection.After, wordContext) {
            demon.foodHolder = it
        }

        return listOf(demon, humanBefore, food)
    }
}

class ModifierWord(word: String, val modifier: String, val value: String = word): WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var thingHolder: ConceptHolder? = null

            override fun run() {
                val thingConcept = thingHolder?.value
                if (thingConcept != null) {
                    thingConcept.addModifier(modifier, value)
                    active = false
                }
            }
        }
        //FIXME list of kinds is not complete
        val thingDemon = ExpectDemon(matchConceptByKind(listOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name)), SearchDirection.After, wordContext) {
            demon.thingHolder = it
        }
        return listOf(demon, thingDemon)
    }
}

class WordMan(word: String): WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        if (!wordContext.isDefSet()) {
            wordContext.defHolder.value = Human("", "", Gender.Male)
        }
        return listOf()
    }
}

class PronounWord(word: String, val genderMatch: Gender): WordHandler(word) {
    override fun build(wordContext: WordContext): List<Demon> {
        // FIXME partial implementation - also why not use demon
        val localHuman = wordContext.context.localCharacter
        if (localHuman != null && localHuman.gender == genderMatch) {
            wordContext.defHolder.value = localHuman
        } else {
            val mostRecentHuman = wordContext.context.mostRecentCharacter
            if (mostRecentHuman != null && mostRecentHuman.gender == genderMatch) {
                wordContext.defHolder.value = mostRecentHuman
            }
        }
        return listOf()
    }
}

class WordWho(): WordHandler("who") {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(WhoDemon(wordContext));
    }
}

class WhoDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        //FIXME not sure what to use for placeholder
        wordContext.defHolder.value = Human("who", "who", Gender.Male)
    }
}