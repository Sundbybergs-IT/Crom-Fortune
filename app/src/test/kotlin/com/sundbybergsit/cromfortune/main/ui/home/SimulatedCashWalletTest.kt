package com.sundbybergsit.cromfortune.main.ui.home

import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SimulatedCashWalletTest {

    @Test
    fun `unused funding remains available for a later purchase`() {
        val wallet = SimulatedCashWallet()

        wallet.fund(amountSek = BigDecimal("100"))
        wallet.fund(amountSek = BigDecimal("50"))
        wallet.buy(quantity = 3, pricePerStock = 40.0, rateInSek = 1.0, commissionFeeSek = 0.0)

        assertEquals(BigDecimal("30.00"), wallet.creditSek)
    }

    @Test
    fun `buy quantity is capped by available credit including commission`() {
        val wallet = SimulatedCashWallet(initialCreditSek = BigDecimal("100"))

        assertEquals(
            9,
            wallet.maximumAffordableQuantity(pricePerStock = 10.0, rateInSek = 1.0, commissionFeeSek = 5.0)
        )
    }

    @Test
    fun `sale proceeds are returned to credit`() {
        val wallet = SimulatedCashWallet(initialCreditSek = BigDecimal("10"))

        wallet.sell(quantity = 2, pricePerStock = 50.0, rateInSek = 1.0, commissionFeeSek = 5.0)

        assertEquals(BigDecimal("105.00"), wallet.creditSek)
    }

    @Test
    fun `wallet rejects overspending`() {
        val wallet = SimulatedCashWallet(initialCreditSek = BigDecimal("99"))

        assertFailsWith<IllegalArgumentException> {
            wallet.buy(quantity = 10, pricePerStock = 10.0, rateInSek = 1.0, commissionFeeSek = 0.0)
        }
    }

    @Test
    fun `purchase uses sale proceeds before requesting outside capital`() {
        val wallet = SimulatedCashWallet()
        wallet.sell(quantity = BigDecimal("10"), pricePerStock = 10.0, rateInSek = 1.0, commissionFeeSek = 0.0)

        val firstTopUp = wallet.buyWithTopUp(
            quantity = BigDecimal("8"), pricePerStock = 10.0, rateInSek = 1.0, commissionFeeSek = 0.0
        )
        val secondTopUp = wallet.buyWithTopUp(
            quantity = BigDecimal("7"), pricePerStock = 10.0, rateInSek = 1.0, commissionFeeSek = 0.0
        )

        assertEquals(BigDecimal.ZERO, firstTopUp)
        assertEquals(BigDecimal("50.00"), secondTopUp)
        assertEquals(BigDecimal("0.00"), wallet.creditSek)
    }
}
