package com.sundbybergsit.cromfortune.algorithm

import java.util.Locale

fun Double.roundTo(n: Int): Double {
    return String.format(Locale.ENGLISH, "%.${n}f", this).toDouble()
}
