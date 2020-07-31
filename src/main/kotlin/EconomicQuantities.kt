import org.jgrapht.*
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge

class EconomicQuantityNetwork(name: String) {
    val quantities = mutableListOf<EconomicQuantity>()
    private val links = mutableListOf<QuantityLink>()
    private val quantitiesMap = mutableMapOf<String, EconomicQuantity>()
    private val linksMap = mutableMapOf<Pair<String,String>, QuantityLink>()

    private val directedGraph: Graph<String, DefaultEdge> = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

    fun findQuantity(name: String): EconomicQuantity {
        return quantities.find { it.name == name } ?: throw IllegalArgumentException("Not found: $name")
    }

    fun addLink(from: EconomicQuantity, to: EconomicQuantity, sign: LinkSign): QuantityLink {
        require(from != to)
        directedGraph.addEdge(from.name, to.name)
        val link = QuantityLink(from, to, sign)
        links.add(link)
        linksMap[Pair(from.name, to.name)] = link
        return link
    }
    fun addQuantity(name: String, actor: Actor): EconomicQuantity {
        val added = directedGraph.addVertex(name)
        require(added)
        val quantity = EconomicQuantity(name, actor)
        quantities.add(quantity)
        quantitiesMap[name] = quantity
        return quantity
    }

    fun causalChainValue(sourceValue: QuantityValue, source: EconomicQuantity, result: EconomicQuantity): QuantityValue {
        val path = causalChain(sourceValue, source, result)
        return path.last().value
    }

    fun causalChain(sourceValue: QuantityValue, source: EconomicQuantity, result: EconomicQuantity): List<EconomicQuantityValue> {
        val path = shortestPath(source, result)
        return calculatePathValues(sourceValue, path)
    }

    fun calculatePathValues(startingValue: QuantityValue, path: List<EconomicQuantity>): List<EconomicQuantityValue>  {
        var currentValue = startingValue
        val linkValues = mapPathToLinks(path).map {
            currentValue = transformQuantityValue(currentValue, it.sign)
            EconomicQuantityValue(it.to, currentValue)}
        return listOf(EconomicQuantityValue(path[0], startingValue)) + linkValues
    }

    private fun mapPathToLinks(path: List<EconomicQuantity>): List<QuantityLink> {
        return path.windowed(2).mapNotNull { linksMap[Pair(it[0].name, it[1].name)] }
    }

    private fun shortestPath(source: EconomicQuantity, result: EconomicQuantity): List<EconomicQuantity> {
        val pathNames = shortestPath(source.name, result.name)
        return pathNames.mapNotNull { quantitiesMap[it] }
    }

    private fun shortestPath(sourceName: String, destinationName: String): List<String> {
        val dijkstraShortestPath = DijkstraShortestPath(directedGraph)
        return dijkstraShortestPath.getPath(sourceName, destinationName).vertexList
    }
}

enum class LinkSign {
    Positive,
    Negative
}

enum class QuantityValue {
    Low,
    High
}

fun transformQuantityValue(value: QuantityValue, sign: LinkSign): QuantityValue {
    return if (sign == LinkSign.Negative) {
        if (value == QuantityValue.Low) {
            QuantityValue.High
        } else {
            QuantityValue.Low
        }
    } else {
        value
    }
}

class Actor(val name: String) {
    override fun toString(): String {
        return "$name"
    }
}

class EconomicQuantity(val name: String, val actor: Actor) {
    override fun toString(): String {
        return "$name by $actor"
    }
}

class EconomicQuantityValue(val quantity: EconomicQuantity, val value: QuantityValue) {
    override fun toString(): String {
        return "$value $quantity"
    }
}

class QuantityLink(val from: EconomicQuantity, val to: EconomicQuantity, val sign: LinkSign) {
    override fun toString(): String {
        val signMarker = if (sign == LinkSign.Positive) '+' else '-'
        return "${from.name} .${signMarker}.> ${to.name}"
    }
}