val tradeRelationships = buildTradeRelationshipModel()

fun buildTradeRelationshipModel(): EconomicQuantityNetwork {
    val network = EconomicQuantityNetwork("Trade Relationships")
    // producer1
    val earnings = network.addQuantity("Earnings")
    val costs = network.addQuantity("Costs")
    network.addLink(costs, earnings, LinkSign.Negative)
    val spending = network.addQuantity("Spending")
    network.addLink(spending, costs, LinkSign.Positive)
    network.addLink(earnings, spending, LinkSign.Positive)
    val salaries = network.addQuantity("Salaries")
    network.addLink(salaries, costs, LinkSign.Positive)
    network.addLink(earnings, salaries, LinkSign.Positive)
    val employment = network.addQuantity("Employment")
    network.addLink(employment, costs, LinkSign.Positive)
    network.addLink(earnings, employment, LinkSign.Positive)
    val salesOfP1 = network.addQuantity("Sales of P1")
    network.addLink(salesOfP1, earnings, LinkSign.Positive)
    val priceOfP1 = network.addQuantity("Price of P1")
    network.addLink(costs, priceOfP1, LinkSign.Positive)
    // consumer
    val spendingOnP1 = network.addQuantity("Spending on P1")
    val spendingOnP2 = network.addQuantity("Spending on P2")
    network.addLink(spendingOnP1, spendingOnP2, LinkSign.Negative)
    network.addLink(spendingOnP2, spendingOnP1, LinkSign.Negative)
    network.addLink(priceOfP1, spendingOnP1, LinkSign.Negative)
    network.addLink(priceOfP1, spendingOnP2, LinkSign.Positive)
    network.addLink(spendingOnP1, salesOfP1, LinkSign.Positive)
    //producer2
    val priceOfP2 = network.addQuantity("Price of P2")
    //network.addLink(priceOfP2, spendingOnP1, LinkSign.Positive)
    network.addLink(priceOfP2, spendingOnP2, LinkSign.Negative)

    return network
}
