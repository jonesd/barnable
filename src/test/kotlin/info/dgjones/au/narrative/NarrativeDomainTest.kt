package info.dgjones.au.narrative

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.domain.general.*
import info.dgjones.au.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NarrativeDomainTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Example execution`() {
        val textProcessor = runTextProcess("John picked up the ball and dropped it in the box", lexicon)

        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val grasp = textProcessor.workingMemory.concepts[0]
        assertEquals("GRASP", grasp.name)
        assertEquals("John", grasp.value("actor")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("ball", grasp.value("thing")?.valueName("name"))
        val actGraspInstr = grasp.value("instr")!!
        // FIXME move structural element not present, rely on name
        assertEquals("MOVE", actGraspInstr.name)
        assertEquals("John", actGraspInstr.value("actor")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("ball", actGraspInstr.value("to")?.valueName("name"))

        val ptrans = textProcessor.workingMemory.concepts[1]
        assertEquals("PTRANS", ptrans.name)
        assertEquals("John", ptrans.value("actor")?.valueName(HumanFields.FIRST_NAME))
        // FIXME thing should be obj
        assertEquals("ball", ptrans.value("thing")?.valueName("name"))
        assertEquals("box", ptrans.value("to")?.valueName("name"))
        val ptransIntr = ptrans.value("instr")
        assertEquals("Gravity", ptransIntr?.value("actor")?.name)
    }

    @Test
    fun `Exercise 1 John gave Mary a book`() {
        val textProcessor = runTextProcess("John gave Mary a book", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val atrans = textProcessor.workingMemory.concepts[0]
        assertEquals("ATRANS", atrans.name)
        assertEquals("John", atrans.value("actor")?.valueName(HumanFields.FIRST_NAME))
        //FIXME should match TYPE (BOOK)
        assertEquals("book", atrans.value("thing")?.valueName("name"))
        assertEquals("Mary", atrans.value("to")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("John", atrans.value("from")?.valueName(HumanFields.FIRST_NAME))
    }

    @Test
    fun `Exercise 2 Fred told Mary that John eats lobster`() {
        val textProcessor = runTextProcess("Fred told Mary that John eats lobster", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val mtrans = textProcessor.workingMemory.concepts[0]
        assertEquals("MTRANS", mtrans.name)
        assertEquals("Fred", mtrans.value("actor")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Fred", mtrans.value("from")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Mary", mtrans.value("to")?.valueName(HumanFields.FIRST_NAME))
        val ingest = mtrans.value("thing")!!
        //FIXME this what the book has:
        // assertEquals("Fred", ingest.actor.firstName)
        // this seems correct:
        assertEquals("John", ingest.value("actor")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Lobster", ingest.value("thing")?.valueName(CoreFields.Name))
    }

    @Test
    fun `Colour modifier`() {
        val textProcessor = runTextProcess("the red book", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(PhysicalObjectKind.Book.name, concept.valueName("kind"))
        assertEquals("red", concept.valueName("colour"))
    }

    @Test
    fun `Age-Weight modifiers`() {
        val textProcessor = runTextProcess("a thin old man", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals("GT-NORM", concept.valueName("age"))
        assertEquals("LT-NORM", concept.valueName("weight"))
    }

    @Test
    fun `Basic pronoun reference`() {
        val textProcessor = runTextProcess("John went home. He kissed his wife Anne.", lexicon)

        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val travel = textProcessor.workingMemory.concepts[0]
        assertEquals("John", travel.value("actor")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Home", travel.value("to")?.valueName(CoreFields.Name))

        val kissAttend = textProcessor.workingMemory.concepts[1]
        assertEquals("ATTEND", kissAttend.name)
        assertEquals("John0", kissAttend.value("actor")?.valueName(CoreFields.INSTANCE))
        assertEquals(Gender.Male.name, kissAttend.value("actor")?.valueName(HumanFields.GENDER))
        assertEquals("Anne0", kissAttend.value("to")?.valueName(CoreFields.INSTANCE))
        assertEquals(Gender.Female.name, kissAttend.value("to")?.valueName(HumanFields.GENDER))

        //FIXME more assertions
    }

    @Test
    fun `Basic pronoun reference 2`() {
        val textProcessor = runTextProcess("John told Bill that he was hungry.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val told = textProcessor.workingMemory.concepts[0]
        assertEquals("MTRANS", told.name)
        assertEquals("John", told.value("actor")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("Bill", told.value("to")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("S-Hunger", told.valueName("thing"))

    }

    @Test
    fun `John had lunch with George`() {
        val textProcessor = runTextProcess("John had lunch with George", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val meal = textProcessor.workingMemory.concepts[0]
        assertEquals("MopMeal", meal.name)
        assertEquals("John", meal.value("eaterA")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("George", meal.value("eaterB")?.valueName(HumanFields.FIRST_NAME))
        assertEquals("EventEatMeal", meal.valueName("event"))
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