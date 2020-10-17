package info.dgjones.au.domain.general

import info.dgjones.au.narrative.buildInDepthUnderstandingLexicon
import info.dgjones.au.parser.runTextProcess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HonorificTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Nested
    inner class CommonTitles {
        @Test
        fun `Mr Snicklefritz`() {
            val textProcessor = runTextProcess("Mr Snicklefritz.", lexicon)

            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FIRST_NAME))
            Assertions.assertEquals(Gender.Male.name, human.valueName(HumanFields.GENDER))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LAST_NAME))
        }

        @Test
        fun `Mrs Snicklefritz`() {
            val textProcessor = runTextProcess("Mrs Snicklefritz.", lexicon)

            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FIRST_NAME))
            Assertions.assertEquals(Gender.Female.name, human.valueName(HumanFields.GENDER))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LAST_NAME))
        }

        @Test
        fun `Miss Snicklefritz`() {
            val textProcessor = runTextProcess("Miss Snicklefritz.", lexicon)

            // FIXME implement
            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FIRST_NAME))
            Assertions.assertEquals(Gender.Female.name, human.valueName(HumanFields.GENDER))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LAST_NAME))
        }

        @Test
        fun `Ms Snicklefritz`() {
            val textProcessor = runTextProcess("Ms Snicklefritz.", lexicon)

            // FIXME implement
            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("", human.valueName(HumanFields.FIRST_NAME))
            Assertions.assertEquals(Gender.Female.name, human.valueName(HumanFields.GENDER))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LAST_NAME))
        }

        @Test
        fun `Mr John Snicklefritz`() {
            val textProcessor = runTextProcess("Mr John Snicklefritz.", lexicon)

            Assertions.assertEquals(1, textProcessor.workingMemory.concepts.size)
            val human = textProcessor.workingMemory.concepts[0]
            Assertions.assertEquals(HumanConcept.Human.name, human.name)
            Assertions.assertEquals("John", human.valueName(HumanFields.FIRST_NAME))
            Assertions.assertEquals(Gender.Male.name, human.valueName(HumanFields.GENDER))
            Assertions.assertEquals("Snicklefritz", human.valueName(HumanFields.LAST_NAME))
        }
    }
}