package com.sundbybergsit.cromfortune.main.ui.home

import java.math.BigDecimal
import java.math.RoundingMode

/** Cash ledger used while replaying portfolio transactions. */
class SimulatedCashWallet(initialCreditSek: BigDecimal = BigDecimal.ZERO) {
    var creditSek: BigDecimal = initialCreditSek
        private set

    fun fund(amountSek: BigDecimal) {
        require(amountSek.signum() >= 0) { "Funding cannot be negative" }
        creditSek += amountSek
    }

    /**
     * Pays a purchase from this wallet and returns the new outside capital needed
     * to cover any shortfall.
     */
    fun buyWithTopUp(
        quantity: BigDecimal,
        pricePerStock: Double,
        rateInSek: Double,
        commissionFeeSek: Double
    ): BigDecimal {
        val cost = quantity * pricePerStock.toBigDecimal() * rateInSek.toBigDecimal() +
            commissionFeeSek.toBigDecimal()
        val topUp = (cost - creditSek).max(BigDecimal.ZERO)
        creditSek += topUp
        creditSek -= cost
        return topUp
    }

    fun maximumAffordableQuantity(pricePerStock: Double, rateInSek: Double, commissionFeeSek: Double): Int {
        val unitPriceSek = pricePerStock.toBigDecimal() * rateInSek.toBigDecimal()
        if (unitPriceSek.signum() <= 0) return 0
        val availableForStocks = creditSek - commissionFeeSek.toBigDecimal()
        if (availableForStocks.signum() <= 0) return 0
        return availableForStocks.divide(unitPriceSek, 0, RoundingMode.DOWN)
            .min(Int.MAX_VALUE.toBigDecimal()).toInt()
    }

    fun buy(quantity: Int, pricePerStock: Double, rateInSek: Double, commissionFeeSek: Double) {
        val cost = quantity.toBigDecimal() * pricePerStock.toBigDecimal() * rateInSek.toBigDecimal() +
            commissionFeeSek.toBigDecimal()
        require(cost <= creditSek) { "A simulated wallet cannot spend more than its available credit" }
        creditSek -= cost
    }

    fun sell(quantity: Int, pricePerStock: Double, rateInSek: Double, commissionFeeSek: Double) {
        sell(
            quantity = quantity.toBigDecimal(),
            pricePerStock = pricePerStock,
            rateInSek = rateInSek,
            commissionFeeSek = commissionFeeSek
        )
    }

    fun sell(quantity: BigDecimal, pricePerStock: Double, rateInSek: Double, commissionFeeSek: Double) {
        creditSek += quantity * pricePerStock.toBigDecimal() * rateInSek.toBigDecimal() -
            commissionFeeSek.toBigDecimal()
    }
}
