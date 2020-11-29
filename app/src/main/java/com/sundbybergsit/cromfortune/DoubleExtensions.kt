package com.sundbybergsit.cromfortune

import java.util.*

fun Double.roundTo(n: Int): Double {
    return String.format(Locale.ENGLISH, "%.${n}f", this).toDouble()
}
