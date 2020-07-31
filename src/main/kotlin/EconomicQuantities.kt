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
    //var g: Graph<String, DefaultEdge> = SimpleGraph<String, DefaultEdge>(DefaultEdge::class.java)

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
    fun run(source: EconomicQuantity, result: EconomicQuantity, value: QuantityValue): QuantityValue {
        val path = shortestPath(source, result)
        return calculateValue(value, path)
    }

    fun calculateValue(startingValue: QuantityValue, path: List<QuantityLink>): QuantityValue {
        return path.fold(startingValue) { currentValue, link -> transformQuantityValue(currentValue, link.sign) }
    }

    fun shortestPath(source: EconomicQuantity, result: EconomicQuantity): List<QuantityLink> {
        val dijkstraShortestPath = DijkstraShortestPath(directedGraph)
        var shortestPath: List<String> = dijkstraShortestPath.getPath(source.name, result.name).getVertexList()
        return shortestPath.windowed(2).map { linksMap[Pair(it[0], it[1])] }.filterNotNull()
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

}

class QuantityLink(val from: EconomicQuantity, val to: EconomicQuantity, val sign: LinkSign) {
    
}