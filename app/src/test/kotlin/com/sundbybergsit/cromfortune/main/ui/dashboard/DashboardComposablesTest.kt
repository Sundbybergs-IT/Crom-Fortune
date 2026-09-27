package com.sundbybergsit.cromfortune.main.ui.dashboard

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.main.CromTestRule
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.ui.home.PortfolioItem
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.util.Currency

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class DashboardComposablesTest {
    @get:Rule
    val cromTestRule = CromTestRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `portfolio value uses quantity price and currency rate`() {
        CurrencyRateRepository.addAll(setOf(CurrencyRate("USD", 10.0)))
        StockPriceRepository.updateAssetPrices(
            assetPrices = listOf(AssetPrice("stock:TEST", Currency.getInstance("USD"), BigDecimal("12.50"))),
            requestedAssetIds = setOf("stock:TEST")
        )

        assertEquals(BigDecimal("250.000"), listOf(item(quantity = "2")).portfolioMarketValueSek())
    }

    @Test
    fun `Crom shakes his fist when user portfolio is worth more`() {
        composeTestRule.setContent {
            AiStonk(
                mood = AiStonkMood.Neutral,
                userPortfolioIsWorthMore = true,
                modifier = Modifier.size(200.dp)
            )
        }

        composeTestRule.onNodeWithContentDescription("shaking his fist", substring = true).assertIsDisplayed()
    }

    @Test
    fun `arms match angry expression when Crom is not behind`() {
        composeTestRule.setContent {
            AiStonk(
                mood = AiStonkMood.Angry,
                userPortfolioIsWorthMore = false,
                modifier = Modifier.size(200.dp)
            )
        }

        composeTestRule.onNodeWithContentDescription("matching his expression", substring = true).assertIsDisplayed()
    }

    private fun item(quantity: String) = PortfolioItem(
        assetId = "stock:TEST",
        assetType = AssetType.STOCK,
        symbol = "TEST",
        displayName = "Test",
        currency = Currency.getInstance("USD"),
        quantity = BigDecimal(quantity),
        acquisitionValue = BigDecimal.ONE,
        profitCalculator = { BigDecimal.ZERO }
    )
}
