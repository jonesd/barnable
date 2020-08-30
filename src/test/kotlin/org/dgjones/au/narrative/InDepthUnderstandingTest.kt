package org.dgjones.au.narrative

import org.dgjones.au.nlp.NaiveTextModelBuilder
import org.dgjones.au.parser.TextProcessor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InDepthUnderstandingTest {

    val johnPickedUpTheBallAndPutItInTheBox = "John picked up the ball and dropped it in the box"

    @Test
    fun `Example execution`() {
        val textModel = NaiveTextModelBuilder(johnPickedUpTheBallAndPutItInTheBox).buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

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
        val textModel = NaiveTextModelBuilder("John gave Mary a book").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

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
    fun `Question who gave mary the book - shared working memory`() {
        val textModel = NaiveTextModelBuilder("John gave Mary a book").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        val questionModel = NaiveTextModelBuilder("Who gave Mary a book").buildModel()
        val response = textProcessor.processQuestion(questionModel.paragraphs.first().sentences.first())

        assertEquals("John", response)

        val questionModel2 = NaiveTextModelBuilder("John gave who the book").buildModel()
        val response2 = textProcessor.processQuestion(questionModel2.paragraphs.first().sentences.first())

        assertEquals("Mary", response2)
    }

    @Test
    fun `Exercise 2 Fred told Mary that John eats lobster`() {
        val textModel = NaiveTextModelBuilder("Fred told Mary that John eats lobster").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

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
        val textModel = NaiveTextModelBuilder("the red book").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()

        assertEquals(1, workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals(PhysicalObjectKind.Book.name, concept.valueName("kind"))
        assertEquals("red", concept.valueName("colour"))
    }

    @Test
    fun `Age-Weight modifiers`() {
        val textModel = NaiveTextModelBuilder("a thin old man").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()

        assertEquals(1, workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals("GT-NORM", concept.valueName("age"))
        assertEquals("LT-NORM", concept.valueName("weight"))
    }

    @Test
    fun `Basic pronoun reference`() {
        val textModel = NaiveTextModelBuilder("John went home. He kissed his wife Anne.").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()
        println(workingMemory.concepts)

        assertEquals(2, workingMemory.concepts.size)
        val travel = textProcessor.workingMemory.concepts[0]
        assertEquals("John", travel.value("actor")?.valueName("firstName"))
        assertEquals("Home", travel.value("to")?.valueName("name"))
        val kissAttend = textProcessor.workingMemory.concepts[1]
        assertEquals("John", kissAttend.value("actor")?.valueName("firstName"))
        assertEquals("Anne", kissAttend.value("to")?.valueName("firstName"))

        //FIXME more assertions
    }

    @Test
    fun `Basic pronoun reference 2`() {
        val textModel = NaiveTextModelBuilder("John told Bill that he was hungry.").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()
        println(workingMemory.concepts)

        assertEquals(2 /*should be 1?*/, workingMemory.concepts.size)
        var told = workingMemory.concepts[0]
        assertEquals("MTRANS", told.name)
        assertEquals("John", told.value("actor")?.valueName("firstName"))
        assertEquals("Bill", told.value("to")?.valueName("firstName"))
        assertEquals("S-Hunger", told.valueName("thing"))

    }

    @Test
    fun `Time modification`() {
        val textModel = NaiveTextModelBuilder("John walked home yesterday").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()
        println(workingMemory.concepts)

        assertEquals(1 /*should be 1?*/, workingMemory.concepts.size)
        var walk = workingMemory.concepts[0]
        assertEquals("PTRANS", walk.name)
        assertEquals("John", walk.value("actor")?.valueName("firstName"))
        assertEquals("Home", walk.value("to")?.valueName("name"))
        assertEquals("Yesterday", walk.valueName("time"))

    }
}