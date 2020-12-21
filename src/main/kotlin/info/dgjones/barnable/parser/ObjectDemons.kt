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

class SaveObjectDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        val o = wordContext.def()
        if (o != null) {
            wordContext.context.mostRecentObject = o
            active = false
        }
    }

    override fun description(): String {
        return "SaveObject def=${wordContext.def()}"
    }
}

class FindObjectReferenceDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        if (!wordContext.isDefSet()) {
            wordContext.defHolder.value = wordContext.context.mostRecentObject
            println("updated word=${wordContext.word} to def=${wordContext.def()}")
        }
        if (wordContext.isDefSet()) {
            active = false
        }
    }
}
