/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.narrative

import info.dgjones.barnable.concept.*
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.grammar.*
import info.dgjones.barnable.parser.*

fun buildInDepthUnderstandingLexicon(): Lexicon {
    val lexicon = Lexicon()
    buildEnglishGrammarLexicon(lexicon)
    buildGeneralDomainLexicon(lexicon)

    buildNarrativeRelationshipLexicon(lexicon)

    lexicon.addMapping(WordPickUp())
    lexicon.addMapping(WordIgnore(EntryWord("up")))
    lexicon.addMapping(WordDrop())

    lexicon.addMapping(WordGive())

    lexicon.addMapping(WordWas())

    lexicon.addMapping(WordTell())
    // FIXME not sure whether should ignore that - its a subordinate conjunction and should link
    // the Mtrans from "told" to the Ingest of "eats"
    lexicon.addMapping(WordIgnore(EntryWord("that")))
    lexicon.addMapping(WordEats())

    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Female.name), "woman"))
    lexicon.addMapping(WordPerson(buildHuman("", "", Gender.Male.name), "man"))

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
    Foot,
    Lips
}
enum class Force {
    Gravity
}

// FIXME how to define this? force
val gravity = Concept("gravity")

class WordHome: WordHandler(EntryWord("home")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, "Location") {
            slot("type", "Residence")
            slot("name", "Home")
        }.demons
}

open class ConceptAccessor(val concept: Concept)

class HumanAccessor(concept: Concept): ConceptAccessor(concept) {
    fun isCompatible(): Boolean {
        return concept.name == InDepthUnderstandingConcepts.Human.name
    }
    fun firstName(): String? {
        return concept.valueName(HumanFields.FirstName)
    }
}

class WordPickUp: WordHandler(EntryWord("pick", listOf("pick", "up"))) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.GRASP.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing")
            slot(ActFields.Instrument.fieldName, Acts.MOVE.name) {
                varReference(ActFields.Actor.fieldName, "actor")
                slot(ActFields.Thing.fieldName, BodyParts.Fingers.name)
                varReference(ActFields.To.fieldName, "thing")
                slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
            }
            slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
        }.demons

    fun description(): String {
        return "Pick Up"
    }
}

