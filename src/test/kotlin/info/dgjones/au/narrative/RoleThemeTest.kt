package info.dgjones.au.narrative

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.domain.general.Gender
import info.dgjones.au.domain.general.HumanFields
import info.dgjones.au.domain.general.RoleThemeFields
import info.dgjones.au.parser.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RoleThemeTest {
    val lexicon = buildInDepthUnderstandingLexicon()

    @Test
    fun `RoleThemes are Humans and so can be used with actions`() {
        val textProcessor = runTextProcess("The teacher had lunch", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val meal = textProcessor.workingMemory.concepts[0]
        assertEquals(MopMeal.MopMeal.name, meal.name)
        assertEquals("RoleThemeTeacher", meal.value(MopMealFields.EATER_A)?.valueName(RoleThemeFields.RoleTheme))
        assertEquals("RoleThemeTeacher0", meal.value(MopMealFields.EATER_A)?.valueName(CoreFields.INSTANCE))
    }

    @Test
    fun `Include gender for gender specific role names`() {
        val textProcessor = runTextProcess("The waitress poured the wine.", lexicon)

        assertEquals(1, textProcessor.workingMemory.concepts.size)

        val grasp = textProcessor.workingMemory.concepts[0]
        assertEquals("GRASP", grasp.name)
        assertEquals("RoleThemeWaiter", grasp.value(ActFields.Actor)?.valueName(RoleThemeFields.RoleTheme))
        assertEquals("RoleThemeWaiter0", grasp.value(ActFields.Actor)?.valueName(CoreFields.INSTANCE))
        assertEquals(Gender.Female.name, grasp.value(ActFields.Actor)?.valueName(HumanFields.GENDER))
        assertEquals("wine", grasp.value(ActFields.Thing)?.valueName(CoreFields.Name))
    }
}