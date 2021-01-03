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

open class Demon(val wordContext: WordContext, val highPriority: Boolean = false) {
    private val demonIndex = wordContext.nextDemonIndex()
    var active = true

    open fun run() {
        // Without an implementation - just deactivate
        active = false
    }

    /* Stop the Demon from being scheduled to run again.
    Will not stop a currently running demon */
    fun deactivate() {
        active = false
    }

    // Runtime comment to understand processing
    open fun comment(): DemonComment {
        return DemonComment("demon: $this", "")
    }

    override fun toString(): String {
        return "{demon${wordContext.defHolder.instanceNumber}/${demonIndex}=${description()}, active=$active, priority=$highPriority}"
    }

    open fun description(): String {
        return ""
    }
}

class DemonComment(val test: String, val act: String)

/*
List of demons for execution.
 */
class Agenda {
    val demons = mutableListOf<Demon>()

    fun withDemon(wordIndex: Int, demon: Demon) {
        demons.add(demon)
    }

    // Active demons in a prioritized order for execution:
    // - Most recently added first - ie current word
    fun prioritizedActiveDemons(): List<Demon> {
        val prioritized = demons.filter { it.highPriority && it.active }.reversed()
        var normal = demons.filter { !it.highPriority && it.active }.reversed()
        return listOf(prioritized, normal).flatten()
    }

    // Run each active demon is a prioritized order.
    // If any complete, then give the demons another
    // chance to react to changes in the state.
    fun runDemons() {
        do {
            var fired = false
            prioritizedActiveDemons().forEach {
                it.run()
                if (!it.active) {
                    println("Killed demon=$it")
                    fired = true
                }
            }
        } while (fired)
    }
}
