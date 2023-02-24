package com.sundbybergsit.cromfortune

import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class CromTestRule : TestRule {

    override fun apply(base: Statement, description: Description) = CromTestBeforeAfter(base)

    class CromTestBeforeAfter(private val base: Statement) : Statement() {

        @Throws(Throwable::class)
        override fun evaluate() {
            StockPriceRepository.clear()
            CurrencyRateRepository.clear()
            try {
                // Execute test
                base.evaluate()
            } finally {
                // After
                StockPriceRepository.clear()
                CurrencyRateRepository.clear()
            }
        }

    }

}

