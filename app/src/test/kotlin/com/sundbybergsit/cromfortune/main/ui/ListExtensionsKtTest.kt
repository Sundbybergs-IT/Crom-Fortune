package com.sundbybergsit.cromfortune.main.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.main.isWithinConfiguredTimeInterval
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.DayOfWeek
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class ListExtensionsKtTest {

    @Test
    fun `isWithinConfiguredTimeInterval - includes start and excludes end`() {
        val days = listOf(DayOfWeek.MONDAY)

        assertTrue(
            days.isWithinConfiguredTimeInterval(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 0), LocalTime.of(17, 0)
            )
        )
        assertFalse(
            days.isWithinConfiguredTimeInterval(
                DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(9, 0), LocalTime.of(17, 0)
            )
        )
    }

    @Test
    fun `isWithinConfiguredTimeInterval - supports interval over midnight`() {
        val days = listOf(DayOfWeek.MONDAY)

        assertTrue(
            days.isWithinConfiguredTimeInterval(
                DayOfWeek.MONDAY, LocalTime.of(23, 0), LocalTime.of(22, 0), LocalTime.of(2, 0)
            )
        )
        assertTrue(
            days.isWithinConfiguredTimeInterval(
                DayOfWeek.TUESDAY, LocalTime.of(1, 0), LocalTime.of(22, 0), LocalTime.of(2, 0)
            )
        )
        assertFalse(
            days.isWithinConfiguredTimeInterval(
                DayOfWeek.TUESDAY, LocalTime.of(2, 0), LocalTime.of(22, 0), LocalTime.of(2, 0)
            )
        )
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
