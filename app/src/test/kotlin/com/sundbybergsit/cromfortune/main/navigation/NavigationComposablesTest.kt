package com.sundbybergsit.cromfortune.main.navigation

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.main.BuildConfig
import com.sundbybergsit.cromfortune.main.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class NavigationComposablesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `bottom navigation excludes notifications`() {
        composeTestRule.setContent {
            BottomNavigation(currentTab = Home, onNavigationSelected = {})
        }

        composeTestRule.onAllNodesWithText(context.getString(R.string.notifications_title)).assertCountEquals(0)
        composeTestRule.onNodeWithText(context.getString(R.string.home_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.dashboard_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.transactions_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.navigation_settings)).assertIsDisplayed()
    }

    @Test
    fun `about menu item invokes about action`() {
        var aboutActionInvoked = false
        composeTestRule.setContent {
            SettingsItems(
                onShowSupportedStocks = {},
                onShowSupportedCryptocurrencies = {},
                onShowStockRetrievalTimeIntervals = {},
                onShowAbout = { aboutActionInvoked = true },
                onShowTodo = {}
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.action_about)).performClick()

        assertTrue(aboutActionInvoked)
    }

    @Test
    fun `about dialog displays app name version and creator`() {
        composeTestRule.setContent {
            AboutDialog(onDismiss = {})
        }

        composeTestRule.onNodeWithText(context.getString(R.string.action_about)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.about_app_tagline)).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.about_app_version, BuildConfig.VERSION_NAME)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.about_app_created_by)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_close)).assertIsDisplayed()
    }
}
