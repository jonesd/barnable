class WordRecentHandler() : WordHandler("recent") {
    /*
    override fun createDemons(): List<Demon> {
        return listOf(
            object : Demon("protectionist-belief") {

            }
        )
    }*/
}

class WordHurtHandler() : WordHandler("hurt") {
    /*
    override fun createDemons(): List<Demon> {
        return listOf(
            // FIXME physical-injury 196
            object : Demon("economic-injury") {

            }
        )
    }*/
}
/* FIXME
// Core Concepts
class Goal(name: String): Concept(name)
class Plan(name: String): Concept(name)
class Event(name: String): Concept(name)

// Editorial Text Concepts
class Authority(name: String): Concept(name)
class Institution(name: String): Concept(name)
class Country(name: String): Concept(name)
class Product(name: String): Concept(name)
class EconomicQuantityConcept(name: String): Concept(name)
class Occupation(name: String): Concept(name)
*/
val ConceptAuthorities = listOf("the Reagan administration")
val ConceptInstitutions = listOf("the Common Market", "steel industry", "automobile industry")
val ConceptCountries = listOf("United States", "Japan")
val ConceptProducts = listOf("imports", "exports", "steel", "automobile")
val ConceptEconomicQuantities = listOf("earnings", "spending", "cost")
val ConceptOccupations = listOf("jobs in export industries")
val ConceptGoals = listOf("Saving jobs", "Attaining economic health of industries")
val ConceptPlans = listOf("Protectionist Policies")
val ConceptEvents = listOf("importing", "exporting")
