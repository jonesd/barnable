val tradeRelationships = buildTradeRelationshipModel()

fun buildTradeRelationshipModel(): EconomicQuantityNetwork {
    val network = EconomicQuantityNetwork("Trade Relationships")
    // producer1
    var producer1 = Actor("P1")
    val earnings = network.addQuantity("Earnings", producer1)
    val costs = network.addQuantity("Costs", producer1)
    network.addLink(costs, earnings, LinkSign.Negative)
    val spending = network.addQuantity("Spending", producer1)
    network.addLink(spending, costs, LinkSign.Positive)
    network.addLink(earnings, spending, LinkSign.Positive)
    val salaries = network.addQuantity("Salaries", producer1)
    network.addLink(salaries, costs, LinkSign.Positive)
    network.addLink(earnings, salaries, LinkSign.Positive)
    val employment = network.addQuantity("Employment", producer1)
    network.addLink(employment, costs, LinkSign.Positive)
    network.addLink(earnings, employment, LinkSign.Positive)
    val salesOfP1 = network.addQuantity("Sales of P1", producer1)
    network.addLink(salesOfP1, earnings, LinkSign.Positive)
    val priceOfP1 = network.addQuantity("Price of P1", producer1)
    network.addLink(costs, priceOfP1, LinkSign.Positive)
    // consumer
    var consumer1 = Actor("C1")
    val spendingOnP1 = network.addQuantity("Spending on P1", consumer1)
    val spendingOnP2 = network.addQuantity("Spending on P2", consumer1)
    network.addLink(spendingOnP1, spendingOnP2, LinkSign.Negative)
    network.addLink(spendingOnP2, spendingOnP1, LinkSign.Negative)
    network.addLink(priceOfP1, spendingOnP1, LinkSign.Negative)
    network.addLink(priceOfP1, spendingOnP2, LinkSign.Positive)
    network.addLink(spendingOnP1, salesOfP1, LinkSign.Positive)
    //producer2
    var producer2 = Actor("P2")
    val priceOfP2 = network.addQuantity("Price of P2", producer2)
    //network.addLink(priceOfP2, spendingOnP1, LinkSign.Positive)
    network.addLink(priceOfP2, spendingOnP2, LinkSign.Negative)

    return network
}
