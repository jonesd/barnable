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

import info.dgjones.barnable.concept.Concept
import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.Slot
import info.dgjones.barnable.domain.general.*
import info.dgjones.barnable.episodic.EpisodicMemory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MopMealTest {
    @Test
    fun `Create new EpisodicMop from populated Concept`() {
        val memory = EpisodicMemory()

        val concept = Concept(MopMeal.MopMeal.name)
            .with(Slot(MopMealFields.EATER_A, buildHuman("john", "smith", Gender.Male.name)))
            .with(Slot(MopMealFields.EATER_B, buildHuman("george", "may", Gender.Male.name)))
            .with(Slot(CoreFields.Event, Concept(MopMeal.EventEatMeal.name))
        )

        // test
        val episodic = memory.checkOrCreateMop(concept)

        assertEquals("MopMeal0", episodic.valueName(CoreFields.INSTANCE))

        assertEquals("john", episodic.value(MopMealFields.EATER_A)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("smith", episodic.value(MopMealFields.EATER_A)?.valueName(HumanFields.LAST_NAME))
        assertEquals("Male", episodic.value(MopMealFields.EATER_A)?.valueName(HumanFields.GENDER))

        assertEquals("george", episodic.value(MopMealFields.EATER_B)?.valueName(HumanFields.FIRST_NAME))

        assertEquals(episodic, memory.mops["MopMeal0"])
    }

    @Test
    fun `Use an existing matching Mop using partial match`() {
        val memory = EpisodicMemory()

        val concept = Concept(MopMeal.MopMeal.name)
            .with(Slot(MopMealFields.EATER_A, buildHuman("john", "smith", Gender.Male.name)))
            .with(Slot(MopMealFields.EATER_B, buildHuman("george", "may", Gender.Male.name)))
            .with(Slot(CoreFields.Event, Concept(MopMeal.EventEatMeal.name)))
        val originalEpisodic = memory.checkOrCreateMop(concept)

        val concept2 = Concept(MopMeal.MopMeal.name)
            .with(Slot(MopMealFields.EATER_A, buildHuman("john", "smith", Gender.Male.name)))
            .with(Slot(CoreFields.Event, Concept(MopMeal.EventEatMeal.name)))

        // test
        val episodic = memory.checkOrCreateMop(concept2)

        assertEquals("MopMeal0", originalEpisodic.valueName(CoreFields.INSTANCE))
        assertEquals("MopMeal0", episodic.valueName(CoreFields.INSTANCE))

        assertEquals("john", episodic.value(MopMealFields.EATER_A)?.valueName(HumanFields.FIRST_NAME))
        assertEquals("george", episodic.value(MopMealFields.EATER_B)?.valueName(HumanFields.FIRST_NAME))

        assertEquals(episodic, memory.mops["MopMeal0"])
    }
}