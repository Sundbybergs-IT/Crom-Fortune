package com.sundbybergsit.cromfortune.main.navigation

import org.junit.Test
import java.text.DateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NavigationDateFormattingTest {

    @Test
    fun `transaction date follows locale short date format`() {
        val dateInMillis = LocalDate.of(2026, 6, 24)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
        val swedish = formatShortDate(dateInMillis, Locale.forLanguageTag("sv-SE"))
        val american = formatShortDate(dateInMillis, Locale.US)

        assertEquals(
            DateFormat.getDateInstance(DateFormat.SHORT, Locale.forLanguageTag("sv-SE"))
                .format(Date(dateInMillis)),
            swedish,
        )
        assertEquals(
            DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(Date(dateInMillis)),
            american,
        )
        assertNotEquals(swedish, american)
    }
}
