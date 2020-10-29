package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.barnable.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TimeTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `Time modification - Yesterday`() {
        val textProcessor = runTextProcess("John walked home yesterday", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val walk = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(Acts.PTRANS.name, walk.name)
        Assertions.assertEquals("John", walk.value(ActFields.Actor)?.valueName(HumanFields.FIRST_NAME))
        Assertions.assertEquals("Home", walk.value(ActFields.To)?.valueName(CoreFields.Name))
        Assertions.assertEquals("Yesterday", walk.valueName(TimeFields.TIME))
    }
}