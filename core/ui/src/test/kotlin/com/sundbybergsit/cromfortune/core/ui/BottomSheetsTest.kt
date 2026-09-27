package com.sundbybergsit.cromfortune.core.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class BottomSheetsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `menu item invokes its action`() {
        var clicked = false
        composeTestRule.setContent {
            BottomSheetMenuItem(text = "Action", onClick = { clicked = true })
        }

        composeTestRule.onNodeWithText("Action").performClick()

        assertTrue(clicked)
    }
}
