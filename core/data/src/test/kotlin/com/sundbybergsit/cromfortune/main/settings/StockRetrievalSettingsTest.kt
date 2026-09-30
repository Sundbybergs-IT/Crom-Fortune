package com.sundbybergsit.cromfortune.main.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.DayOfWeek

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockRetrievalSettingsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun clearSettings() {
        context.getSharedPreferences(StockRetrievalSettings.PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun `refresh interval defaults to one hour`() {
        assertEquals(
            StockRetrievalSettings.RefreshInterval.ONE_HOUR,
            StockRetrievalSettings(context).timeInterval.value.refreshInterval
        )
    }

    @Test
    fun `selected refresh interval is persisted`() {
        StockRetrievalSettings(context).set(
            fromTimeHours = 8,
            fromTimeMinutes = 0,
            toTimeHours = 17,
            toTimeMinutes = 0,
            weekDays = listOf(DayOfWeek.MONDAY),
            refreshInterval = StockRetrievalSettings.RefreshInterval.THREE_HOURS
        )

        assertEquals(
            StockRetrievalSettings.RefreshInterval.THREE_HOURS,
            StockRetrievalSettings(context).timeInterval.value.refreshInterval
        )
    }
}
