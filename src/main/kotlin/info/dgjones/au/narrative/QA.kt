package info.dgjones.au.narrative

import info.dgjones.au.parser.*


class WordWho(): WordHandler(EntryWord("who")) {
    override fun build(wordContext: WordContext): List<Demon> {
        return listOf(WhoDemon(wordContext));
    }
}

class WhoDemon(wordContext: WordContext): Demon(wordContext) {
    override fun run() {
        //FIXME not sure what to use for placeholder
        wordContext.defHolder.value = buildHuman("who", "who", Gender.Male.name)
    }
}