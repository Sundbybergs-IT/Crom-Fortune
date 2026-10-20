package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.TransactionAction
import com.sundbybergsit.cromfortune.main.CromTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockOrderRepositoryTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @get:Rule
    val cromTestRule = CromTestRule()

    private val portfolioName = "test-portfolio"

    @Before
    fun setUp() {
        com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase.getInstance(context).clearAllTables()
    }

    @Test
    fun `list and listOfAssetNames return stock transactions saved in AssetTransactionRepository`() {
        val teslaAsset = requireNotNull(AssetCatalog.findBySymbol(AssetType.STOCK, "TSLA"))
        val transactionRepository = AssetTransactionRepository(context, portfolioName)
        val stockOrderRepository = StockOrderRepository(context, portfolioName)

        val transaction = AssetTransaction(
            assetId = teslaAsset.id,
            assetType = teslaAsset.type,
            symbol = teslaAsset.symbol,
            displayName = teslaAsset.displayName,
            quoteCurrencyCode = teslaAsset.quoteCurrency.currencyCode,
            action = TransactionAction.BUY,
            dateInMillis = 1000L,
            unitPrice = BigDecimal("150.0"),
            quantity = BigDecimal("5")
        )
        transactionRepository.putAll(teslaAsset.id, setOf(transaction))

        val names = stockOrderRepository.listOfAssetNames().toList()
        assertTrue(names.contains("TSLA"))

        val orders = stockOrderRepository.list("TSLA")
        assertEquals(1, orders.size)
        val order = orders.first()
        assertEquals("Buy", order.orderAction)
        assertEquals("TSLA", order.name)
        assertEquals(150.0, order.pricePerStock, 0.001)
        assertEquals(BigDecimal("5"), order.quantity)
    }

    @Test
    fun `saving identical transactions retains all transactions in stockOrderRepository`() {
        val teslaAsset = requireNotNull(AssetCatalog.findBySymbol(AssetType.STOCK, "TSLA"))
        val transactionRepository = AssetTransactionRepository(context, portfolioName)
        val stockOrderRepository = StockOrderRepository(context, portfolioName)

        val transaction = AssetTransaction(
            assetId = teslaAsset.id,
            assetType = teslaAsset.type,
            symbol = teslaAsset.symbol,
            displayName = teslaAsset.displayName,
            quoteCurrencyCode = teslaAsset.quoteCurrency.currencyCode,
            action = TransactionAction.BUY,
            dateInMillis = 1000L,
            unitPrice = BigDecimal("150.0"),
            quantity = BigDecimal("5")
        )
        transactionRepository.putAll(teslaAsset.id, listOf(transaction, transaction))

        val orders = stockOrderRepository.list("TSLA")
        assertEquals(2, orders.size)
    }
}
