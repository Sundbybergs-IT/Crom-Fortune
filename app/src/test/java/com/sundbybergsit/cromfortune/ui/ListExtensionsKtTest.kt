package com.sundbybergsit.cromfortune.ui

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.isWithinConfiguredTimeInterval
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class ListExtensionsKtTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `isWithinConfiguredTimeInterval - when correct day and time - returns true`() {
        val currentTime = LocalTime.of(11, 0)
        val currentDayOfWeek = DayOfWeek.WEDNESDAY
        val fromTime = LocalTime.of(9, 0)
        val toTime = LocalTime.of(22, 0)
        val weekDays = setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
                .map { stringRepresentation ->
                    DayOfWeek.valueOf(stringRepresentation)
                }

        val withinConfiguredTimeInterval = weekDays.isWithinConfiguredTimeInterval(currentDayOfWeek, currentTime,
                fromTime, toTime)

        assertTrue(withinConfiguredTimeInterval)
    }

    @Test
    fun `isWithinConfiguredTimeInterval - when wrong day but correct time - returns false`() {
        val currentTime = LocalTime.of(11, 0)
        val currentDayOfWeek = DayOfWeek.SATURDAY
        val fromTime = LocalTime.of(9, 0)
        val toTime = LocalTime.of(22, 0)
        val weekDays = setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
                .map { stringRepresentation ->
                    DayOfWeek.valueOf(stringRepresentation)
                }

        val withinConfiguredTimeInterval = weekDays.isWithinConfiguredTimeInterval(currentDayOfWeek, currentTime,
                fromTime, toTime)

        assertFalse(withinConfiguredTimeInterval)
    }

    @Test
    fun `isWithinConfiguredTimeInterval - when it is not - returns false`() {
        val currentTime = LocalTime.of(3, 0)
        val currentDayOfWeek = DayOfWeek.SATURDAY
        val fromTime = LocalTime.of(9, 0)
        val toTime = LocalTime.of(22, 0)
        val weekDays = setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
                .map { stringRepresentation ->
                    DayOfWeek.valueOf(stringRepresentation)
                }

        val withinConfiguredTimeInterval = weekDays.isWithinConfiguredTimeInterval(currentDayOfWeek, currentTime,
                fromTime, toTime)

        assertFalse(withinConfiguredTimeInterval)
    }

}
