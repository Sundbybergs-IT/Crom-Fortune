package com.sundbybergsit.cromfortune.main.ui.home

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
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
import java.util.Currency
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class HomeComposablesKtTest {

    @get:Rule
    val cromTestRule = CromTestRule()

    @get:Rule
    val coroutineScopeTestRule = CoroutineScopeTestRule()

    @get:Rule
    val composeTestRule: createComposeRule = createComposeRule()

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
        assertTrue(teslaTopBefore < intelTopBefore)

        viewModel.sortNameAscending(TEST_PORTFOLIO_NAME)
        composeTestRule.waitForIdle()

        val teslaTopAfter = nodeTop(TESLA_NAME)
        val intelTopAfter = nodeTop(INTEL_NAME)
        assertTrue(intelTopAfter < teslaTopAfter)
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
        assertTrue(teslaTopBefore < intelTopBefore)

        viewModel.sortProfitDescending(TEST_PORTFOLIO_NAME)
        composeTestRule.waitForIdle()

        val teslaTopAfter = nodeTop(TESLA_NAME)
        val intelTopAfter = nodeTop(INTEL_NAME)
        assertTrue(teslaTopAfter < intelTopAfter)
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
