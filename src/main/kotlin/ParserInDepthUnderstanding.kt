fun buildInDepthUnderstandingLexicon(): Lexicon {
    val lexicon = Lexicon();
    lexicon.addMapping(WordIgnore("up"))
    lexicon.addMapping(WordIgnore("the"))
    //FIXME lexicon.addMapping(WordIgnoreHandler("and"))

    lexicon.addMapping(WordBall())
    lexicon.addMapping(WordBox())
    lexicon.addMapping(WordJohn())

    // FIXME lexicon.addMapping(WordCharacterHandler("John", Gender.Male))
    return lexicon
}

enum class Gender {
    Male,
    Female,
    Other
}

class Human(val firstName: String, val lastName: String, val gender: Gender): Concept(firstName)

enum class PhysicalObjectKind() {
    Container,
    GameObject
}

open class PhysicalObject(val kind: PhysicalObjectKind, name: String): Concept(name)

class WordBall(): WordHandler("ball") {
    override fun build(): List<Demon> {
        def = PhysicalObject(PhysicalObjectKind.GameObject, "ball")
        return listOf(SaveObjectDemon(this))
    }
}

class WordBox(): WordHandler("box") {
    override fun build(): List<Demon> {
        def = PhysicalObject(PhysicalObjectKind.Container, "box")
        return listOf()
    }
}

class WordJohn(): WordHandler("john") {
    override fun build(): List<Demon> {
        def = Human("John", "", Gender.Male)
        return listOf(SaveCharacterDemon(this))
    }
}

class SaveCharacterDemon(wordHandler: WordHandler): Demon(wordHandler){
    override fun run(context: SentenceContext) {
        val character = wordHandler.def
        if (character is Human) {
            context.mostRecentCharacter = character
            if (context.localCharacter != null) {
                context.localCharacter = character
            }
            active = false
        }
    }
}

class SaveObjectDemon(wordHandler: WordHandler): Demon(wordHandler) {
    override fun run(context: SentenceContext) {
        val o = wordHandler.def
        if (o != null) {
            context.mostRecentObject = o
            active = false
        }
    }
}
