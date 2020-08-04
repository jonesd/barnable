class Lexicon() {
    val wordMappings: MutableMap<String, MutableList<WordHandler>> = mutableMapOf()
    fun addConcepts(concept: Concept, phrases: List<String>) {
        phrases.forEach {
            val handler = ConceptHandler(concept, it)
            addMapping(handler)
        }
    }

    fun addConcept(concept: Concept, phrase: String): WordHandler {
        return ConceptHandler(concept, phrase)
    }

    fun addMapping(handler: WordHandler) {
        val key = handler.matchingTriggerWord()
        val values = wordMappings.getOrElse(key) {
            val v = mutableListOf<WordHandler>()
            wordMappings.put(key, v)
            v
        }
        values.add(handler)
    }

    fun createDemons(word: String): List<Demon>? {
        // FIXME what kind of word? should we be testing lemmas/word/etc?
        // FIXME should we be able to create more demons that already exist in the sentence agenda?
        return wordMappings[word]?.flatMap { it.createDemons() }
    }
}

// FIXME move population elsewhere
fun buildLexicon() {
    val lexicon = Lexicon();
    buildConceptHandlers(lexicon)
    lexicon.addMapping(WordRecentHandler())
    return
}

private fun buildConceptHandlers(lexicon: Lexicon) {
    lexicon.addConcepts(Concept.Authority, ConceptAuthorities)
    lexicon.addConcepts(Concept.Institution, ConceptInstitutions)
    lexicon.addConcepts(Concept.Country, ConceptCountries)
    lexicon.addConcepts(Concept.Product, ConceptProducts)
    lexicon.addConcepts(Concept.EconomicQuantity, ConceptEconomicQuantities)
    lexicon.addConcepts(Concept.Occupation, ConceptOccupations)
    lexicon.addConcepts(Concept.Goal, ConceptGoals)
    lexicon.addConcepts(Concept.Plan, ConceptPlans)
    lexicon.addConcepts(Concept.Event, ConceptEvents)
}

val ConceptAuthorities = listOf("the Reagan administration")
val ConceptInstitutions = listOf("the Common Market", "steel industry", "automobile industry")
val ConceptCountries = listOf("United States", "Japan")
val ConceptProducts = listOf("imports", "exports", "steel", "automobile")
val ConceptEconomicQuantities = listOf("earnings", "spending", "cost")
val ConceptOccupations = listOf("jobs in export industries")
val ConceptGoals = listOf("Saving jobs", "Attaining economic health of industries")
val ConceptPlans = listOf("Protectionist Policies")
val ConceptEvents = listOf("importing", "exporting")

open abstract class WordHandler {
    abstract fun matchingTriggerWord(): String
    open fun conceptMatch(matchConcept: Concept): String? {
        return null
    }
    open fun createDemons(): List<Demon> {
        return listOf<Demon>()
    }
}

class ConceptHandler(val concept: Concept, val phrase: String) : WordHandler() {
    val elements = phrase.split("\\W+".toRegex())
    init {
        require(elements.size > 0)
    }
    override fun matchingTriggerWord(): String {
        return elements[0]
    }
    override fun conceptMatch(matchConcept: Concept): String? {
        return if (concept == matchConcept) phrase else null
    }
}

open class Demon(name: String) {
    fun run(working: WorkingMemory, context: SentenceContext) {
        // FIXME
    }
}

class WordRecentHandler() : WordHandler() {
    override fun matchingTriggerWord(): String = "recent"
    override fun createDemons(): List<Demon> {
        return listOf(
            object : Demon("protectionist-belief") {

            }
        )
    }
}

class WordHurtHandler() : WordHandler() {
    override fun matchingTriggerWord(): String = "hurt"
    override fun createDemons(): List<Demon> {
        return listOf(
            // FIXME physical-injury 196
            object : Demon("economic-injury") {

            }
        )

    }
}