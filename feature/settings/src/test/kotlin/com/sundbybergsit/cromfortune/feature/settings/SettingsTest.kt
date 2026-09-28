package com.sundbybergsit.cromfortune.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class SettingsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `settings content is displayed`() {
        composeTestRule.setContent {
            Settings(
                onShowRetrievalIntervals = {},
                onShowSupportedStocks = {},
                onShowSupportedCryptocurrencies = {},
                onShowIssues = {},
                onShowAbout = {}
            )
        }

        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_retrieval_intervals)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_supported_stocks)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.settings_about))
            .performScrollTo()
            .assertIsDisplayed()
    }
}
