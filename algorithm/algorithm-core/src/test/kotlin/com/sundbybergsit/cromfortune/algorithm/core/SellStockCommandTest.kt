package com.sundbybergsit.cromfortune.algorithm.core

import org.junit.jupiter.api.Test
import java.util.Currency
import kotlin.test.assertEquals

class SellStockCommandTest {

    @Test
    fun `execute - when repository empty - adds stock order to repository`() {
        val stockName = "stock"
        val command = SellStockCommand(
            currentTimeInMillis = 0L,
            currency = Currency.getInstance("SEK"),
            name = stockName,
            pricePerStock =0.0,
            quantity = 0,
            commissionFee = 0.0
        )
        val repository = TestableStockOrderApi(mutableSetOf())

        command.execute(repository = repository)

        assertEquals(expected = 1, actual = repository.count(stockName = stockName))
    }

}
