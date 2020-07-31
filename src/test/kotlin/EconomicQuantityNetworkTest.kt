import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EconomicQuantityNetworkTest {
    @Test
    fun `can add quantity to network`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1", Actor("A"))
        assertTrue(network.quantities.contains(eq1)) { "should be able to add quantities to network" }
    }

    @Test
    fun `can link quantities`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1", Actor("A"))
        val eq2 = network.addQuantity("eq2", Actor("A"))
        val link = network.addLink(eq1, eq2, LinkSign.Positive)
        assertNotNull(link) { "should be able to link quantities" }
    }

    @Test
    fun `cannot link quantity to itself`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1", Actor("A"))
        assertThrows(IllegalArgumentException::class.java) { network.addLink(eq1, eq1, LinkSign.Positive) }
    }

    @Test
    fun `causal chain value for positive link should preserve value`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1", Actor("A"))
        val eq2 = network.addQuantity("eq2", Actor("A"))
        network.addLink(eq1, eq2, LinkSign.Positive)
        assertEquals(QuantityValue.Low, network.causalChainValue(QuantityValue.Low, eq1, eq2)) { "should preserve eq2"}
        assertEquals(QuantityValue.High, network.causalChainValue(QuantityValue.High, eq1, eq2)) { "should preserve eq2"}
    }

    @Test
    fun `causal chain value for negative link should negate value`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1", Actor("A"))
        val eq2 = network.addQuantity("eq2", Actor("A"))
        network.addLink(eq1, eq2, LinkSign.Negative)
        assertEquals(QuantityValue.High, network.causalChainValue(QuantityValue.Low, eq1, eq2)) { "should negate eq2"}
        assertEquals(QuantityValue.Low, network.causalChainValue(QuantityValue.High, eq1, eq2)) { "should negate eq2"}
    }

    @Test
    fun `causal chain value is positive for two negate links`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1", Actor("A"))
        val eq2 = network.addQuantity("eq2", Actor("A"))
        val eq3 = network.addQuantity("eq3", Actor("A"))
        network.addLink(eq1, eq2, LinkSign.Negative)
        network.addLink(eq2, eq3, LinkSign.Negative)
        assertEquals(QuantityValue.Low, network.causalChainValue(QuantityValue.Low, eq1, eq3)) { "should double-negate eq2"}
        assertEquals(QuantityValue.High, network.causalChainValue(QuantityValue.High, eq1, eq3)) { "should double-negate eq2"}
    }

    @Test
    fun `causal chain for self should be unit`() {
        val network = EconomicQuantityNetwork("test")
        val eqSelf = network.addQuantity("eqSelf", Actor("A"))
        val eqOther = network.addQuantity("eqOther", Actor("A"))
        network.addLink(eqSelf, eqOther, LinkSign.Negative)
        assertEquals(QuantityValue.Low, network.causalChainValue(QuantityValue.Low, eqSelf, eqSelf)) { "self should be unit"}
    }
}

