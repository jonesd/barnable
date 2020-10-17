package info.dgjones.au.domain.general

import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HumanTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `First name male`() {
        val textProcessor = runTextProcess("John.", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(HumanConcept.Human.name, human.name)
        Assertions.assertEquals("John", human.valueName(HumanFields.FIRST_NAME))
        Assertions.assertEquals(Gender.Male.name, human.valueName(HumanFields.GENDER))
        Assertions.assertNull(human.valueName(HumanFields.LAST_NAME))
    }

    @Test
    fun `First name female`() {
        val textProcessor = runTextProcess("Jane.", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(HumanConcept.Human.name, human.name)
        Assertions.assertEquals("Jane", human.valueName(HumanFields.FIRST_NAME))
        Assertions.assertEquals(Gender.Female.name, human.valueName(HumanFields.GENDER))
        Assertions.assertNull(human.valueName(HumanFields.LAST_NAME))
    }

    @Test
    fun `Full Name`() {
        val textProcessor = runTextProcess("John Snicklefritz.", lexicon)

        Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
        val human = textProcessor.workingMemory.concepts[0]
        Assertions.assertEquals(HumanConcept.Human.name, human.name)
        Assertions.assertEquals("John", human.valueName(HumanFields.FIRST_NAME))
        Assertions.assertEquals(Gender.Male.name, human.valueName(HumanFields.GENDER))
        Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LAST_NAME))
    }
}