import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EconomicQuantityNetworkTest {
    @Test
    fun `can add quantity to network`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1");
        assertTrue(network.quantities.contains(eq1)) { "should be able to add quantities to network" }
    }

    @Test
    fun `can link quantities`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1")
        val eq2 = network.addQuantity("eq2")
        val link = network.addLink(eq1, eq2, LinkSign.Positive)
        assertNotNull(link) { "should be able to link quantities" }
    }

    @Test
    fun `cannot link quantity to itself`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1")
        assertThrows(IllegalArgumentException::class.java) { network.addLink(eq1, eq1, LinkSign.Positive) }
    }

    @Test
    fun `run update for positive link should preserve value`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1")
        val eq2 = network.addQuantity("eq2")
        val link = network.addLink(eq1, eq2, LinkSign.Positive)
        assertEquals(QuantityValue.Low, network.run(eq1, eq2, QuantityValue.Low)) { "should preserve eq2"}
        assertEquals(QuantityValue.High, network.run(eq1, eq2, QuantityValue.High)) { "should preserve eq2"}
    }

    @Test
    fun `run update for negative link should negate value`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1")
        val eq2 = network.addQuantity("eq2")
        val link = network.addLink(eq1, eq2, LinkSign.Negative)
        assertEquals(QuantityValue.High, network.run(eq1, eq2, QuantityValue.Low)) { "should negate eq2"}
        assertEquals(QuantityValue.Low, network.run(eq1, eq2, QuantityValue.High)) { "should negate eq2"}
    }

    @Test
    fun `run update be positive for two negate linkns`() {
        val network = EconomicQuantityNetwork("test")
        val eq1 = network.addQuantity("eq1")
        val eq2 = network.addQuantity("eq2")
        val eq3 = network.addQuantity("eq3")
        val link12 = network.addLink(eq1, eq2, LinkSign.Negative)
        val link23 = network.addLink(eq2, eq3, LinkSign.Negative)
        assertEquals(QuantityValue.Low, network.run(eq1, eq3, QuantityValue.Low)) { "should double-negate eq2"}
        assertEquals(QuantityValue.High, network.run(eq1, eq3, QuantityValue.High)) { "should double-negate eq2"}
    }
}

