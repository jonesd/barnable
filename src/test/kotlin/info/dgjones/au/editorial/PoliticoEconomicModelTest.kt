package info.dgjones.au.editorial

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PoliticoEconomicModelTest {
    @Test
    fun `casual chain for low price on p2`() {
        val network = buildTradeRelationshipModel()
        val priceOfP2 = network.findQuantity("Price of P2")
        val earnings = network.findQuantity("Earnings")
        assertEquals(QuantityValue.Low, network.causalChainValue(QuantityValue.Low, priceOfP2, earnings))
    }

    @Test
    fun `causal chain for low price on p2 for Earnings of p1`() {
        val network = buildTradeRelationshipModel()
        val priceOfP2 = network.findQuantity("Price of P2")
        val earnings = network.findQuantity("Earnings")
        val path = network.causalChain(QuantityValue.Low, priceOfP2, earnings)
        print(path)
        assertEquals(QuantityValue.Low, path.last().value)
    }

    @Test
    fun `causal chain for decrease on Spending on p2 for Earnings of P1`() {
        val network = buildTradeRelationshipModel()
        val priceOfP2 = network.findQuantity("Spending on P2")
        val earnings = network.findQuantity("Earnings")
        val path = network.causalChain(QuantityValue.Low, priceOfP2, earnings)
        print(path)
        assertEquals(QuantityValue.High, path.last().value)
    }
}