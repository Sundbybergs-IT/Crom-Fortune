package com.sundbybergsit.cromfortune.main

import java.time.DayOfWeek
import java.time.LocalTime

fun List<DayOfWeek>.isWithinConfiguredTimeInterval(
        currentDayOfWeek: DayOfWeek,
        currentTime: LocalTime,
        fromTime: LocalTime,
        toTime: LocalTime,
): Boolean {
    if (fromTime == toTime) return contains(currentDayOfWeek)
    return if (fromTime < toTime) {
        contains(currentDayOfWeek) && currentTime >= fromTime && currentTime < toTime
    } else {
        (contains(currentDayOfWeek) && currentTime >= fromTime) ||
            (contains(currentDayOfWeek.minus(1)) && currentTime < toTime)
    }
}

