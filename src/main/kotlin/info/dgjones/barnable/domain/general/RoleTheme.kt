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

package info.dgjones.barnable.domain.general

import info.dgjones.barnable.concept.CoreFields
import info.dgjones.barnable.concept.Fields
import info.dgjones.barnable.concept.lexicalConcept
import info.dgjones.barnable.narrative.InDepthUnderstandingConcepts
import info.dgjones.barnable.parser.*

/*
RoleTheme typically represents a Human indicated with a role, such as the profession of Teacher.
The Role can be used to identify MOPs appropriate for the story.
 */
enum class RoleTheme {
    RoleTheme,
    RoleThemeTeacher,
    RoleThemeWaiter,
}

enum class RoleThemeFields(override val fieldName: String): Fields {
    RoleTheme("roleTheme")
}

// Word Senses

fun buildGeneralRoleThemeLexicon(lexicon: Lexicon) {
    lexicon.addMapping(RoleThemeWord("teacher", RoleTheme.RoleThemeTeacher))
    lexicon.addMapping(RoleThemeWord("waiter", RoleTheme.RoleThemeWaiter))
    lexicon.addMapping(RoleThemeWord("waitress", RoleTheme.RoleThemeWaiter, Gender.Female))
}

class RoleThemeWord(word: String, private val roleTheme: RoleTheme, val gender: Gender? = null): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> =
        lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(RoleThemeFields.RoleTheme, roleTheme.name)
            gender?.let {
                slot(HumanFields.Gender, gender.name)
            }
            checkCharacter(CoreFields.Instance.fieldName)
        }.demons
}