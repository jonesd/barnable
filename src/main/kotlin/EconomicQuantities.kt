import org.jgrapht.*
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge

class EconomicQuantityNetwork(name: String) {
    val quantities = mutableListOf<EconomicQuantity>()
    val links = mutableListOf<QuantityLink>()
    val quantitiesMap = mutableMapOf<String, EconomicQuantity>()
    val linksMap = mutableMapOf<Pair<String,String>, QuantityLink>()

    var directedGraph: Graph<String, DefaultEdge> = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

    fun findQuantity(name: String): EconomicQuantity {
        return quantities.find { it.name == name } ?: throw IllegalArgumentException("Not found: $name")
    }

    fun addLink(from: EconomicQuantity, to: EconomicQuantity, sign: LinkSign): QuantityLink {
        require(from != to)
        directedGraph.addEdge(from.name, to.name)
        val link = QuantityLink(from, to, sign)
        links.add(link)
        linksMap.put(Pair(from.name, to.name), link)
        return link
    }
    fun addQuantity(name: String): EconomicQuantity {
        val added = directedGraph.addVertex(name)
        require(added)
        var quantity = EconomicQuantity(name)
        quantities.add(quantity)
        quantitiesMap.put(name, quantity)
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
        val linkValues = mapPathToLinks(path).map() {
            currentValue = transformQuantityValue(currentValue, it.sign)
            EconomicQuantityValue(it.to, currentValue)}
        return listOf(EconomicQuantityValue(path[0], startingValue)) + linkValues
    }

    private fun mapPathToLinks(path: List<EconomicQuantity>): List<QuantityLink> {
        return path.windowed(2).map { linksMap[Pair(it[0].name, it[1].name)] }.filterNotNull()
    }

    private fun shortestPath(source: EconomicQuantity, result: EconomicQuantity): List<EconomicQuantity> {
        val pathNames = shortestPath(source.name, result.name)
        return pathNames.map { quantitiesMap[it] }.filterNotNull()
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
    if (sign == LinkSign.Negative) {
        if (value == QuantityValue.Low) {
            return QuantityValue.High
        } else {
            return QuantityValue.Low
        }
    } else {
        return value
    }
}

class EconomicQuantity(val name: String) {
    override fun toString(): String {
        return "EconomicQuantity($name)"
    }
}

class EconomicQuantityValue(val quantity: EconomicQuantity, val value: QuantityValue) {
    override fun toString(): String {
        return "$quantity=$value"
    }
}

class QuantityLink(val from: EconomicQuantity, val to: EconomicQuantity, val sign: LinkSign) {
    override fun toString(): String {
        val signMarker = if (sign == LinkSign.Positive) '+' else '-'
        return "${from.name} .${signMarker}.> ${to.name}"
    }
}