package com.sundbybergsit.cromfortune.main.stocks

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockEventRepositoryTest {

    private lateinit var repository: StockEventRepository

    @Before
    fun setUp() {
        repository = StockEventRepository(
            ApplicationProvider.getApplicationContext(),
            portfolioName = "test",
            stockOrderApi = StubbedStockOrderApi(),
            stockSplitApi = StubbedStockSplitApi(),
        )
    }

    @Test
    fun countCurrent() {
        val count = repository.countCurrent(stockSymbol ="Test")

        assertEquals(actual = count, expected = 0)
    }

    private class StubbedStockSplitApi : StockSplitApi {

        override fun list(stockName: String): Set<StockSplit> {
            return setOf(StockSplit(false, 3L, stockName, 2))
        }

        override fun putAll(stockName: String, stockSplits: Set<StockSplit>) {
            TODO("Not yet implemented")
        }

        override fun putReplacingAll(stockName: String, stockSplit: StockSplit) {
            TODO("Not yet implemented")
        }

        override fun remove(stockName: String) {
            TODO("Not yet implemented")
        }

        override fun remove(stockSplit: StockSplit) {
            TODO("Not yet implemented")
        }

    }

    private class StubbedStockOrderApi : StockOrderApi {

        override fun count(stockSymbol: String): Int {
            TODO("Not yet implemented")
        }

        override fun countAll(): Int {
            TODO("Not yet implemented")
        }

        override fun listOfStockNames(): Iterable<String> {
            TODO("Not yet implemented")
        }

        override fun isEmpty(): Boolean {
            TODO("Not yet implemented")
        }

        override fun list(stockSymbol: String): Set<StockOrder> {
            return setOf(
                StockOrder("Buy", "SEK", 0L, stockSymbol, 1.0, 0.0, 1),
                StockOrder("Sell", "SEK", 5L, stockSymbol, 1.0, 0.0, 2),
            )
        }

        override fun putAll(stockSymbol: String, stockOrders: Set<StockOrder>) {
            TODO("Not yet implemented")
        }

        override fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder) {
            TODO("Not yet implemented")
        }

        override fun remove(stockSymbol: String) {
            TODO("Not yet implemented")
        }

        override fun remove(stockOrder: StockOrder) {
            TODO("Not yet implemented")
        }

    }

}
