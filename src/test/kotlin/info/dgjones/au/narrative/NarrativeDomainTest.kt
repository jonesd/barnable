package info.dgjones.au.narrative

import info.dgjones.au.nlp.NaiveTextModelBuilder
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
        assertEquals("John", grasp.value("actor")?.valueName("firstName"))
        assertEquals("ball", grasp.value("thing")?.valueName("name"))
        val actGraspInstr = grasp.value("instr")!!
        // FIXME move structural element not present, rely on name
        assertEquals("MOVE", actGraspInstr.name)
        assertEquals("John", actGraspInstr.value("actor")?.valueName("firstName"))
        assertEquals("ball", actGraspInstr.value("to")?.valueName("name"))

        val ptrans = textProcessor.workingMemory.concepts[1]
        assertEquals("PTRANS", ptrans.name)
        assertEquals("John", ptrans.value("actor")?.valueName("firstName"))
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
        assertEquals("John", atrans.value("actor")?.valueName("firstName"))
        //FIXME should match TYPE (BOOK)
        assertEquals("book", atrans.value("thing")?.valueName("name"))
        assertEquals("Mary", atrans.value("to")?.valueName("firstName"))
        assertEquals("John", atrans.value("from")?.valueName("firstName"))
    }

    @Test
    fun `Exercise 2 Fred told Mary that John eats lobster`() {
        val textProcessor = runTextProcess("Fred told Mary that John eats lobster", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val mtrans = textProcessor.workingMemory.concepts[0]
        assertEquals("MTRANS", mtrans.name)
        assertEquals("Fred", mtrans.value("actor")?.valueName("firstName"))
        assertEquals("Fred", mtrans.value("from")?.valueName("firstName"))
        assertEquals("Mary", mtrans.value("to")?.valueName("firstName"))
        val ingest = mtrans.value("thing")!!
        //FIXME this what the book has:
        // assertEquals("Fred", ingest.actor.firstName)
        // this seems correct:
        assertEquals("John", ingest.value("actor")?.valueName("firstName"))
        assertEquals("Lobster", ingest.value("thing")?.valueName("kind"))
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
        assertEquals("John", travel.value("actor")?.valueName("firstName"))
        assertEquals("Home", travel.value("to")?.valueName("name"))

        val kissAttend = textProcessor.workingMemory.concepts[1]
        assertEquals("ATTEND", kissAttend.name)
        assertEquals("John0", kissAttend.value("actor")?.valueName(CoreFields.INSTANCE))
        assertEquals(Gender.Male.name, kissAttend.value("actor")?.valueName(Human.GENDER))
        assertEquals("Anne0", kissAttend.value("to")?.valueName(CoreFields.INSTANCE))
        assertEquals(Gender.Female.name, kissAttend.value("to")?.valueName(Human.GENDER))

        //FIXME more assertions
    }

    @Test
    fun `Basic pronoun reference 2`() {
        val textProcessor = runTextProcess("John told Bill that he was hungry.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        var told = textProcessor.workingMemory.concepts[0]
        assertEquals("MTRANS", told.name)
        assertEquals("John", told.value("actor")?.valueName("firstName"))
        assertEquals("Bill", told.value("to")?.valueName("firstName"))
        assertEquals("S-Hunger", told.valueName("thing"))

    }

    @Test
    fun `Time modification`() {
        val textProcessor = runTextProcess("John walked home yesterday", lexicon)

        assertEquals(1 /*should be 1?*/, textProcessor.workingMemory.concepts.size)
        var walk = textProcessor.workingMemory.concepts[0]
        assertEquals("PTRANS", walk.name)
        assertEquals("John", walk.value("actor")?.valueName("firstName"))
        assertEquals("Home", walk.value("to")?.valueName("name"))
        assertEquals("Yesterday", walk.valueName("time"))
    }

    @Test
    fun `John had lunch with George`() {
        val textProcessor = runTextProcess("John had lunch with George", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val meal = textProcessor.workingMemory.concepts[0]
        assertEquals("M-Meal", meal.name)
        assertEquals("John", meal.value("eater-a")?.valueName("firstName"))
        assertEquals("George", meal.value("eater-b")?.valueName("firstName"))
        assertEquals("EV-LUNCH", meal.valueName("event"))
    }

    @Test
    fun `Word KNOCKED - John knocked a glass of water over`() {
        val textProcessor = runTextProcess("John knocked a glass of water over", lexicon)

        // FIXME implement
        assertEquals(3 /*should be 1?*/, textProcessor.workingMemory.concepts.size)
    }

    @Test
    fun `John had lunch with his wife Ann`() {
        val textProcessor = runTextProcess("John had lunch with his wife Ann.", lexicon)

        // FIXME implement
        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val meal = textProcessor.workingMemory.concepts[0]
        assertEquals("M-Meal", meal.name)
        assertEquals("John0", meal.value(MealFields.EATER_A)?.valueName(CoreFields.INSTANCE))
        assertEquals("Ann0", meal.value(MealFields.EATER_B)?.valueName(CoreFields.INSTANCE))
    }

    @Test
    fun `John and his wife Ann`() {
        val textProcessor = runTextProcess("John and his wife Ann.", lexicon)

        // FIXME implement
        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val john = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, john.name)
        assertEquals("John", john.valueName(Human.FIRST_NAME))

        val wife = textProcessor.workingMemory.concepts[1]
        assertEquals(Human.CONCEPT.fieldName, wife.name)
        val marriage = wife.value(Relationships.Name)
        assertNotNull(marriage)
        assertEquals(Gender.Male.name, marriage?.value(Marriage.Husband)?.valueName(Human.GENDER))
        assertEquals("Ann", marriage?.value(Marriage.Wife)?.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Female.name, marriage?.value(Marriage.Wife)?.valueName(Human.GENDER))
    }

    @Test
    fun `John Snicklefritz`() {
        val textProcessor = runTextProcess("John Snicklefritz.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, human.name)
        assertEquals("John", human.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Male.name, human.valueName(Human.GENDER))
        assertEquals("Snicklefritz", human.valueName(Human.LAST_NAME))
    }

    @Test
    fun `Mr Snicklefritz`() {
        val textProcessor = runTextProcess("Mr Snicklefritz.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, human.name)
        assertEquals("", human.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Male.name, human.valueName(Human.GENDER))
        assertEquals("Snicklefritz", human.valueName(Human.LAST_NAME))
    }

    @Test
    fun `Mrs Snicklefritz`() {
        val textProcessor = runTextProcess("Mrs Snicklefritz.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, human.name)
        assertEquals("", human.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Female.name, human.valueName(Human.GENDER))
        assertEquals("Snicklefritz", human.valueName(Human.LAST_NAME))
    }

    @Test
    fun `Miss Snicklefritz`() {
        val textProcessor = runTextProcess("Miss Snicklefritz.", lexicon)

        // FIXME implement
        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, human.name)
        assertEquals("", human.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Female.name, human.valueName(Human.GENDER))
        assertEquals("Snicklefritz", human.valueName(Human.LAST_NAME))
    }

    @Test
    fun `Ms Snicklefritz`() {
        val textProcessor = runTextProcess("Ms Snicklefritz.", lexicon)

        // FIXME implement
        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, human.name)
        assertEquals("", human.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Female.name, human.valueName(Human.GENDER))
        assertEquals("Snicklefritz", human.valueName(Human.LAST_NAME))
    }

    @Test
    fun `Mr John Snicklefritz`() {
        val textProcessor = runTextProcess("Mr John Snicklefritz.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        assertEquals(Human.CONCEPT.fieldName, human.name)
        assertEquals("John", human.valueName(Human.FIRST_NAME))
        assertEquals(Gender.Male.name, human.valueName(Human.GENDER))
        assertEquals("Snicklefritz", human.valueName(Human.LAST_NAME))
    }
}