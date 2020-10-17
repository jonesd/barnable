package info.dgjones.au.narrative

import info.dgjones.au.concept.*
import info.dgjones.au.domain.general.*
import info.dgjones.au.grammar.*
import info.dgjones.au.parser.*

fun buildInDepthUnderstandingLexicon(): Lexicon {
    val lexicon = Lexicon()
    buildEnglishGrammarLexicon(lexicon)
    buildGeneralDomainLexicon(lexicon)

    buildNarrativeRelationshipLexicon(lexicon)

    lexicon.addMapping(WordPickUp())
    lexicon.addMapping(WordIgnore(EntryWord("up")))
    lexicon.addMapping(WordBall())
    lexicon.addMapping(WordDrop())
    lexicon.addMapping(WordBox())

    // Titles
    lexicon.addMapping(TitleWord("Mr", Gender.Male))
    lexicon.addMapping(TitleWord("Mr.", Gender.Male))
    lexicon.addMapping(TitleWord("Mrs", Gender.Female))
    lexicon.addMapping(TitleWord("Mrs.", Gender.Female))
    lexicon.addMapping(TitleWord("Miss", Gender.Female))
    lexicon.addMapping(TitleWord("Ms", Gender.Female))
    lexicon.addMapping(TitleWord("Ms.", Gender.Female))

    lexicon.addMapping(WordGive())

    lexicon.addMapping(WordWas())

    lexicon.addMapping(WordTell())
    // FIXME not sure whether should ignore that - its a subordinate conjunction and should link
    // the Mtrans from "told" to the Ingest of "eats"
    lexicon.addMapping(WordIgnore(EntryWord("that")))
    lexicon.addMapping(WordEats())

    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Female.name), "woman"))
    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Male.name), "man"))

    // Modifiers
    lexicon.addMapping(ModifierWord("red", "colour"))
    lexicon.addMapping(ModifierWord("black", "colour"))
    lexicon.addMapping(ModifierWord("fat", "weight", "GT-NORM"))
    lexicon.addMapping(ModifierWord("thin", "weight", "LT-NORM"))
    lexicon.addMapping(ModifierWord("old", "age", "GT-NORM"))
    lexicon.addMapping(ModifierWord("young", "age", "LT-NORM"))

    // Locations
    lexicon.addMapping(WordHome())

    lexicon.addMapping(WordGo())
    lexicon.addMapping(WordKiss())
    lexicon.addMapping(WordKick())
    lexicon.addMapping(WordHungry())
    lexicon.addMapping(WordWalk())
    lexicon.addMapping(WordKnock())
    lexicon.addMapping(WordPour())

    lexicon.addMapping(WordLunch())

    // Divorce2

    // Disambiguate
    lexicon.addMapping(WordMeasureObject())

    // FIXME lexicon.addMapping(WordWas())

    lexicon.addMapping(WordAnother())

    // FIXME only for QA
    lexicon.addMapping(WordWho())

    return lexicon
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

enum class BodyParts {
    Legs,
    Fingers,
    Foot
}
enum class Force {
    Gravity
}

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

// FIXME how to define this? force
val gravity = Concept("gravity")

class WordBall: WordHandler(EntryWord("ball")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = buildPhysicalObject(PhysicalObjectKind.GameObject.name, word.word)
        return listOf(SaveObjectDemon(wordContext))
    }
}

class WordBox: WordHandler(EntryWord("box")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value =  buildPhysicalObject(PhysicalObjectKind.Container.name, word.word)
        return listOf()
    }
}

class WordHome: WordHandler(EntryWord("home")) {
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
        return concept.valueName(HumanFields.FIRST_NAME)
    }
}

//FIXME should use pick and calculate picked...
class WordPickUp: WordHandler(EntryWord("picked", listOf("picked", "up"))/*.and("picked")*/) {
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

class WordPour: WordHandler(EntryWord("pour")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "GRASP") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", variableName = "thing", headValue = PhysicalObjectKind.Liquid.name)
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
}

