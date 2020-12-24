/*
 * Copyright  2020 David G Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package info.dgjones.barnable.concept

import info.dgjones.barnable.episodic.EpisodicMemory
import info.dgjones.barnable.nlp.TextSentence
import info.dgjones.barnable.nlp.WordElement
import info.dgjones.barnable.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException

class VariableSpaceTest {
    var space = VariableSpace();
    @BeforeEach
    fun setup() {
        space = VariableSpace()
    }
    @Test
    fun `Can record and lookup variables`() {
        val variable = CompletableVariable("testField", space, "testVar")
        space.declareVariable(variable)
        assertSame(variable, space.getVariable("testVar"))
    }
    @Test()
    fun `Looking up unknown variable fails`() {
        assertThrows(NoSuchElementException::class.java, {
            space.getVariable("unknownVar")
        })
    }
    @Test
    fun `Cannot replace variable`() {
        val variable = CompletableVariable("testField", space, "testVar")
        space.declareVariable(variable)

        assertThrows(IllegalStateException::class.java, {
            val overwrite = CompletableVariable("otherField", space, "testVar")
            space.declareVariable(overwrite)
        })
    }
    @Test
    fun `Id sequence takes into account declared variables`() {
        assertEquals(1, space.nextVariableIndex())
        space.declareVariable(CompletableVariable("field", space, "testVar"))
        assertEquals(2, space.nextVariableIndex())
    }
}

class CompletableVariableTest {
    var space: VariableSpace = VariableSpace()

    // Word Context
    val defHolder = ConceptHolder(9)
    val testElement = WordElement("one", "", "","")
    val workingMemory = WorkingMemory()
    val sentenceContext = SentenceContext(TextSentence("test", listOf(testElement)), workingMemory, EpisodicMemory())
    val wordContext = WordContext(0, "test", defHolder, sentenceContext)


    enum class TestFields(override val fieldName: String): Fields {
        // Related episodic concept identity
        Source("source"),
        Dependent1("dep1"),
        Dependent2("dep2")
    }

    @BeforeEach
    fun setup() {
        space = VariableSpace()
    }

    @Test
    fun `Create Variable Slot with name`() {
        var variable = CompletableVariable(TestFields.Source.fieldName, space, "testVar")

        assertEquals(Slot("source", Concept("*VAR.testVar*")), variable.sourceReference.variableSlot)
    }
    @Test
    fun `Create Variable Slot with generated name`() {
        var variable = CompletableVariable(TestFields.Source.fieldName, space)

        assertEquals(Slot("source", Concept("*VAR.1*")), variable.sourceReference.variableSlot)
    }
    @Test
    fun `Resolve variable`() {
        var variable = CompletableVariable(TestFields.Source.fieldName, space, "testVar")
        variable.complete(ConceptHolder(1, Concept("testResult")), wordContext)

        assertEquals(Slot("source", Concept("testResult")), variable.sourceReference.variableSlot)
    }
    @Test
    fun `Resolved variable value can be transformed`() {
        val upperCaseTransformer = ConceptTransformer { if (it != null) Concept(it.name.toUpperCase()) else null }

        var variable = CompletableVariable(TestFields.Source.fieldName, space, sourceExpression = upperCaseTransformer)
        variable.complete(ConceptHolder(1, Concept("testResult")), wordContext)

        assertEquals(Slot("source", Concept("TESTRESULT")), variable.sourceReference.variableSlot)
    }
    @Test
    fun `Can update dependent reference with resolved value`() {
        var source = CompletableVariable(TestFields.Source.fieldName, space, "testVar")
        var dependentSlot = source.addVariableReference(TestFields.Dependent1)

        assertEquals(Slot(TestFields.Dependent1, Concept("*VAR.testVar*")), dependentSlot)

        source.complete(ConceptHolder(1, Concept("resolvedValue")), wordContext)

        assertEquals(Slot(TestFields.Source, Concept("resolvedValue")), source.sourceReference.variableSlot)
        assertEquals(Slot(TestFields.Dependent1, Concept("resolvedValue")), dependentSlot)
    }
    @Test
    fun `Preserve changed dependent references if they no longer reference variable`() {
        var source = CompletableVariable(TestFields.Source.fieldName, space, "testVar")
        var dependentSlot = source.addVariableReference(TestFields.Dependent1)

        assertEquals(Slot(TestFields.Dependent1, Concept("*VAR.testVar*")), dependentSlot)
        dependentSlot.value = Concept("manualUpdate")

        source.complete(ConceptHolder(1, Concept("resolvedValue")), wordContext)

        assertEquals(Slot(TestFields.Source, Concept("resolvedValue")), source.sourceReference.variableSlot)
        assertEquals(Slot(TestFields.Dependent1, Concept("manualUpdate")), dependentSlot)
    }
    @Test
    fun `Should mark embedded resolved source concept as being Inside`() {
        var variable = CompletableVariable(TestFields.Source.fieldName, space, "testVar")
        val conceptHolder = ConceptHolder(1, Concept("testResult"))
        variable.complete(conceptHolder, wordContext)

        assertEquals(Slot(TestFields.Source, Concept("testResult")), variable.sourceReference.variableSlot)
        assertTrue(conceptHolder.hasFlag(ParserFlags.Inside))
    }
    @Test
    fun `Can inject resolved value into provided Concept`() {
        var variable = CompletableVariable(TestFields.Source.fieldName, space, "testVar")

        val conceptHolder = ConceptHolder(1, Concept("testResult"))
        conceptHolder.value?.value("testChild", Concept("testChildValue"))

        val replacementConcept = Concept("replacementRoot")
        replacementConcept.value("replacementChild", Concept("replacementChildValue"))
        replacementConcept.value("testChild", conceptHolder.value)
        variable.overwriteResolvedHolder = replacementConcept

        variable.complete(conceptHolder, wordContext)

        assertEquals(Slot(TestFields.Source, Concept("replacementRoot")), variable.sourceReference.variableSlot)
        assertEquals(Concept("replacementRoot"), conceptHolder.value)
    }
}

class VariableReferenceTest {

}