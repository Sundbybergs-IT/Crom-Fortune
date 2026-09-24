package com.sundbybergsit.cromfortune.main.ui.home

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.TransactionAction
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.main.CoroutineScopeTestRule
import com.sundbybergsit.cromfortune.main.CromTestRule
import com.sundbybergsit.cromfortune.main.Databases
import com.sundbybergsit.cromfortune.main.PortfolioRepository
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.notifications.NotificationsRepositoryImpl
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
        NotificationsRepositoryImpl(context).clear()
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

        assertEquals(listOf(INTEL_SYMBOL, TESLA_SYMBOL), currentSymbols())
        scrollToHolding(1)
        composeTestRule.onNodeWithText(TESLA_NAME).assertIsDisplayed()

        viewModel.sortNameDescending(TEST_PORTFOLIO_NAME)
        composeTestRule.waitForIdle()

        assertEquals(listOf(TESLA_SYMBOL, INTEL_SYMBOL), currentSymbols())
        assertEquals(
            HomeViewModel.SortOrder.NAME_DESCENDING,
            viewModel.portfoliosStateFlow.value.getValue(TEST_PORTFOLIO_NAME).sortOrder
        )
        scrollToHolding(1)
        composeTestRule.onNodeWithText(INTEL_NAME).assertIsDisplayed()
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

        assertEquals(listOf(INTEL_SYMBOL, TESLA_SYMBOL), currentSymbols())

        viewModel.sortProfitDescending(TEST_PORTFOLIO_NAME)
        composeTestRule.waitForIdle()

        assertEquals(listOf(TESLA_SYMBOL, INTEL_SYMBOL), currentSymbols())
        assertEquals(
            HomeViewModel.SortOrder.PROFIT_DESCENDING,
            viewModel.portfoliosStateFlow.value.getValue(TEST_PORTFOLIO_NAME).sortOrder
        )
        scrollToHolding(1)
        composeTestRule.onNodeWithText(INTEL_NAME).assertIsDisplayed()
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
    fun `portfolio summary remains visible when portfolio is empty`() {
        setContent()

        composeTestRule.onNodeWithText(context.getString(R.string.home_portfolio_profit)).assertIsDisplayed()
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
    fun `Crom applies a Default notification only to its matching stock`() {
        configureCromPortfolio()
        NotificationsRepositoryImpl(context).add(
            notification(portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME, symbol = TESLA_SYMBOL)
        )

        saveDefaultBuy(TESLA_SYMBOL)
        saveDefaultBuy(INTEL_SYMBOL)

        assertTrue(cromItem(TESLA_SYMBOL).quantity < BigDecimal("50"))
        assertEquals(BigDecimal("50"), cromItem(INTEL_SYMBOL).quantity)
    }

    @Test
    fun `Crom ignores notifications for another portfolio`() {
        configureCromPortfolio()
        NotificationsRepositoryImpl(context).add(
            notification(portfolioName = TEST_PORTFOLIO_NAME, symbol = TESLA_SYMBOL)
        )

        saveDefaultBuy(TESLA_SYMBOL)

        assertEquals(BigDecimal("50"), cromItem(TESLA_SYMBOL).quantity)
    }

    @Test
    fun `removing a notification removes its derived Crom trade`() {
        configureCromPortfolio()
        val repository = NotificationsRepositoryImpl(context)
        val notification = notification(PortfolioRepository.DEFAULT_PORTFOLIO_NAME, TESLA_SYMBOL)
        repository.add(notification)
        saveDefaultBuy(TESLA_SYMBOL)
        assertTrue(cromItem(TESLA_SYMBOL).quantity < BigDecimal("50"))

        repository.remove(notification)
        viewModel.showCurrent(context)

        assertEquals(BigDecimal("50"), cromItem(TESLA_SYMBOL).quantity)
        assertTrue(
            context.getSharedPreferences(PortfolioRepository.CROM_PORTFOLIO_NAME, Context.MODE_PRIVATE).all.isEmpty()
        )
    }

    @Test
    fun `Crom ignores a notification before the first transaction`() {
        configureCromPortfolio()
        NotificationsRepositoryImpl(context).add(
            notification(
                portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME,
                symbol = TESLA_SYMBOL,
                dateInMillis = 0L
            )
        )

        saveDefaultBuy(TESLA_SYMBOL)

        assertEquals(BigDecimal("50"), cromItem(TESLA_SYMBOL).quantity)
    }

    @Test
    fun `Crom notification buy uses proceeds from an earlier Crom sale`() {
        configureCromPortfolio()
        NotificationsRepositoryImpl(context).add(
            notification(
                portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME,
                symbol = TESLA_SYMBOL,
                dateInMillis = SIXTEEN_DAYS_IN_MILLIS,
                price = 70.0
            )
        )
        saveDefaultBuy(TESLA_SYMBOL)
        saveDefaultSell(TESLA_SYMBOL, dateInMillis = EIGHT_DAYS_IN_MILLIS, price = 200.0)

        val actions = cromItem(TESLA_SYMBOL).legacyStockEvents.mapNotNull { event ->
            event.stockOrder?.orderAction
        }

        assertEquals(listOf("Buy", "Sell", "Buy"), actions)
    }

    @Test
    fun `Crom ignores notifications without structured stock data`() {
        configureCromPortfolio()
        NotificationsRepositoryImpl(context).add(
            NotificationMessage(dateInMillis = EIGHT_DAYS_IN_MILLIS, message = "legacy notification")
        )

        saveDefaultBuy(TESLA_SYMBOL)

        assertEquals(BigDecimal("50"), cromItem(TESLA_SYMBOL).quantity)
    }

    @Test
    fun `Litecoin holding displays its name and stale price`() {
        val litecoin = requireNotNull(AssetCatalog.findById("crypto:LTC"))
        viewModel.save(
            context,
            TEST_PORTFOLIO_NAME,
            AssetTransaction(
                assetId = litecoin.id,
                assetType = litecoin.type,
                symbol = litecoin.symbol,
                displayName = litecoin.displayName,
                quoteCurrencyCode = litecoin.quoteCurrency.currencyCode,
                action = TransactionAction.BUY,
                dateInMillis = 1L,
                unitPrice = BigDecimal("85.40"),
                quantity = BigDecimal("0.5")
            )
        )
        StockPriceRepository.updateAssetPrices(
            assetPrices = listOf(AssetPrice(litecoin.id, litecoin.quoteCurrency, BigDecimal("92.25"))),
            requestedAssetIds = setOf(litecoin.id)
        )
        StockPriceRepository.updateAssetPrices(assetPrices = emptyList(), requestedAssetIds = setOf(litecoin.id))

        setContent()

        scrollToHolding(0)
        composeTestRule.onNodeWithText("Litecoin").assertIsDisplayed()
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

    private fun currentSymbols(): List<String> = viewModel.portfoliosStateFlow.value
        .getValue(TEST_PORTFOLIO_NAME)
        .items
        .map(PortfolioItem::symbol)

    private fun scrollToHolding(index: Int) {
        composeTestRule.onAllNodes(hasScrollAction())[1].performScrollToIndex(index + 1)
        composeTestRule.waitForIdle()
    }

    private fun configureCromPortfolio() {
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
        context.getSharedPreferences(PortfolioRepository.CROM_PORTFOLIO_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()
        viewModel = HomeViewModel(PortfolioRepository, coroutineScopeTestRule.testDispatcher)
    }

    private fun saveDefaultBuy(symbol: String) {
        viewModel.save(
            context = context,
            portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME,
            stockOrder = StockOrder(
                orderAction = "Buy",
                currency = "SEK",
                dateInMillis = 1L,
                name = symbol,
                pricePerStock = 100.0,
                commissionFee = 0.0,
                quantity = 50
            )
        )
    }

    private fun saveDefaultSell(symbol: String, dateInMillis: Long, price: Double) {
        viewModel.save(
            context = context,
            portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME,
            stockOrder = StockOrder(
                orderAction = "Sell",
                currency = "SEK",
                dateInMillis = dateInMillis,
                name = symbol,
                pricePerStock = price,
                commissionFee = 0.0,
                quantity = 1
            )
        )
    }

    private fun notification(
        portfolioName: String,
        symbol: String,
        dateInMillis: Long = EIGHT_DAYS_IN_MILLIS,
        price: Double = 200.0
    ) = NotificationMessage(
        dateInMillis = dateInMillis,
        message = "recommendation",
        portfolioName = portfolioName,
        stockSymbol = symbol,
        currencyCode = "SEK",
        pricePerStock = price
    )

    private fun cromItem(symbol: String) = viewModel.portfoliosStateFlow.value
        .getValue(PortfolioRepository.CROM_PORTFOLIO_NAME)
        .items.single { item -> item.symbol == symbol }

    private companion object {
        const val TEST_CLASS_NAME = "HomeComposablesKtTest"
        const val TEST_PORTFOLIO_NAME = "HomeComposablesKtTestPortfolio"
        const val TESLA_NAME = "Tesla, Inc."
        const val INTEL_NAME = "Intel Corporation"
        const val TESLA_SYMBOL = "TSLA"
        const val EIGHT_DAYS_IN_MILLIS = 8L * 24L * 60L * 60L * 1000L
        const val SIXTEEN_DAYS_IN_MILLIS = 16L * 24L * 60L * 60L * 1000L
        const val INTEL_SYMBOL = "INTC"
    }
}
