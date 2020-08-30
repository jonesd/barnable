package org.dgjones.au.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StansStemmerTest {
    @Test
    fun `should match stem output from paper table 4`() {
        val stemmer = StansStemmer()
        assertEquals("Probate", stemmer.stemWord("Probate"))
        assertEquals("Deadly", stemmer.stemWord("Deadly"))
        assertEquals("Microscope", stemmer.stemWord("Microscopic"))
        assertEquals("Possible", stemmer.stemWord("Possibly"))
        assertEquals("Serious", stemmer.stemWord("Serious"))
        assertEquals("Verify", stemmer.stemWord("Verifiable"))
        assertEquals("Careful", stemmer.stemWord("Carefully")) // Paper incorrectly lists output
        assertEquals("Care", stemmer.stemWord("Carelessly"))
        assertEquals("Purify", stemmer.stemWord("Purifying"))
        assertEquals("Typical", stemmer.stemWord("Typically"))
        assertEquals("Success", stemmer.stemWord("Succeed"))
        assertEquals("Capable", stemmer.stemWord("Capability"))
        assertEquals("Captive", stemmer.stemWord("Captivity"))
        assertEquals("Convenient", stemmer.stemWord("Conveniently"))
        assertEquals("Electric", stemmer.stemWord("Electricity"))
        assertEquals("Score", stemmer.stemWord("Scoring"))
        assertEquals("Happy", stemmer.stemWord("Happy"))
        assertEquals("Provide", stemmer.stemWord("Provided"))
        assertEquals("Serious", stemmer.stemWord("Seriously")) // Paper incorrectly lists output
        assertEquals("Archaeology", stemmer.stemWord("Archaeology"))
    }

    @Test
    fun `should handle empty case`() {
        val stemmer = StansStemmer()
        assertEquals("", stemmer.stemWord(""))
    }
}