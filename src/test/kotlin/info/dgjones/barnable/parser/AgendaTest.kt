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

package info.dgjones.barnable.parser

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AgendaTest {
    @Test
    fun `most recently added demons are prioritized earlier`() {
        val agenda = Agenda()

        val wordContext = withWordContext()
        val demonOldest = Demon(wordContext)
        val demonOlder = Demon(wordContext)
        val demonNewer = Demon(wordContext)

        agenda.withDemon(0, demonOldest)
        agenda.withDemon(0, demonOlder)
        agenda.withDemon(0, demonNewer)

        // test
        val demons = agenda.prioritizedActiveDemons()

        assertEquals(listOf(demonNewer, demonOlder, demonOldest), demons)
    }

    @Nested
    inner class RunningDemons {
        @Test
        fun `run all demons once in prioritized order`() {
            val agenda = Agenda()

            val demonOldest = mockk<Demon>(relaxUnitFun = true)
            every { demonOldest.active } returns true
            val demonOlder = mockk<Demon>(relaxUnitFun = true)
            every { demonOlder.active } returns true
            val demonNewer = mockk<Demon>(relaxUnitFun = true)
            every { demonNewer.active } returns true

            agenda.withDemon(0, demonOldest)
            agenda.withDemon(0, demonOlder)
            agenda.withDemon(0, demonNewer)

            // test
            agenda.runDemons()

            verifyOrder {
                demonNewer.run()
                demonOlder.run()
                demonOldest.run()
            }
        }

        @Test
        fun `rerun active demons after each demon completes`() {
            val agenda = Agenda()

            val demonOldest = mockk<Demon>(relaxUnitFun = true)
            every { demonOldest.active } returns true
            val demonOlder = mockk<Demon>(relaxUnitFun = true)
            every { demonOlder.active } returns true
            val demonNewerCompletesAfterRun = mockk<Demon>(relaxUnitFun = true)
            every { demonNewerCompletesAfterRun.active } returnsMany listOf(true, false)

            agenda.withDemon(0, demonOldest)
            agenda.withDemon(0, demonOlder)
            agenda.withDemon(0, demonNewerCompletesAfterRun)

            // test
            agenda.runDemons()

            verify(exactly = 1) {demonNewerCompletesAfterRun.run()}
            verify(exactly = 2) {demonOlder.run()}
            verify(exactly = 2) {demonOldest.run()}
        }
    }
}