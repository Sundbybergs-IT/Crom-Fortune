package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockDataRetrievalCoroutineWorkerNotificationWindowTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @get:Rule
    val cromTestRule = CromTestRule()

    @Before
    fun setUp() {
        StockRetrievalSettings(context).set(
            fromTimeHours = 9,
            fromTimeMinutes = 0,
            toTimeHours = 17,
            toTimeMinutes = 0,
            weekDays = listOf(DayOfWeek.MONDAY)
        )
    }

    @Test
    fun `isWithinNotificationWindow - when inside configured interval - returns true`() {
        val result = StockDataRetrievalCoroutineWorker.isWithinNotificationWindow(
            context = context,
            currentDayOfWeek = DayOfWeek.MONDAY,
            currentTime = LocalTime.of(10, 0)
        )

        assertTrue(result)
    }

    @Test
    fun `isWithinNotificationWindow - when outside configured interval - returns false`() {
        val result = StockDataRetrievalCoroutineWorker.isWithinNotificationWindow(
            context = context,
            currentDayOfWeek = DayOfWeek.MONDAY,
            currentTime = LocalTime.of(18, 0)
        )

        assertFalse(result)
    }
}
