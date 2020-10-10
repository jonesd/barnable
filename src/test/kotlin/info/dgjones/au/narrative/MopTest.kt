package info.dgjones.au.narrative

import info.dgjones.au.concept.Concept
import info.dgjones.au.concept.CoreFields
import info.dgjones.au.concept.Slot
import info.dgjones.au.domain.general.*
import info.dgjones.au.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MopMealTest() {
    @Test
    fun `Create new EpisodicMop from populated Concept`() {
        val memory = EpisodicMemory()

        val concept = Concept(MopMeal.MopMeal.name)
            .with(Slot(MopMealFields.EATER_A, buildHuman("john", "smith", Gender.Male.name)))
            .with(Slot(MopMealFields.EATER_B, buildHuman("george", "may", Gender.Male.name)))
            .with(Slot(MopMealFields.Event, Concept(MopMeal.EventEatMeal.name))
        )

        // test
        val episodic = memory.checkOrCreateMop(concept)

        assertEquals("MopMeal0", episodic.valueName(CoreFields.INSTANCE))

        assertEquals("john", episodic.value(MopMealFields.EATER_A)?.valueName(Human.FIRST_NAME))
        assertEquals("smith", episodic.value(MopMealFields.EATER_A)?.valueName(Human.LAST_NAME))
        assertEquals("Male", episodic.value(MopMealFields.EATER_A)?.valueName(Human.GENDER))

        assertEquals("george", episodic.value(MopMealFields.EATER_B)?.valueName(Human.FIRST_NAME))

        assertEquals(episodic, memory.mops["MopMeal0"])
    }

    @Test
    fun `Use an existing matching Mop using partial match`() {
        val memory = EpisodicMemory()

        val concept = Concept(MopMeal.MopMeal.name)
            .with(Slot(MopMealFields.EATER_A, buildHuman("john", "smith", Gender.Male.name)))
            .with(Slot(MopMealFields.EATER_B, buildHuman("george", "may", Gender.Male.name)))
            .with(Slot(MopMealFields.Event, Concept(MopMeal.EventEatMeal.name)))
        val originalEpisodic = memory.checkOrCreateMop(concept)

        val concept2 = Concept(MopMeal.MopMeal.name)
            .with(Slot(MopMealFields.EATER_A, buildHuman("john", "smith", Gender.Male.name)))
            .with(Slot(MopMealFields.Event, Concept(MopMeal.EventEatMeal.name)))

        // test
        val episodic = memory.checkOrCreateMop(concept2)

        assertEquals("MopMeal0", originalEpisodic.valueName(CoreFields.INSTANCE))
        assertEquals("MopMeal0", episodic.valueName(CoreFields.INSTANCE))

        assertEquals("john", episodic.value(MopMealFields.EATER_A)?.valueName(Human.FIRST_NAME))
        assertEquals("george", episodic.value(MopMealFields.EATER_B)?.valueName(Human.FIRST_NAME))

        assertEquals(episodic, memory.mops["MopMeal0"])
    }
}