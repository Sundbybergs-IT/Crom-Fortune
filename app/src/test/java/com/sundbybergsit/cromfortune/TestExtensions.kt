package com.sundbybergsit.cromfortune

import org.hamcrest.CoreMatchers
import org.junit.Assume

fun assumeEquals(expected: Any?, actual: Any?) {
    Assume.assumeThat(actual, CoreMatchers.`is`(expected))
}
