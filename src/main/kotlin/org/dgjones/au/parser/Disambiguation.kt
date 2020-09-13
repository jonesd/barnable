package org.dgjones.au.parser

/*
Disambiguation is the process of deciding which word sense is selected when a word/expression has two or more meanings in
the Lexicon. There are two ways that can be accomplished:

- Top-Down involves demons NOT belonging to the word expecting a specific sense, and the first one to match a meaning
will trigger that specific word sense to be selected.
- Bottom-Up involves disambiguation demons associated with the word sense searching the current context to decide which
sense is appropriate.

See: InDepth p180
 */
open class DisambiguationDemon(val disambiguationHandler: DisambiguationHandler, wordContext: WordContext): Demon(wordContext) {
    fun disambiguationCompleted() {
        disambiguationHandler.disambigationMatchCompleted(this)
        active = false
    }
}

class DisambiguateUsingWord(val word: String, val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, wordContext: WordContext, disambiguationHandler: DisambiguationHandler): DisambiguationDemon(disambiguationHandler, wordContext) {
    override fun run() {
        searchContext(matcher, matchPreviousWord = word, direction = direction, wordContext = wordContext) {
            disambiguationCompleted()
        }
    }
    override fun description(): String {
        return "DisambiguateUsingWord word=$word"
    }
}

class DisambiguateUsingMatch(val matcher: ConceptMatcher, val direction: SearchDirection = SearchDirection.After, val distance: Int? = null, wordContext: WordContext, disambiguationHandler: DisambiguationHandler): DisambiguationDemon(disambiguationHandler, wordContext) {
    override fun run() {
        searchContext(matcher, direction = direction, distance = distance, wordContext = wordContext) {
            disambiguationCompleted()
        }
    }
    override fun description(): String {
        return "DisambiguateUsingMatch"
    }
}