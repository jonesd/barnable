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
    buildInDepthUnderstandingLexicon(lexicon)
    return lexicon
}

fun buildInDepthUnderstandingLexicon(lexicon: Lexicon) {
    buildGeneralDomainLexicon(lexicon)
    buildNarrativeRelationshipLexicon(lexicon)

    lexicon.addMapping(WordPickUp())
    lexicon.addMapping(WordIgnore(EntryWord("up")))
    lexicon.addMapping(WordDrop())

    lexicon.addMapping(WordGive())

    lexicon.addMapping(WordWas())

    lexicon.addMapping(WordTell())
    lexicon.addMapping(WordIgnore(EntryWord("that")))
    lexicon.addMapping(WordEats())

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

    // FIXME only for QA
    lexicon.addMapping(WordWho())
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
        return concept.name == GeneralConcepts.Human.name
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
            instrumentByActorToThing(Acts.MOVE, BodyParts.Fingers.name)
            slot(CoreFields.Kind.fieldName, GeneralConcepts.Act.name)
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
            instrumentByActorToThing(Acts.MOVE, BodyParts.Fingers.name)
            slot(CoreFields.Kind.fieldName, GeneralConcepts.Act.name)
        }.demons
}

class WordDrop: WordHandler(EntryWord("drop")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PTRANS.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing")
            varReference(ActFields.From.fieldName, "actor")
            expectPrep(ActFields.To.fieldName, preps = setOf(Preposition.In, Preposition.Into, Preposition.On, Preposition.Upon), matcher = matchConceptByHead(setOf(
                GeneralConcepts.Human.name, GeneralConcepts.PhysicalObject.name))
            )
            slot(ActFields.Instrument.fieldName, Acts.PROPEL.name) {
                slot(ActFields.Actor.fieldName, Force.Gravity.name)
                varReference(ActFields.Thing.fieldName, "thing")
                slot(CoreFields.Kind.fieldName, GeneralConcepts.Act.name)
            }
            slot(CoreFields.Kind.fieldName, GeneralConcepts.Act.name)
        }.demons
}

// InDepth pp307
class WordKnock: WordHandler(EntryWord("knock")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PROPEL.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing")
            expectPrep("to", preps = setOf(Preposition.On), matcher = matchConceptByHead(setOf(
                GeneralConcepts.Human.name, GeneralConcepts.PhysicalObject.name))
            )
            instrumentByActorToThing(Acts.MOVE, BodyParts.Fingers.name)
            slot(CoreFields.Kind.fieldName, GeneralConcepts.Act.name)
        }.demons
}

class WordKick: WordHandler(EntryWord("kick")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PROPEL.name) {
            expectActor(variableName = "actor")
            expectThing(variableName = "thing")
            expectPrep("to", preps = setOf(Preposition.To), matcher = matchConceptByHead(setOf(
                GeneralConcepts.Human.name, GeneralConcepts.PhysicalObject.name))
            )
            slot(ActFields.Instrument.fieldName, Acts.MOVE.name) {
                slot(ActFields.Actor.fieldName, BodyParts.Foot.name)
                varReference(ActFields.Thing.fieldName, "thing")
                slot(CoreFields.Kind, GeneralConcepts.Act.name)
            }
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
        }.demons
}

class WordGive: WordHandler(EntryWord("give")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.ATRANS.name) {
            expectActor(variableName = "actor")
            expectThing()
            varReference(ActFields.From.fieldName, "actor")
            expectHead(ActFields.To.fieldName, headValue = GeneralConcepts.Human.name)
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
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
                slot(CoreFields.Kind, GeneralConcepts.Act.name)
            }
            expectHead(ActFields.To.fieldName, headValue = GeneralConcepts.Human.name)
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
        }.demons
}

class WordTell: WordHandler(EntryWord("tell").past("told")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.MTRANS.name) {
            expectActor(variableName = "actor")
            expectThing(matcher = matchConceptByKind(listOf(GeneralConcepts.Act.name, GeneralConcepts.Goal.name, GeneralConcepts.Plan.name)))
            varReference(ActFields.From.fieldName, "actor")
            expectHead(ActFields.To.fieldName, headValue = GeneralConcepts.Human.name)
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
        }.demons
}

class WordGo: WordHandler(EntryWord("go").past("went")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PTRANS.name) {
            expectActor()
            expectHead(ActFields.To.fieldName, headValues = listOf(GeneralConcepts.Location.name, GeneralConcepts.Setting.name))
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
        }.demons
}

class WordWalk: WordHandler(EntryWord("walk")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.PTRANS.name) {
            expectActor(variableName = "actor")
            expectHead(ActFields.To.fieldName, headValues = listOf(GeneralConcepts.Location.name, GeneralConcepts.Setting.name))
            slot(ActFields.Instrument.fieldName, Acts.MOVE.name) {
                varReference(ActFields.Actor.fieldName, "actor")
                slot(ActFields.Thing.fieldName, BodyParts.Legs.name)
                slot(CoreFields.Kind, GeneralConcepts.Act.name)
            }
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
        }.demons
}

class WordHungry: WordHandler(EntryWord("hungry")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, SatisfactionGoal.HungerSatisfactionGoal.name) {
            slot(CoreFields.Kind, GeneralConcepts.Goal.name)
        }.demons
}

class WordLunch: WordHandler(EntryWord("lunch")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, MopMeal.MopMeal.name) {
            expectHead(MopMealFields.EATER_A.fieldName, headValue = "Human", direction = SearchDirection.Before)
            expectPrep(MopMealFields.EATER_B.fieldName, preps = listOf(Preposition.With), matcher= matchConceptByHead(
                GeneralConcepts.Human.name))
            slot(CoreFields.Event, MopMeal.EventEatMeal.name)
            checkMop(CoreFields.Instance.fieldName)
        }.demons
}

class WordEats: WordHandler(EntryWord("eat")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.INGEST.name) {
            expectActor()
            expectThing(matcher = matchConceptByKind(listOf(PhysicalObjectKind.Food.name)))
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
        }.demons
}

/**
 * The act of measuring an object.
 * For example: George measures the tree
 */
class WordMeasureObject: WordHandler(EntryWord("measure")) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, Acts.ATRANS.name) {
            expectActor(variableName = "actor")
            expectThing()
            varReference(ActFields.From.fieldName, "actor")
            slot(CoreFields.Kind, GeneralConcepts.Act.name)
        }.demons

    override fun disambiguationDemons(wordContext: WordContext, disambiguationHandler: DisambiguationHandler): List<Demon> {
        return listOf(
            DisambiguateUsingMatch(matchConceptByHead(GeneralConcepts.Human.name), SearchDirection.Before, null, false, wordContext, disambiguationHandler),
            DisambiguateUsingMatch(matchConceptByHead(GeneralConcepts.PhysicalObject.name), SearchDirection.After, null, false, wordContext, disambiguationHandler)
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
                matchConceptByKind(GeneralConcepts.Act.name),
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