class WordPour: WordHandler(EntryWord("pour")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.GRASP.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing", matcher = matchConceptByKind(PhysicalObjectKind.Liquid.name))
            slot(ActFields.Instrument.fieldName, Acts.MOVE.name) {
                varReference(ActFields.Actor.fieldName, "actor")
                slot(ActFields.Thing.fieldName, BodyParts.Fingers.name)
                varReference(ActFields.To.fieldName, "thing")
                slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
            }
            slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordDrop: WordHandler(EntryWord("drop")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PTRANS.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing")
            varReference(ActFields.From.fieldName, "actor")
            expectPrep(ActFields.To.fieldName, preps = setOf(Preposition.In, Preposition.Into, Preposition.On), matcher = matchConceptByHead(setOf(
                InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
            )
            slot(ActFields.Instrument.fieldName, Acts.PROPEL.name) {
                slot(ActFields.Actor.fieldName, Force.Gravity.name)
                varReference(ActFields.Thing.fieldName, "thing")
                slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
            }
            slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

// InDepth pp307
class WordKnock: WordHandler(EntryWord("knock")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PROPEL.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing")
            expectPrep("to", preps = setOf(Preposition.On), matcher = matchConceptByHead(setOf(
                InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
            )
            slot(ActFields.Instrument.fieldName, Acts.PROPEL.name) {
                slot(ActFields.Actor.fieldName, Force.Gravity.name)
                varReference(ActFields.Thing.fieldName, "thing")
                slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
            }
            slot(CoreFields.Kind.fieldName, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordKick: WordHandler(EntryWord("kick")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PROPEL.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing")
            expectPrep("to", preps = setOf(Preposition.To), matcher = matchConceptByHead(setOf(
                InDepthUnderstandingConcepts.Human.name, InDepthUnderstandingConcepts.PhysicalObject.name))
            )
            slot(ActFields.Instrument.fieldName, Acts.MOVE.name) {
                slot(ActFields.Actor.fieldName, BodyParts.Foot.name)
                varReference(ActFields.Thing.fieldName, "thing")
                slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
            }
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordGive: WordHandler(EntryWord("give")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.ATRANS.name) {
            expectActor(variableName = "actor")
            expectThing()
            varReference(ActFields.From.fieldName, "actor")
            expectHead(ActFields.To.fieldName, headValue = InDepthUnderstandingConcepts.Human.name)
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordKiss: WordHandler(EntryWord("kiss")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.ATTEND.name) {
            expectActor()
            expectThing()
            slot(ActFields.Instrument.fieldName, Acts.MOVE.name) {
                slot(ActFields.Actor.fieldName, BodyParts.Lips.name)
                slot(ActFields.Thing.fieldName, PhysicalObjectKind.BodyPart.name)
                slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
            }
            expectHead(ActFields.To.fieldName, headValue = InDepthUnderstandingConcepts.Human.name)
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordTell: WordHandler(EntryWord("tell").past("told")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.MTRANS.name) {
            expectActor(variableName = "actor")
            expectThing(matcher = matchConceptByKind(listOf(InDepthUnderstandingConcepts.Act.name, InDepthUnderstandingConcepts.Goal.name, InDepthUnderstandingConcepts.Plan.name)))
            varReference(ActFields.From.fieldName, "actor")
            expectHead(ActFields.To.fieldName, headValue = InDepthUnderstandingConcepts.Human.name)
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordGo: WordHandler(EntryWord("go").past("went")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PTRANS.name) {
            expectActor()
            expectHead(ActFields.To.fieldName, headValue = InDepthUnderstandingConcepts.Location.name)
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordWalk: WordHandler(EntryWord("walk")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PTRANS.name) {
            expectActor(variableName = "actor")
            expectHead(ActFields.To.fieldName, headValue = InDepthUnderstandingConcepts.Location.name)
            slot(ActFields.Instrument.fieldName, Acts.MOVE.name) {
                varReference(ActFields.Actor.fieldName, "actor")
                slot(ActFields.Thing.fieldName, BodyParts.Legs.name)
                slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
            }
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordHungry: WordHandler(EntryWord("hungry")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, SatisfactionGoal.`S-Hunger`.name) {
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Goal.name)
        }.demons
}

// InDepth
class WordLunch: WordHandler(EntryWord("lunch")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, MopMeal.MopMeal.name) {
            expectHead(MopMealFields.EATER_A.fieldName, headValue = "Human", direction = SearchDirection.Before)
            expectPrep(MopMealFields.EATER_B.fieldName, preps = listOf(Preposition.With), matcher= matchConceptByHead(InDepthUnderstandingConcepts.Human.name))
            slot(CoreFields.Event, MopMeal.EventEatMeal.name) // FIXME find associated event
            checkMop(CoreFields.Instance.fieldName)
        }.demons
}

//FIXME differentiate between eat food vs eat meal?
class WordEats: WordHandler(EntryWord("eat")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.INGEST.name) {
            expectActor()
            expectThing(matcher = matchConceptByKind(listOf(PhysicalObjectKind.Food.name)))
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons
}

class WordMeasureObject: WordHandler(EntryWord("measure")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.ATRANS.name) {
            expectActor(variableName = "actor")
            expectThing()
            varReference(ActFields.From.fieldName, "actor")
            slot(CoreFields.Kind, InDepthUnderstandingConcepts.Act.name)
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(InDepthUnderstandingConcepts.Human.name), SearchDirection.Before, null, wordContext, disambiguationHandler),
            DisambiguateUsingMatch(matchConceptByHead(InDepthUnderstandingConcepts.PhysicalObject.name), SearchDirection.After, null, wordContext, disambiguationHandler)
        )
    }
}

// FIXME not sure what this is?
class WordMan(word: String): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.FirstName, "")
            slot(HumanFields.LastName, "")
            slot(HumanFields.Gender, Gender.Male.name)
        }.demons
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
                when {
                    concept.valueName(GrammarFields.Aspect) == Aspect.Progressive.name -> {
                        // Do nothing
                    }
                    concept.valueName(TimeFields.TIME) == TimeConcepts.Past.name -> {
                        // FIXME should Past also match yesterday, etc....
                        wordContext.def()?.with(Slot(GrammarFields.Voice, Concept(Voice.Passive.name)))
                    }
                    else -> {
                        concept.with(Slot(TimeFields.TIME, Concept(TimeConcepts.Past.name)))
                    }
                }
            }
        }
        return listOf(wasDisambiguation)
    }
}

// InDepth p150/5.4, p304
class WordAnother: WordHandler(EntryWord("another")) {
    //FIXME implement this?
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(HumanFields.Gender, Gender.Female.name)
            slot(Relationships.Name, Marriage.Concept.fieldName) {
                possessiveRef(Marriage.Husband, gender = Gender.Male)
                nextChar("wife", relRole = "Wife")
                checkRelationship(CoreFields.Instance, waitForSlots = listOf("husband", "wife"))
            }
            innerInstan(CoreFields.Instance, observeSlot = "wife")
        }.demons
}
