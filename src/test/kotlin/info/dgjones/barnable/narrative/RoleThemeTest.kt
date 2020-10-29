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

package info.dgjones.barnable.narrative

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.domain.general.ActFields
import info.dgjones.barnable.domain.general.Gender
import info.dgjones.barnable.domain.general.HumanFields
import info.dgjones.barnable.domain.general.RoleThemeFields
import info.dgjones.barnable.parser.*
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