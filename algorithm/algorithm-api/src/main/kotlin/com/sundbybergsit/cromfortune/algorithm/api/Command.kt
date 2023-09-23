package com.sundbybergsit.cromfortune.algorithm.api

fun interface Command<T> {

    fun execute(item : T)

}
