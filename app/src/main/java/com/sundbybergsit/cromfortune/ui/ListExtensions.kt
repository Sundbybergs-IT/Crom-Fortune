package com.sundbybergsit.cromfortune.ui

import java.time.DayOfWeek
import java.time.LocalTime

fun List<DayOfWeek>.isWithinConfiguredTimeInterval(
        currentDayOfWeek: DayOfWeek,
        currentTime: LocalTime,
        fromTime: LocalTime,
        toTime: LocalTime,
): Boolean {
    return contains(currentDayOfWeek) && currentTime.isAfter(fromTime) && currentTime.isBefore(toTime)
}