class WordDrop: WordHandler(EntryWord("drop")) {
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
class WordKnock: WordHandler(EntryWord("knock")) {
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

class WordKick: WordHandler(EntryWord("kick")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "PROPEL") {
            expectActor("actor", variableName = "actor")
            expectThing("thing", variableName = "thing", headValues = listOf(InDepthUnderstandingConcepts.PhysicalObject.name))
            expectPrep("to", preps = setOf(Preposition.To), matcher = matchConceptByHead(setOf(
                InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
            )
            slot("instr", "MOVE") {
                slot("actor", BodyParts.Foot.name)
                varReference("thing", "thing")
                slot("kind", InDepthUnderstandingConcepts.Act.name)
            }
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordGive: WordHandler(EntryWord("give").and("gave")) {
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

class WordKiss: WordHandler(EntryWord("kiss")) {
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

class WordTell: WordHandler(EntryWord("tell").past("told")) {
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

class WordGo: WordHandler(EntryWord("go").past("went")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, "PTRANS") {
            expectHead("actor", variableName = "actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("to", headValue = "Location")
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordWalk: WordHandler(EntryWord("walk")) {
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

class WordHungry: WordHandler(EntryWord("hungry")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = Concept(SatisfactionGoal.`S-Hunger`.name)
            .with(Slot("kind", Concept(InDepthUnderstandingConcepts.Goal.name)))
        return listOf()
    }
}

// InDepth
class WordLunch: WordHandler(EntryWord("lunch")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, MopMeal.MopMeal.name) {
            expectHead(MopMealFields.EATER_A.fieldName, headValue = "Human", direction = SearchDirection.Before)
            expectPrep(MopMealFields.EATER_B.fieldName, preps = listOf(Preposition.With), matcher= matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
            slot(CoreFields.Event, MopMeal.EventEatMeal.name) // FIXME find associated event
            checkMop(CoreFields.INSTANCE.fieldName)
            // FIXME slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

//FIXME differentiate between eat food vs eat meal?
class WordEats: WordHandler(EntryWord("eat")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, Acts.INGEST.name) {
            expectHead("actor", headValue = "Human", direction = SearchDirection.Before)
            expectHead("thing", headValue = PhysicalObjectKind.Food.name)
            slot("kind", "Act")
        }
        return lexicalConcept.demons
    }
}

class WordMeasureObject: WordHandler(EntryWord("measure")) {
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
            slot(HumanFields.FIRST_NAME, "")
            slot(HumanFields.LAST_NAME, "")
            slot(HumanFields.GENDER, Gender.Male.name)
        }
        return lexicalConcept.demons
    }
}

class TitleWord(word: String, val gender: Gender): WordHandler(EntryWord(word, noSuffix = true)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.FIRST_NAME, "")
            lastName(HumanFields.LAST_NAME)
            slot(HumanFields.GENDER, gender.name)
            // FIXME include title
            checkCharacter(CoreFields.INSTANCE.fieldName)
        }
        return lexicalConcept.demons
    }
    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(listOf(InDepthUnderstandingConcepts.UnknownWord.name)), SearchDirection.After, 1, wordContext, disambiguationHandler)
        )
    }
}

/*
Verb

was

    first-person singular simple past indicative of be.

        I was castigated and scorned.

    third-person singular simple past indicative of be.

    It was a really humongous slice of cake.

(now colloquial) Used in phrases with existential there when the semantic subject is (usually third-person) plural.

    There was three of them there.

(now colloquial or nonstandard) second-person singular simple past indicative of be.
(colloquial, nonstandard) first-person plural simple past indicative of be

https://en.wiktionary.org/wiki/was#English
*/
class WordWas : WordHandler(EntryWord("was")) {
    override fun build(wordContext: WordContext): List<Demon> {
        wordContext.defHolder.value = Concept("word")
        wordContext.defHolder.addFlag(ParserFlags.Ignore)
        val matcher = matchAny(
            listOf(
                matchConceptByKind(InDepthUnderstandingConcepts.Act.name),
                matchConceptValueName(GrammarFields.Aspect, Aspect.Progressive.name)
            )
        )
        val wasDisambiguation = InsertAfterDemon(matcher, wordContext) {
            it.value?.let { concept ->
                if (concept.valueName(GrammarFields.Aspect) == Aspect.Progressive.name) {
                    // Do nothing
                } else if (concept.valueName(TimeFields.TIME) == TimeConcepts.Past.name) {
                    // FIXME should Past also match yesterday, etc....
                    wordContext.def()?.with(Slot(GrammarFields.Voice, Concept(Voice.Passive.name)))
                } else {
                    concept.with(Slot(TimeFields.TIME, Concept(TimeConcepts.Past.name)))
                }
            }
        }
        return listOf(wasDisambiguation)
    }
}

// InDepth p150/5.4, p304
class WordAnother: WordHandler(EntryWord("another")) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.GENDER, Gender.Female.name)
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