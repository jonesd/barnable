import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InDepthUnderstandingTest {

    val johnPickedUpTheBallAndPutItInTheBox = "John picked up the ball and dropped it in the box"

    @Test
    fun `Execution Test`() {
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
}