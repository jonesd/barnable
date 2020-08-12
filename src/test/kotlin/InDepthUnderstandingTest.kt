import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InDepthUnderstandingTest {

    val johnPickedUpTheBallAndPutItInTheBox = "John picked up the ball and dropped it in the box"

    @Test
    fun `Example execution`() {
        val textModel = TextModelBuilder(johnPickedUpTheBallAndPutItInTheBox).buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        assertEquals(2, textProcessor.workingMemory.concepts.size)

        val grasp = textProcessor.workingMemory.concepts[0]
        assertEquals("GRASP", grasp.name)
        val actGrasp = grasp as ActGrasp
        assertEquals("John",  actGrasp.actor.firstName)
        assertEquals("ball", (actGrasp.obj as PhysicalObject).name)
        val actGraspInstr = (grasp as ActGrasp).instr
        // FIXME move structural element not present, rely on name
        assertEquals("MOVE", actGraspInstr.name)
        assertEquals("John", actGraspInstr.actor.firstName)
        assertEquals("ball", (actGraspInstr.to as PhysicalObject).name)

        val ptrans = textProcessor.workingMemory.concepts[1]
        assertEquals("PTRANS", ptrans.name)
        val actPtrans = ptrans as ActPtrans
        assertEquals("John", actPtrans.actor.firstName)
        // FIXME thing should be obj
        assertEquals("ball", actPtrans.thing.name)
        assertEquals("box", (actPtrans.to as PhysicalObject).name)
        val actPtransIntr = actPtrans.instr as ActPropel
        assertEquals("gravity", actPtransIntr.actor.name)
    }



    @Test
    fun `Exercise 1 John gave Mary a book`() {
        val textModel = TextModelBuilder("John gave Mary a book").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val atrans = textProcessor.workingMemory.concepts[0]
        assertEquals("ATRANS", atrans.name)
        val actAtrans = atrans as ActAtrans
        assertEquals("John", actAtrans.actor.firstName)
        //FIXME should match TYPE (BOOK)
        assertEquals("book", (actAtrans.obj as PhysicalObject).name)
        assertEquals("Mary", actAtrans.to.firstName)
        assertEquals("John", actAtrans.from.firstName)
    }

    @Test
    fun `Question who gave mary the book - shared working memory`() {
        val textModel = TextModelBuilder("John gave Mary a book").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        val questionModel = TextModelBuilder("Who gave Mary a book").buildModel()
        val response = textProcessor.processQuestion(questionModel.sentences[0])

        assertEquals("John", response)

        val questionModel2 = TextModelBuilder("John gave who the book").buildModel()
        val response2 = textProcessor.processQuestion(questionModel2.sentences[0])

        assertEquals("Mary", response2)
    }

    @Test
    fun `Exercise 2 Fred told Mary that John eats lobster`() {
        val textModel = TextModelBuilder("Fred told Mary that John eats lobster").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        textProcessor.runProcessor()

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val mtrans = textProcessor.workingMemory.concepts[0]
        assertEquals("MTRANS", mtrans.name)
        val actMtrans = mtrans as ActMtrans
        assertEquals("Fred", actMtrans.actor.firstName)
        assertEquals("Fred", actMtrans.from.firstName)
        assertEquals("Mary", actMtrans.to.firstName)
        val ingest = actMtrans.obj as ActIngest
        //FIXME this what the book has:
        // assertEquals("Fred", ingest.actor.firstName)
        // this seems correct:
        assertEquals("John", ingest.actor.firstName)
        assertTrue(ingest.obj.isKind("Lobster"))
    }

    @Test
    fun `Colour modifier`() {
        val textModel = TextModelBuilder("the red book").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()

        assertEquals(1, workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertTrue(concept is PhysicalObject)
        assertEquals("red", (concept as PhysicalObject).modifier("colour"))
    }

    @Test
    fun `Age-Weight modifiers`() {
        val textModel = TextModelBuilder("a thin old man").buildModel()
        val lexicon = buildInDepthUnderstandingLexicon()

        val textProcessor = TextProcessor(textModel, lexicon)
        val workingMemory = textProcessor.runProcessor()

        assertEquals(1, workingMemory.concepts.size)
        val concept = textProcessor.workingMemory.concepts.first()
        assertEquals("GT-NORM", concept.modifier("age"))
        assertEquals("LT-NORM", concept.modifier("weight"))
    }
}