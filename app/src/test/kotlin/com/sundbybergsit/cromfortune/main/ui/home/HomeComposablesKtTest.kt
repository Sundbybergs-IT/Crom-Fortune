package com.sundbybergsit.cromfortune.main.ui.home

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.TransactionAction
import com.sundbybergsit.cromfortune.main.CoroutineScopeTestRule
import com.sundbybergsit.cromfortune.main.CromTestRule
import com.sundbybergsit.cromfortune.main.Databases
import com.sundbybergsit.cromfortune.main.PortfolioRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.util.Currency
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class HomeComposablesKtTest {

    @get:Rule
    val cromTestRule = CromTestRule()

    @get:Rule
    val coroutineScopeTestRule = CoroutineScopeTestRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val sharedPreferences = context.getSharedPreferences(TEST_CLASS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, setOf(TEST_PORTFOLIO_NAME))
            .commit()
        PortfolioRepository.init(sharedPreferences)
        PortfolioRepository.setCurrentPortfolio(TEST_PORTFOLIO_NAME)
        context.getSharedPreferences(TEST_PORTFOLIO_NAME, Context.MODE_PRIVATE).edit().clear().commit()
        viewModel = HomeViewModel(
            portfolioRepository = PortfolioRepository,
            ioDispatcher = coroutineScopeTestRule.testDispatcher
        )
    }

    @Test
    fun `Home reflects alphabetical sorting changes`() {
        seedPortfolio()
        setContent()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.portfoliosStateFlow.value[TEST_PORTFOLIO_NAME]?.items?.size == 2
        }

        composeTestRule.onNodeWithText(TESLA_NAME).assertIsDisplayed()
        composeTestRule.onNodeWithText(INTEL_NAME).assertIsDisplayed()

        val teslaTopBefore = nodeTop(TESLA_NAME)
        val intelTopBefore = nodeTop(INTEL_NAME)
        assertTrue(intelTopBefore < teslaTopBefore)

        viewModel.sortNameDescending(TEST_PORTFOLIO_NAME)
        composeTestRule.waitForIdle()

        val teslaTopAfter = nodeTop(TESLA_NAME)
        val intelTopAfter = nodeTop(INTEL_NAME)
        assertTrue(teslaTopAfter < intelTopAfter)
    }

    @Test
    fun `Home reflects profit sorting changes`() {
        seedPortfolio()
        setContent()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.portfoliosStateFlow.value[TEST_PORTFOLIO_NAME]?.items?.size == 2
        }

        StockPriceRepository.put(
            setOf(
                StockPrice(stockSymbol = "TSLA", currency = Currency.getInstance("USD"), price = 30.0),
                StockPrice(stockSymbol = "INTC", currency = Currency.getInstance("USD"), price = 20.0)
            )
        )
        composeTestRule.waitForIdle()

        val teslaTopBefore = nodeTop(TESLA_NAME)
        val intelTopBefore = nodeTop(INTEL_NAME)
        assertTrue(intelTopBefore < teslaTopBefore)

        viewModel.sortProfitDescending(TEST_PORTFOLIO_NAME)
        composeTestRule.waitForIdle()

        val teslaTopAfter = nodeTop(TESLA_NAME)
        val intelTopAfter = nodeTop(INTEL_NAME)
        assertTrue(teslaTopAfter < intelTopAfter)
    }

    @Test
    fun `crypto transaction uses asset repository and holding path`() {
        val bitcoin = AssetCatalog.cryptocurrencies.first()
        val transaction = AssetTransaction(
            assetId = bitcoin.id,
            assetType = bitcoin.type,
            symbol = bitcoin.symbol,
            displayName = bitcoin.displayName,
            quoteCurrencyCode = bitcoin.quoteCurrency.currencyCode,
            action = TransactionAction.BUY,
            dateInMillis = 1L,
            unitPrice = BigDecimal("60000.123456"),
            quantity = BigDecimal("0.00000001")
        )

        viewModel.save(context, TEST_PORTFOLIO_NAME, transaction)

        val item = viewModel.portfoliosStateFlow.value.getValue(TEST_PORTFOLIO_NAME).items.single()
        assertEquals(bitcoin.id, item.assetId)
        assertEquals(BigDecimal("0.00000001"), item.quantity)
        assertTrue(item.assetEvents.isNotEmpty())
        assertTrue(item.legacyStockEvents.isEmpty())
        assertEquals(setOf(bitcoin.id), context.getSharedPreferences(TEST_PORTFOLIO_NAME, Context.MODE_PRIVATE).all.keys)
    }

    @Test
    fun `Crom portfolio mimics the first crypto purchase`() {
        val sharedPreferences = context.getSharedPreferences(TEST_CLASS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putStringSet(
                Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET,
                setOf(PortfolioRepository.DEFAULT_PORTFOLIO_NAME, PortfolioRepository.CROM_PORTFOLIO_NAME)
            )
            .commit()
        PortfolioRepository.init(sharedPreferences)
        context.getSharedPreferences(PortfolioRepository.DEFAULT_PORTFOLIO_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()
        viewModel = HomeViewModel(PortfolioRepository, coroutineScopeTestRule.testDispatcher)
        val bitcoin = AssetCatalog.cryptocurrencies.first()
        val firstBuy = AssetTransaction(
            assetId = bitcoin.id,
            assetType = bitcoin.type,
            symbol = bitcoin.symbol,
            displayName = bitcoin.displayName,
            quoteCurrencyCode = bitcoin.quoteCurrency.currencyCode,
            action = TransactionAction.BUY,
            dateInMillis = 1L,
            unitPrice = BigDecimal("60000"),
            quantity = BigDecimal("0.01")
        )

        viewModel.save(context, PortfolioRepository.DEFAULT_PORTFOLIO_NAME, firstBuy)

        val cromItem = viewModel.portfoliosStateFlow.value
            .getValue(PortfolioRepository.CROM_PORTFOLIO_NAME).items.single()
        assertEquals(bitcoin.id, cromItem.assetId)
        assertEquals(firstBuy.quantity, cromItem.quantity)
        assertEquals(listOf(firstBuy), cromItem.assetEvents.mapNotNull { it.transaction })
    }

    @Test
    fun `crypto holding displays asset type and stale price`() {
        val bitcoin = AssetCatalog.cryptocurrencies.first()
        viewModel.save(
            context,
            TEST_PORTFOLIO_NAME,
            AssetTransaction(
                assetId = bitcoin.id,
                assetType = bitcoin.type,
                symbol = bitcoin.symbol,
                displayName = bitcoin.displayName,
                quoteCurrencyCode = bitcoin.quoteCurrency.currencyCode,
                action = TransactionAction.BUY,
                dateInMillis = 1L,
                unitPrice = BigDecimal("60000"),
                quantity = BigDecimal("0.5")
            )
        )
        StockPriceRepository.updateAssetPrices(
            assetPrices = listOf(AssetPrice(bitcoin.id, bitcoin.quoteCurrency, BigDecimal("61000.25"))),
            requestedAssetIds = setOf(bitcoin.id)
        )
        StockPriceRepository.updateAssetPrices(assetPrices = emptyList(), requestedAssetIds = setOf(bitcoin.id))

        setContent()

        composeTestRule.onNodeWithText("[CRYPTO] Bitcoin (BTC)").assertIsDisplayed()
        composeTestRule.onNodeWithText("(stale)", substring = true).assertIsDisplayed()
    }

    private fun seedPortfolio() {
        StockPriceRepository.put(
            setOf(
                StockPrice(stockSymbol = "TSLA", currency = Currency.getInstance("USD"), price = 10.0),
                StockPrice(stockSymbol = "INTC", currency = Currency.getInstance("USD"), price = 10.0)
            )
        )
        viewModel.save(
            context = context,
            portfolioName = TEST_PORTFOLIO_NAME,
            stockOrder = StockOrder(
                orderAction = "Buy",
                currency = "USD",
                dateInMillis = 1L,
                name = "TSLA",
                pricePerStock = 10.0,
                commissionFee = 0.0,
                quantity = 1
            )
        )
        viewModel.save(
            context = context,
            portfolioName = TEST_PORTFOLIO_NAME,
            stockOrder = StockOrder(
                orderAction = "Buy",
                currency = "USD",
                dateInMillis = 2L,
                name = "INTC",
                pricePerStock = 10.0,
                commissionFee = 0.0,
                quantity = 1
            )
        )
    }

    private fun setContent() {
        composeTestRule.setContent {
            Home(
                viewModel = viewModel,
                onNavigateTo = {},
                appUpdateManager = AppUpdateManagerFactory.create(context)
            )
        }
    }

    private fun nodeTop(text: String): Float =
        composeTestRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot.top

    private companion object {
        const val TEST_CLASS_NAME = "HomeComposablesKtTest"
        const val TEST_PORTFOLIO_NAME = "HomeComposablesKtTestPortfolio"
        const val TESLA_NAME = "Tesla, Inc. (TSLA)"
        const val INTEL_NAME = "Intel Corporation (INTC)"
    }
}
