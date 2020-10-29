package info.dgjones.barnable.narrative

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NarrativeDomainTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Example execution`() {
        val textProcessor = runTextProcess("John picked up the ball and dropped it in the box", lexicon)

        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val grasp = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.GRASP.name, grasp.name)
        assertEquals("John", grasp.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("ball", grasp.value(ActFields.Thing)?.valueName(CoreFields.Name))
        val actGraspInstr = grasp.value("instr")!!
        // FIXME move structural element not present, rely on name
        assertEquals(Acts.MOVE.name, actGraspInstr.name)
        assertEquals("John", actGraspInstr.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("ball", actGraspInstr.value(ActFields.To)?.valueName(CoreFields.Name))

        val ptrans = textProcessor.workingMemory.concepts[1]
        assertEquals(Acts.PTRANS.name, ptrans.name)
        assertEquals("John", ptrans.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        // FIXME thing should be obj
        assertEquals("ball", ptrans.value(ActFields.Thing)?.valueName(CoreFields.Name))
        assertEquals("box", ptrans.value(ActFields.To)?.valueName(CoreFields.Name))
        val ptransInstrument = ptrans.value(ActFields.Instrument)
        assertEquals("Gravity", ptransInstrument?.value(ActFields.Actor)?.name)
    }

    @Test
    fun `Exercise 1 John gave Mary a book`() {
        val textProcessor = runTextProcess("John gave Mary a book", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val atrans = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.ATRANS.name, atrans.name)
        assertEquals("John", atrans.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("book", atrans.value(ActFields.Thing)?.valueName(CoreFields.Name))
        assertEquals("Mary", atrans.value(ActFields.To)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("John", atrans.value(ActFields.From)?.valueName(HumanFields.FIRST_NAME))
    }

    @Test
    fun `Exercise 2 Fred told Mary that John eats lobster`() {
        val textProcessor = runTextProcess("Fred told Mary that John eats lobster", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val mtrans = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.MTRANS.name, mtrans.name)
        assertEquals("Fred", mtrans.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Fred", mtrans.value(ActFields.From)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Mary", mtrans.value(ActFields.To)?.valueName(HumanFields.FIRST_NAME))
        val ingest = mtrans.value("thing")!!
        //FIXME this what the book has:
        // assertEquals("Fred", ingest.actor.firstName)
        // this seems correct:
        assertEquals("John", ingest.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("lobster", ingest.value(ActFields.Thing)?.valueName(CoreFields.Name))
    }

    @Test
    fun `Basic pronoun reference`() {
        val textProcessor = runTextProcess("John went home. He kissed his wife Anne.", lexicon)

        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val travel = textProcessor.workingMemory.concepts[0]
        assertEquals("John", travel.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Home", travel.value(ActFields.To)?.valueName(CoreFields.Name))

        val kissAttend = textProcessor.workingMemory.concepts[1]
        assertEquals(Acts.ATTEND.name, kissAttend.name)
        assertEquals("John0", kissAttend.value(ActFields.Actor)?.valueName(CoreFields.INSTANCE))
        assertEquals(Gender.Male.name, kissAttend.value(ActFields.Actor)?.valueName(HumanFields.GENDER))
        assertEquals("Anne0", kissAttend.value(ActFields.To)?.valueName(CoreFields.INSTANCE))
        assertEquals(Gender.Female.name, kissAttend.value(ActFields.To)?.valueName(HumanFields.GENDER))

        //FIXME more assertions
    }

    @Test
    fun `Basic pronoun reference 2`() {
        val textProcessor = runTextProcess("John told Bill that he was hungry.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val told = textProcessor.workingMemory.concepts[0]
        assertEquals(Acts.MTRANS.name, told.name)
        assertEquals("John", told.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Bill", told.value(ActFields.To)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("S-Hunger", told.valueName(ActFields.Thing))

    }

    @Test
    fun `John had lunch with George`() {
        val textProcessor = runTextProcess("John had lunch with George", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val meal = textProcessor.workingMemory.concepts[0]
        assertEquals("MopMeal", meal.name)
        assertEquals("John", meal.value(MopMealFields.EATER_A)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("George", meal.value(MopMealFields.EATER_B)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("EventEatMeal", meal.valueName(CoreFields.Event))
    }

    @Test
    fun `Word KNOCKED - John knocked a glass of water over`() {
        val textProcessor = runTextProcess("John knocked a glass of water over", lexicon)

        // FIXME implement
        assertEquals(1 /*should be 1?*/, textProcessor.workingMemory.concepts.size)
    }

    @Test
    fun `John had lunch with his wife Ann`() {
        val textProcessor = runTextProcess("John had lunch with his wife Ann.", lexicon)

        // FIXME implement
        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val meal = textProcessor.workingMemory.concepts[0]
        assertEquals(MopMeal.MopMeal.name, meal.name)
        assertEquals("John0", meal.value(MopMealFields.EATER_A)?.valueName(CoreFields.INSTANCE))
        assertEquals("Ann0", meal.value(MopMealFields.EATER_B)?.valueName(CoreFields.INSTANCE))
    }
}