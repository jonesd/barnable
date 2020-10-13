package info.dgjones.au.domain.general

import info.dgjones.au.concept.CoreFields
import info.dgjones.au.concept.Fields
import info.dgjones.au.concept.lexicalConcept
import info.dgjones.au.narrative.InDepthUnderstandingConcepts
import info.dgjones.au.parser.*

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

class RoleThemeWord(word: String, val roleTheme: RoleTheme, val gender: Gender? = null): WordHandler(EntryWord(word)) {
    override fun build(wordContext: WordContext): List<Demon> {
        val lexicalConcept = lexicalConcept(wordContext, InDepthUnderstandingConcepts.Human.name) {
            slot(RoleThemeFields.RoleTheme, roleTheme.name)
            gender?.let {
                slot(Human.GENDER, gender.name)
            }
            checkCharacter(CoreFields.INSTANCE.fieldName)
        }
        return lexicalConcept.demons
    }
}