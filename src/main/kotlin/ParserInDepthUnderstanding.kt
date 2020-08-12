
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
class ActPtrans(val actor: Human, val thing: Concept, val to: Concept, propelActor: Concept): Act(Acts.PTRANS) {
    val instr = ActPropel(propelActor, thing)
}

class Human(val firstName: String, val lastName: String = "", val gender: Gender): Concept(firstName) {
    init {
        kinds.add(InDepthUnderstandingConcepts.Human.name)
    }
}

enum class PhysicalObjectKind() {
    Container,
    GameObject,
    Book,
    Food
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

// Exercise 1
class WordPerson(val human: Human): WordHandler(human.firstName.toLowerCase()) {
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
            var human: Human? = null
            var obj: PhysicalObject? = null

            override fun run() {
                if (wordContext.isDefSet() || !nextWordIsUp) {
                    active = false
                } else {
                    val humanConcept = human
                    val objectConcept = obj
                    if (humanConcept != null && objectConcept != null) {
                        humanConcept.addFlag(ParserFlags.Inside)
                        objectConcept.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActGrasp(humanConcept, objectConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            pickUp.human = it as? Human
        }
        val objectAfter = ExpectDemon(matchConceptByClass<PhysicalObject>(), SearchDirection.After, wordContext) {
            pickUp.obj = it as? PhysicalObject
        }

        return listOf(pickUp, humanBefore, objectAfter)
    }
}

class WordDropped(): WordHandler("dropped") {
    override fun build(wordContext: WordContext): List<Demon> {
        val dropped = object : Demon(wordContext) {
            var actor: Human? = null
            var thing: PhysicalObject? = null
            var to: Concept? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actor
                    val thingConcept = thing
                    val toConcept = to
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorConcept.addFlag(ParserFlags.Inside)
                        thingConcept.addFlag(ParserFlags.Inside)
                        toConcept.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActPtrans(actorConcept, thingConcept, toConcept, gravity)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            dropped.actor = it as? Human
        }
        val thing = ExpectDemon(matchConceptByClass<PhysicalObject>(), SearchDirection.After, wordContext) {
            dropped.thing = it as? PhysicalObject
        }
        val matchers = matchAll(listOf(
            matchPrepIn(setOf(Preposition.In.name, Preposition.Into.name, Preposition.On.name)),
            matchConceptByKind(setOf(InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
        ))
        var to = PrepDemon(matchers, SearchDirection.After, wordContext) {
            dropped.to = it
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
                wordContext.def()?.addFlag(ParserFlags.Inside)
                it.prepobj = wordContext.def() as? Prep
                println("Updated with prepobj concept=${it}")
            }
        }
        return listOf(addPrepObj)
    }
}

class InsertAfterDemon(val matcher: (Concept?) -> Boolean, wordContext: WordContext, val action: (Concept) -> Unit): Demon(wordContext) {
    override fun run() {
        searchContext(matcher, matchNever(), SearchDirection.After, wordContext) {
            if (it != null) {
                active = false
                action(it)
            }
        }
    }
}

class WordGave(): WordHandler("gave") {
    override fun build(wordContext: WordContext): List<Demon> {
        val gave = object : Demon(wordContext) {
            var actor: Human? = null
            var thing: PhysicalObject? = null
            var to: Human? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actor
                    val thingConcept = thing
                    val toConcept = to
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorConcept.addFlag(ParserFlags.Inside)
                        thingConcept.addFlag(ParserFlags.Inside)
                        toConcept.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActAtrans(actorConcept, thingConcept, actorConcept, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            gave.actor = it as? Human
        }
        val thing = ExpectDemon(matchConceptByClass<PhysicalObject>(), SearchDirection.After, wordContext) {
            gave.thing = it as? PhysicalObject
        }
        val humanAfter = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.After, wordContext) {
            gave.to = it as? Human
        }

        return listOf(gave, humanBefore, thing, humanAfter)
    }
}

class WordTold(): WordHandler("told") {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actor: Human? = null
            var thing: Concept? = null
            var to: Human? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actor
                    val thingConcept = thing
                    val toConcept = to
                    if (actorConcept != null && thingConcept != null && toConcept != null) {
                        actorConcept.addFlag(ParserFlags.Inside)
                        thingConcept.addFlag(ParserFlags.Inside)
                        toConcept.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActMtrans(actorConcept, thingConcept, actorConcept, toConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            demon.actor = it as? Human
        }
        // fIXME unsure of this, really the "that" word to find the linked act?
        val actAfter = ExpectDemon(matchConceptByKind(InDepthUnderstandingConcepts.Act.name), SearchDirection.After, wordContext) {
            demon.thing = it
        }
        val humanAfter = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.After, wordContext) {
            demon.to = it as? Human
        }

        return listOf(demon, humanBefore, actAfter, humanAfter)
    }
}

class WordEats(): WordHandler("eats") {
    override fun build(wordContext: WordContext): List<Demon> {
        val demon = object : Demon(wordContext) {
            var actor: Human? = null
            var food: PhysicalObject? = null

            override fun run() {
                if (wordContext.isDefSet()) {
                    active = false
                } else {
                    val actorConcept = actor
                    val thingConcept = food
                    if (actorConcept != null && thingConcept != null) {
                        actorConcept.addFlag(ParserFlags.Inside)
                        thingConcept.addFlag(ParserFlags.Inside)
                        wordContext.defHolder.value = ActIngest(actorConcept, thingConcept)
                        active = false
                    }
                }
            }
        }
        val humanBefore = ExpectDemon(matchConceptByClass<Human>(), SearchDirection.Before, wordContext) {
            demon.actor = it as? Human
        }
        val food = ExpectDemon(matchConceptByKind(PhysicalObjectKind.Food.name), SearchDirection.After, wordContext) {
            demon.food = it as? PhysicalObject
        }

        return listOf(demon, humanBefore, food)
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