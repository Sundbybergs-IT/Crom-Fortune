package com.sundbybergsit.cromfortune.domain

import org.hamcrest.CoreMatchers
import org.junit.Assume.assumeThat

fun assumeEquals(expected: Any?, actual: Any?) {
    assumeThat(actual, CoreMatchers.`is`(expected))
}
